/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.diagnostics;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.salesforce.slds.lsp.diagnostics.Identifier.DiagnosticCode;
import com.salesforce.slds.lsp.models.DiagnosticBuilder;
import com.salesforce.slds.lsp.models.DiagnosticResult;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.override.ComponentOverride;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.springframework.stereotype.Component;

import static com.salesforce.slds.shared.models.core.Input.Type.MARKUP;
import static com.salesforce.slds.shared.models.core.Input.Type.STYLE;

@Component
public class DiagnosticConverter {

    public List<DiagnosticResult> convert(Entry entry) {
        Set<DiagnosticResult> diagnostics = new LinkedHashSet<>();

        for (Recommendation recommendation : entry.getRecommendation()) {
            diagnostics.addAll(convertRecommendationToDiagnostics(entry.getEntityType(), recommendation));
        }

        for (ComponentOverride componentOverride : entry.getOverrides()) {
            diagnostics.addAll(convertComponentOverrideToDiagnostics(entry.getEntityType(), componentOverride));
        }

        return new ArrayList<>(diagnostics);
    }


    private List<DiagnosticResult> convertRecommendationToDiagnostics(Entry.EntityType entityType, Recommendation recommendation) {
        Set<DiagnosticResult> diagnostics = new LinkedHashSet<>();

        diagnostics.addAll(createInvalidTokenDiagnostics(entityType, recommendation));
        diagnostics.addAll(createUtilityTokenDiagnostics(entityType, recommendation));
        diagnostics.addAll(createDesignTokenDiagnostics(entityType, recommendation));
        diagnostics.addAll(createMobileSLDSDiagnostics(entityType, recommendation));

        return new ArrayList<>(diagnostics);
    }

    private List<DiagnosticResult> createMobileSLDSDiagnostics(Entry.EntityType entityType, Recommendation recommendation) {
        Set<DiagnosticResult> diagnostics = new LinkedHashSet<>();

        DiagnosticBuilder diagnosticBuilder = new DiagnosticBuilder().source(Identifier.SOURCE)
                .severity(DiagnosticSeverity.Warning)
                .code(DiagnosticCode.MOBILE_SLDS.toString());

        for (Item item : recommendation.getItems()) {
            for (Action action : item.getActions()) {
                if (action.getActionType().equals(ActionType.NONE)) {
                    diagnosticBuilder.message(action.getDescription())
                            .start(action.getRange().getStart().getLine(), action.getRange().getStart().getColumn())
                            .end(action.getRange().getEnd().getLine(), action.getRange().getEnd().getColumn());

                    DiagnosticResult diagnosticResult = new DiagnosticResult(diagnosticBuilder.build(), recommendation,
                            null, entityType, Lists.newArrayList(item));
                    diagnostics.add(diagnosticResult);
                }
            }
        }

        return new ArrayList<>(diagnostics);
    }

    private List<DiagnosticResult> convertComponentOverrideToDiagnostics(Entry.EntityType entityType, ComponentOverride override) {

        Set<DiagnosticResult> diagnostics = new LinkedHashSet<>(
                createComponentOverrideDiagnostics(entityType, override));

        return new ArrayList<>(diagnostics);
    }

    private Set<DiagnosticResult> createUtilityTokenDiagnostics(Entry.EntityType entityType, Recommendation recommendation) {

        Set<DiagnosticResult> diagnostics = new LinkedHashSet<>();

        if (recommendation.getRuleSet() == null) {
            return diagnostics;
        }

        Map<Range, Set<Item>> rangeAndItems = new TreeMap<>();
        Map<Range, Set<Action>> rangeWithActions = new TreeMap<>();

        recommendation.getItems().forEach(item -> {
            for (Action action : item.getActions()) {
                if (action.getActionType() != ActionType.REPLACE) {
                    continue;
                }

                Range range = action.getRange();

                Set<Item> items = rangeAndItems.getOrDefault(range, new LinkedHashSet<>());
                items.add(item);
                rangeAndItems.put(range, items);

                Set<Action> actions = rangeWithActions.getOrDefault(range, new LinkedHashSet<>());
                actions.add(action);
                rangeWithActions.put(range, actions);
            }
        });

        DiagnosticBuilder builder = new DiagnosticBuilder()
                .source(Identifier.SOURCE)
                .severity(DiagnosticSeverity.Error)
                .code(DiagnosticCode.UTILITY_TOKENS.toString());

        rangeWithActions.forEach((range, actions) -> {
            int size =  actions.size();
            StringBuilder message = new StringBuilder()
                    .append("Utility Class").append((size > 1 ? "es" : ""))
                    .append(" available").append(System.lineSeparator());

            actions.stream().forEachOrdered(
                    action -> message.append("\"")
                            .append(action.getName().replaceAll("\\.", ""))
                            .append("\"").append(System.lineSeparator()));

            builder.message(message.toString())
                    .start(range.getStart().getLine(), range.getStart().getColumn())
                    .end(range.getEnd().getLine(), range.getEnd().getColumn());

            diagnostics.add(new DiagnosticResult(builder.build(), recommendation, null,
                    entityType,
                    Lists.newArrayList(rangeAndItems.get(range))));
        });

        return diagnostics;
    }

