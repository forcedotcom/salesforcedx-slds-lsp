/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.codeactions;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.salesforce.slds.lsp.diagnostics.Identifier;
import com.salesforce.slds.lsp.diagnostics.Identifier.DiagnosticCode;
import com.salesforce.slds.lsp.models.DiagnosticResult;
import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.shared.models.annotations.AnnotationType;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static com.salesforce.slds.shared.models.core.Input.Type.MARKUP;
import static com.salesforce.slds.shared.models.core.Input.Type.STYLE;

@Component
public class CodeActionConverter {
    @Lazy
    @Autowired
    private TextDocumentRegistry documentRegistry;

    public static final String FILE_IGNORE_SLDS_VALIDATION = "Ignore mobile SLDS validation for this file";
    public static final String LINE_IGNORE_SLDS_VALIDATION = "Ignore mobile SLDS validation for this line";

    public List<Either<Command, CodeAction>> convert(CodeActionParams params) {
        List<DiagnosticResult> diagnosticResults = documentRegistry.getDiagnosticResults(params.getTextDocument().getUri());

        return params.getContext().getDiagnostics().stream().filter(diagnostic -> process(diagnostic, params)).map(diagnostic -> {
            Optional<DiagnosticResult> info = diagnosticResults.stream()
                    .filter(result -> result.getDiagnostic().equals(diagnostic)).findFirst();

            if (info.isPresent()) {
                DiagnosticCode code = DiagnosticCode.getCode(diagnostic.getCode().getLeft());

                switch (code) {
                    case INVALID_TOKENS:
                        return createDeprecatedTokenCodeAction(params, info.get());
                    case ALTERNATIVE_TOKENS:
                        return createAlternativeTokenCodeAction(params, info.get());
                    case UTILITY_TOKENS:
                        return createUtilityTokenCodeAction(params, info.get());
                    case MOBILE_SLDS:
                        return createNonMobileFriendlyCodeAction(params, info.get());
                    default:
                        break;
                }
            }

            return new ArrayList<Either<Command, CodeAction>>();
        }).flatMap(t -> t.stream()).collect(Collectors.toList());

    }

    private List<Either<Command, CodeAction>> createUtilityTokenCodeAction(CodeActionParams params,
                                                                           DiagnosticResult diagnosticResult) {
        List<Either<Command, CodeAction>> actions = Lists.newArrayList();

        for (Item item : diagnosticResult.getItems()) {

            Map<String, List<Action>> actionGroupByName = item.getActions().stream()
                    .collect(Collectors.groupingBy(Action::getName, TreeMap::new, Collectors.toList()));

            actionGroupByName.forEach((name, actionGroup) -> {

                Map<String, List<TextEdit>> changes = Maps.newLinkedHashMap();
                List<TextEdit> styleEdits = Lists.newArrayList();
                CodeAction codeAction = new CodeAction("Update to utility class \'" + name.replaceAll("\\.", "") + "\'");
                codeAction.setKind(CodeActionKind.QuickFix);
                codeAction.setDiagnostics(Lists.newArrayList(diagnosticResult.getDiagnostic()));

                for (int index = 0; index < actionGroup.size() ; index++) {
                    Action action = actionGroup.get(index);

                    if(index == 0 && action.getInformation() != null) {
                        action.getInformation().forEach(info -> {
                            List<TextEdit> edits = changes.getOrDefault(info.getPath(), new ArrayList<>());

                            TextEdit edit = new TextEdit(convertRange(info.getRange()), info.getValue());
                            edits.add(edit);
                            changes.put(info.getPath(), edits);
                        });
                    }

                    styleEdits.add(new TextEdit(convertRange(action.getRange()), ""));
                }

                changes.put(params.getTextDocument().getUri(), styleEdits);

                WorkspaceEdit workspaceEdit = new WorkspaceEdit();
                workspaceEdit.setChanges(changes);

                codeAction.setEdit(workspaceEdit);
                actions.add(Either.forRight(codeAction));
            });
        }

        return actions;
    }

    private CodeAction buildCodeAction(CodeActionParams params, Action action, Diagnostic diagnostic, String codeActionTitle, Range textEditRange, String textEditNewText) {
        CodeAction codeAction = new CodeAction(codeActionTitle);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Lists.newArrayList(diagnostic));

        TextEdit edit = new TextEdit(textEditRange, textEditNewText);
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();

        Map<String, List<TextEdit>> changes = Maps.newLinkedHashMap();
        changes.put(params.getTextDocument().getUri(), Lists.newArrayList(edit));
        workspaceEdit.setChanges(changes);

