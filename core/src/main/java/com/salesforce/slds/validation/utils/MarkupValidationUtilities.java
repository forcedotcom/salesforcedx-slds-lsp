/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.utils;

import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.models.TokenType;
import com.salesforce.slds.validation.validators.models.ProcessingItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MarkupValidationUtilities {

    private final ActionUtilities actionUtilities;

    @Autowired
    public MarkupValidationUtilities(ActionUtilities actionUtilities) {
        this.actionUtilities = actionUtilities;
    }

    public Recommendation match(HTMLElement element, List<DesignToken> tokens) {
        final Map<String, List<ProcessingItem>> possibleValues = getPossibleValue(element);

        Set<Item> items = tokens.stream()
                .filter(token -> token.getTokenType() == TokenType.UTILITY)
                .map(token -> {
                    if (possibleValues.containsKey(token.getValue())) {
                        List<ProcessingItem> processingItems = possibleValues.get(token.getValue());
                        String val = processingItems.get(0).getValue();
                        return actionUtilities.converts(token, val,
                                processingItems.stream().map(ProcessingItem::getRange).collect(Collectors.toList()));
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (items.isEmpty() == false) {
            return Recommendation.builder()
                    .input(element)
                    .items(items).build();
        } else {
            return null;
        }
    }

    public Map<String, List<ProcessingItem>> getPossibleValue(HTMLElement element) {
        final Map<String, List<ProcessingItem>> classes = new HashMap<>();

        element.getClasses().forEach((className, range) -> {
            ProcessingItem item = new ProcessingItem(className, range);

            for (String pValue : bemNamingChange(className)) {
                List<ProcessingItem> processingItems = classes.getOrDefault(pValue, new ArrayList<>());
                processingItems.add(item);
                classes.put(pValue, processingItems);
            }
        });

        return classes;
    }

    public static Set<String> bemNamingChange(String value) {
        Set<String> results = new LinkedHashSet<>();

        results.add(value);
        Matcher matcher = SLDS.matcher(value);

        if (matcher.matches()) {
            results.add(matcher.group("PREFIX") + "_" + matcher.group("SUFFIX"));
        }

        return results;
    }

    static final Pattern SLDS = Pattern.compile("(?<PREFIX>^slds-.*)--(?<SUFFIX>.*$)");
}
