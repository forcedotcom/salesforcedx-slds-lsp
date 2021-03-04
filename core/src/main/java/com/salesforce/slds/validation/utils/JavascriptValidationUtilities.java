/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.utils;

import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.models.TokenType;
import com.salesforce.slds.shared.models.core.Block;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.validators.models.ProcessingItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class JavascriptValidationUtilities {

    private final ActionUtilities actionUtilities;

    @Autowired
    public JavascriptValidationUtilities(ActionUtilities actionUtilities) {
        this.actionUtilities = actionUtilities;
    }

    public Recommendation match(Block block, List<DesignToken> tokens, List<String> rawContent) {
        Map<String, List<ProcessingItem>> possibleValues = getPossibleValues(block, rawContent);

        if (possibleValues.isEmpty()) {
            return null;
        }

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
                    .input(block)
                    .items(items).build();
        } else {
            return null;
        }
    }

    public Set<ProcessingItem> getStyleValues(Block block, List<String> rawContent) {
        Matcher matcher = SLDS.matcher(block.getValue());

        Set<String> styles = new LinkedHashSet<>();

        while(matcher.find()){
            styles.add(matcher.group());
        }

        return styles.stream().map(s -> {
            int numberOfOccurrences = StringUtils.countOccurrencesOf(block.getValue(), s);
            List<ProcessingItem> processingItems = new ArrayList<>();

            int lineIndex = block.getLineNumber() - 2;
            int column = -1;

            while(numberOfOccurrences > 0) {
                do {
                    if (column == -1) {
                        lineIndex++;
                    }
                    column = rawContent.get(lineIndex).indexOf(s, column);
                } while (column == -1);


                processingItems.add(new ProcessingItem(s, new Range(
                        new Location(lineIndex, column),
                        new Location(lineIndex, column + s.length())
                )));

                numberOfOccurrences--;
                column++;
            }

            return processingItems;
        }).flatMap(List::stream).collect(Collectors.toSet());
    }

    public Map<String, List<ProcessingItem>> getPossibleValues(Block block, List<String> rawContent) {
        Map<String, List<ProcessingItem>> styles = new HashMap<>();

        for (ProcessingItem item : getStyleValues(block, rawContent)) {
            for (String pValue : MarkupValidationUtilities.bemNamingChange(item.getValue())) {
                List<ProcessingItem> items = styles.getOrDefault(pValue, new ArrayList<>());
                items.add(item);

                styles.put(pValue, items);
            }
        }

        return styles;
    }

    private static final Pattern SLDS = Pattern.compile("slds-[^\\s\"\';\\.,:\\)\\[]*");
}