        codeAction.setEdit(workspaceEdit);
        return codeAction;
    }

    private List<Either<Command, CodeAction>> createNonMobileFriendlyCodeAction(CodeActionParams params, DiagnosticResult diagnosticResult) {
        List<Either<Command, CodeAction>> actions = Lists.newArrayList();

        for (Item item : diagnosticResult.getItems()) {
            for (Action action : item.getActions()) {
                Diagnostic diagnostic = diagnosticResult.getDiagnostic();
                Optional<Input.Type> fileType = action.getFileType();
                Boolean isStyle = fileType.isPresent() && fileType.get() == Input.Type.STYLE;

                // Adding ignore action for entire file
                if (!isStyle) {
                    CodeAction ignoreFileCodeAction = buildCodeAction(
                        params,
                        action,
                        diagnostic,
                        CodeActionConverter.FILE_IGNORE_SLDS_VALIDATION,
                        new Range(new Position(0,0), new Position(0, 0)),
                        "<!-- " + AnnotationType.IGNORE.value() + " -->\n");
                    actions.add(Either.forRight(ignoreFileCodeAction));
                }

                // Adding ignore action for a specific line
                Range range = isStyle ? convertRange(action.getInformation().get(0).getRange()) : diagnostic.getRange();
                int startColumn = range.getStart().getCharacter();
                String indent = (startColumn > 0) ? String.format("%" + startColumn + "c", ' ') : "";
                String ignoreNextLine = isStyle ?  "/* @" + AnnotationType.IGNORE.value() + " */" : "<!-- " + AnnotationType.IGNORE_NEXT_LINE.value() + " -->";
                CodeAction ignoreLineCodeAction = buildCodeAction(
                        params,
                        action,
                        diagnostic,
                        CodeActionConverter.LINE_IGNORE_SLDS_VALIDATION,
                        new Range(range.getStart(), range.getStart()),
                        ignoreNextLine + "\n" + indent);
                actions.add(Either.forRight(ignoreLineCodeAction));
            }
        }

        return actions;
    }

    private List<Either<Command, CodeAction>> createAlternativeTokenCodeAction(CodeActionParams params,
                                                                               DiagnosticResult diagnosticResult) {
        List<Either<Command, CodeAction>> actions = Lists.newArrayList();

        for (Item item : diagnosticResult.getItems()) {
            for (Action action : item.getActions()) {
                if (withinRange(diagnosticResult.getDiagnostic().getRange(), convertRange(action.getRange()))) {
                    if (action.getActionType() != ActionType.NONE) {
                        String title = "Update token to \'" + action.getName() + "\'";
                        Diagnostic diagnostic = diagnosticResult.getDiagnostic();
                        CodeAction codeAction = buildCodeAction(params, action, diagnostic, title, diagnostic.getRange(), action.getValue());
                        actions.add(Either.forRight(codeAction));
                    }
                }
            }
        }

        return actions;
    }

    private List<Either<Command, CodeAction>> createDeprecatedTokenCodeAction(CodeActionParams params,
                                                                              DiagnosticResult diagnosticResult) {
        List<Either<Command, CodeAction>> actions = Lists.newArrayList();

        for (Item item : diagnosticResult.getItems()) {
            for (Action action : item.getActions()) {
                String type = (action.getFileType().orElse(MARKUP).equals(STYLE)) ? "design token" : "utility class";
                String title = "Remove " + type + " \'" + action.getName() + "\'";
                Diagnostic diagnostic = diagnosticResult.getDiagnostic();
                CodeAction codeAction = buildCodeAction(params, action, diagnostic, title, diagnostic.getRange(), action.getValue() == null ? "" : action.getValue());
                actions.add(Either.forRight(codeAction));
                actions.add(Either.forRight(codeAction));
            }
        }
        return actions;
    }

    private Range convertRange(com.salesforce.slds.shared.models.locations.Range input) {
        Position start = new Position(input.getStart().getLine(), input.getStart().getColumn());
        Position end = new Position(input.getEnd().getLine(), input.getEnd().getColumn());

        return new Range(start, end);
    }

    private boolean withinRange(Range diagnostic, Range cursor) {
        if (diagnostic.getStart().getLine() > cursor.getStart().getLine() ||
                diagnostic.getEnd().getLine() < cursor.getEnd().getLine()) {
            return false;
        }

        if (diagnostic.getStart().getLine() == cursor.getStart().getLine() &&
                diagnostic.getStart().getCharacter() > cursor.getStart().getCharacter()){
            return false;
        }

        return diagnostic.getEnd().getLine() != cursor.getEnd().getLine() ||
                cursor.getEnd().getCharacter() <= diagnostic.getEnd().getCharacter();
    }

    private boolean process(Diagnostic diagnostic, CodeActionParams params) {
        return diagnostic.getSource().contentEquals(Identifier.SOURCE)
                && withinRange(diagnostic.getRange(), params.getRange());
    }
}