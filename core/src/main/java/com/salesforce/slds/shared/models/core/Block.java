/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

public class Block extends Input {
    private int lineNumber;
    private String functionName;
    private String value;

    private Block(int lineNumber, String functionName, String value) {
        this.lineNumber = lineNumber;
        this.functionName = functionName;
        this.value = value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static BlockBuilder builder() {
        return new BlockBuilder();
    }

    @Override
    public Type getType() {
        return Type.JAVASCRIPT;
    }

    public static class BlockBuilder {
        private int lineNumber;
        private String functionName;
        private String value;

        public BlockBuilder lineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public BlockBuilder functionName(String functionName) {
            this.functionName = functionName;
            return this;
        }

        public BlockBuilder value(String value) {
            this.value = value;
            return this;
        }

        public Block build() {
            return new Block(lineNumber, functionName, value);
        }
    }
}
