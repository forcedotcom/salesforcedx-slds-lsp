/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.slds.shared.utils;

import com.salesforce.slds.shared.RegexPattern;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.Input;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EntryUtilities {

    private static Pattern IMPORT_AND_EXPORT_PATTERN = Pattern.compile(RegexPattern.IMPORT_AND_EXPORT_TOKENS);

    public static Entry.EntityType getType(Bundle bundle) {
        List<Entry.EntityType> types = bundle.getEntries().stream()
                .map(entry -> {

                    if (entry.getPath().endsWith(".js")) {
                        if (entry.getRawContent().stream()
                                .anyMatch(content -> IMPORT_AND_EXPORT_PATTERN.matcher(content).find())) {
                            return Entry.EntityType.LWC;
                        }
                    }

                    for (Input input : entry.getInputs()) {
                        Optional<Entry.EntityType> type = getType(entry.getComponentName(), input);
                        if (type.isPresent()) {
                            return type.get();
                        }
                    }

                    return Entry.EntityType.OTHER;
                }).filter(entityType -> entityType != Entry.EntityType.OTHER)
                .distinct().collect(Collectors.toList());

        return types.isEmpty() ? Entry.EntityType.OTHER : types.get(0);
    }

    static Optional<Entry.EntityType> getType(String componentName, Input input) {
        switch(input.getType()) {
            case MARKUP:
                String tagName = input.asElement().getContent().tagName();

                if (tagName.matches("^template$")) {
                    return Optional.of(Entry.EntityType.LWC);
                }

                if (tagName.matches("^aura:\\w+$")) {
                    return Optional.of(Entry.EntityType.AURA);
                }
                break;
            case STYLE:
                String[] selectors = input.asRuleSet().getSelectorsAsString().split(",");

                for (String selector : selectors) {
                    String[] selectorArr = selector.split(" ");
                    String firstSelector = selectorArr[0];

                    if (firstSelector.contains(":host") || firstSelector.contains("c-" + componentName)) {
                        return Optional.of(Entry.EntityType.LWC);
                    }

                    if (firstSelector.contains(".THIS") || firstSelector.contains(".c" + componentName)) {
                        return Optional.of(Entry.EntityType.AURA);
                    }
                }
                break;
        }

        return Optional.empty();
    }

    public static String getComponentName(Bundle bundle) {
        List<String> possibleName = bundle.getEntries().stream()
                .map(entry -> {
                    if (entry.getEntityType() != Entry.EntityType.OTHER) {
                        boolean containMarkupOrStyle = entry.getInputs().stream()
                                .anyMatch(input -> input.getType() == Input.Type.MARKUP || input.getType() == Input.Type.STYLE);

                        if (containMarkupOrStyle) {
                            return extractComponentName(entry.getPath());
                        }
                    }

                    return extractComponentName(entry.getPath());
                }).distinct().collect(Collectors.toList());

        return possibleName.get(0);
    }

    private static String extractComponentName(String path) {
        String baseUri = path;

        int fileNameSeparate = baseUri.lastIndexOf(System.getProperty("file.separator"));
        int componentNameSeparate = fileNameSeparate > 0 ?
                baseUri.lastIndexOf(System.getProperty("file.separator"), fileNameSeparate - 1) : -1;

        String componentName;

        if (componentNameSeparate >= 0) {
            componentName = baseUri.substring(componentNameSeparate + 1, fileNameSeparate);
        } else if (fileNameSeparate >= 0){
            componentName = baseUri.substring(fileNameSeparate + 1, baseUri.lastIndexOf("."));
        } else {
            componentName = baseUri.substring(0, baseUri.lastIndexOf("."));
        }

        return componentName;
    }

}