    /**
     * This returns invalid token diagnostics, but can also return alternative tokens in case the deprecated/invalid tokens have alternative tokens
     * @param recommendation
     * @return
     */
    private Set<DiagnosticResult> createInvalidTokenDiagnostics(Entry.EntityType entityType, Recommendation recommendation) {
        Set<DiagnosticResult> diagnostics = new LinkedHashSet<>();

        DiagnosticBuilder invalidBuilder = new DiagnosticBuilder().source(Identifier.SOURCE).severity(DiagnosticSeverity.Error)
                .code(DiagnosticCode.INVALID_TOKENS.toString());

        for (Item item : recommendation.getItems()) {
            for (Action action : item.getActions()) {

                String type = (action.getFileType().orElse(MARKUP).equals(STYLE)) ? "design token" : "utility class";
                ActionType actionType = action.getActionType();
                DiagnosticResult diagnosticResult;

                if (actionType == ActionType.REMOVE) {

                    invalidBuilder.message("Deprecated, or invalid, "+type+": \"" + action.getName() + "\"")
                            .start(action.getRange().getStart().getLine(), action.getRange().getStart().getColumn())
                            .end(action.getRange().getEnd().getLine(), action.getRange().getEnd().getColumn());

                    diagnosticResult = new DiagnosticResult(invalidBuilder.build(), recommendation,
                            null, entityType, Lists.newArrayList(item));
                    diagnostics.add(diagnosticResult);

                }
            }
        }

        return diagnostics;
    }

    private Set<DiagnosticResult> createDesignTokenDiagnostics(Entry.EntityType entityType, Recommendation recommendation) {

        Set<DiagnosticResult> diagnostics = new LinkedHashSet<>();

        if (recommendation.getRuleSet() == null) {
            for (Item item : recommendation.getItems()) {

                Map<Range, List<Action>> actionGroups = item.getActions().stream()
                        .filter(action -> action.getActionType() == ActionType.REPLACE)
                        .collect(Collectors.groupingBy(Action::getRange));

                actionGroups.forEach((range, actions) -> {

                    int size = actions.size();
                    StringBuilder message = new StringBuilder()
                            .append("Updated design token").append((size > 1 ? "s" : ""))
                            .append(" available ").append(System.lineSeparator());

                    actions.stream().forEachOrdered(action -> {
                        message.append("\"").append(action.getName());

                        if (action.getDescription() != null) {
                            message.append("\": ").append(action.getDescription());
                        }

                        message.append(System.lineSeparator());
                    });

                    DiagnosticBuilder builder = new DiagnosticBuilder().source(Identifier.SOURCE)
                            .severity(DiagnosticSeverity.Information).message(message.toString())
                            .code(DiagnosticCode.ALTERNATIVE_TOKENS.toString())
                            .start(range.getStart().getLine(), range.getStart().getColumn())
                            .end(range.getEnd().getLine(), range.getEnd().getColumn());

                    DiagnosticResult diagnosticResult = new DiagnosticResult(builder.build(),
                            recommendation, null, entityType, Lists.newArrayList(item));
                    diagnostics.add(diagnosticResult);
                });
            }
        }
        return diagnostics;
    }

    private Set<DiagnosticResult> createComponentOverrideDiagnostics(Entry.EntityType entityType, ComponentOverride componentOverride) {

        Set<DiagnosticResult> diagnostics = new LinkedHashSet<>();

        DiagnosticBuilder builder = new DiagnosticBuilder().source(Identifier.SOURCE).severity(DiagnosticSeverity.Warning)
                .code(DiagnosticCode.COMPONENT_OVERRIDE.toString());

        Action action = componentOverride.getAction();
        if (action.getActionType() == ActionType.NONE) {
            builder.message("This selector overrides styles from the existing SLDS class \"."+action.getName()+
                    "\". \n\nReplace this class with your own custom class and update the markup accordingly.")
                    .start(action.getRange().getStart().getLine(), action.getRange().getStart().getColumn())
                    .end(action.getRange().getEnd().getLine(), action.getRange().getEnd().getColumn());

            DiagnosticResult diagnosticResult = new DiagnosticResult(builder.build(), null,
                    componentOverride, entityType, null);
            diagnostics.add(diagnosticResult);
        }

        return diagnostics;
    }
}