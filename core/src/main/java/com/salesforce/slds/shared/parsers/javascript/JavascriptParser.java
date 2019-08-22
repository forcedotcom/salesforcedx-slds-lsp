/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.parsers.javascript;

import com.salesforce.slds.shared.models.core.Block;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavascriptParser {

    public static List<Block> convert(String path, List<String> lines) {

        try {
            CompilerEnvirons env = new CompilerEnvirons();
            env.setRecoverFromErrors(true);
            env.setStrictMode(false);
            env.setWarnTrailingComma(false);
            AstRoot node = new Parser(env).parse(
                    StringUtils.collectionToDelimitedString(lines, System.lineSeparator()),
                    path, 1);

            BlockVisitor blockVisitor = new BlockVisitor();
            node.visitAll(blockVisitor);

            return blockVisitor.getBlocks()
                    .stream().filter(block -> block.getValue() != null)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            Optional<Block> optionalBlock = defaultBlock(lines);

            List<Block> results = new ArrayList<>();
            if (optionalBlock.isPresent()) {
                results.add(optionalBlock.get());
            }

            return results;
        }
    }

    private static Optional<Block> defaultBlock(List<String> lines) {
        int index = 0;
        for (String line : lines) {
            if (line.contains("slds-")) {
                Block.BlockBuilder builder = Block.builder();

                builder.value(line)
                        .lineNumber(index)
                        .functionName("");

                return Optional.of(builder.build());
            }
            index++;
        }

        return Optional.empty();
    }

    enum JAVASCRIPT_TYPE { LIBRARY, COMPONENT }

    static class BlockVisitor implements NodeVisitor {
        final List<Block> blocks = new ArrayList<>();
        JAVASCRIPT_TYPE type = null;

        public List<Block> getBlocks() {
            return blocks;
        }

        @Override
        public boolean visit(AstNode node) {
            if (node instanceof AstRoot) {
                type = node.getFirstChild() instanceof FunctionNode ? JAVASCRIPT_TYPE.LIBRARY : JAVASCRIPT_TYPE.COMPONENT;
            }

           if (node instanceof FunctionNode && type == JAVASCRIPT_TYPE.COMPONENT) {
               return visitFunctionNode((FunctionNode)node);
           }

           if (type == JAVASCRIPT_TYPE.LIBRARY) {
                if (node instanceof VariableInitializer) {
                    return visitVariableInitializer((VariableInitializer) node);
                }

                if (node instanceof ReturnStatement) {
                    return visitReturnStatement((ReturnStatement)node);
                }
           }

           return true;
        }

        private boolean visitReturnStatement(ReturnStatement block) {
            Block.BlockBuilder builder = Block.builder();
            if (block.getReturnValue() instanceof ObjectLiteral) {
                builder.value(block.toSource())
                        .lineNumber(block.getLineno())
                        .functionName(block.shortName());

                blocks.add(builder.build());
            }
            return false;
        }

        private boolean visitVariableInitializer(VariableInitializer block) {
            Block.BlockBuilder builder = Block.builder();

            String functionName = block.getTarget().toSource();

            builder.functionName(functionName)
                    .lineNumber(block.getLineno());

            if (block.getInitializer() != null) {
                builder.value(block.getInitializer().toSource());
            }

            blocks.add(builder.build());

            return false;
        }

        private boolean visitFunctionNode(FunctionNode functionNode) {
            Block.BlockBuilder builder = Block.builder();

            String functionName = functionNode.getName();
            if (functionName.isEmpty()) {
                if (functionNode.getParent() instanceof ObjectProperty) {
                    functionName = ((ObjectProperty)functionNode.getParent()).getLeft().toSource();
                }
            }

            if (functionName.isEmpty()) {
                return true;
            }

            builder.functionName(functionName)
                    .value(functionNode.toSource())
                    .lineNumber(functionNode.getLineno());

            blocks.add(builder.build());

            return false;
        }
    }
}
