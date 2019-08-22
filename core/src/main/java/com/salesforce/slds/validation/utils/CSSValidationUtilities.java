/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.utils;

import com.salesforce.slds.shared.converters.Converter;
import com.salesforce.slds.shared.models.core.Style;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.shared.utils.ValueUtilities;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.models.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class CSSValidationUtilities {

    private final ValueUtilities valueUtilities;
    private final ActionUtilities actionUtilities;

    @Autowired
    public CSSValidationUtilities(ValueUtilities valueUtilities, ActionUtilities actionUtilities) {
        this.valueUtilities = valueUtilities;
        this.actionUtilities = actionUtilities;
    }

    public Recommendation match(Style style, List<DesignToken> tokens, List<String> rawContent, Boolean hasRange) {
        if (style.validate() == false) {
            return null;
        }

        Set<DesignToken> applicableTokens = getApplicableTokens(style, tokens);

        if (applicableTokens.isEmpty()) {
            return null;
        }

        final String originalValue = style.getValue();
        final Converter.State state = valueUtilities.getState(originalValue);
        Set<String> possibleValues = valueUtilities.generatePossibleValues(originalValue, state);

        Set<Item> result = applicableTokens.stream()
                .map(token -> {

                    List<Converter.State.Location> locations = new ArrayList<>();

                    state.getValues().forEach((tokenLocation, values) -> {
                        Converter.State.Location valueLocation = tokenLocation;

                        if (state.getValues().get(tokenLocation).contains(token.getValue())) {
                            locations.add(valueLocation);
                        }
                    });

                    List<Item> items = new ArrayList<>();

                    if (locations.size() > 0) {

                        int startIndex = rawContent.get(style.getRange().getStart().getLine()).indexOf(originalValue);

                        for (Converter.State.Location location : locations) {

                            Location start = new Location(style.getRange().getStart().getLine(),
                                    startIndex + location.getStart());

                            Location end = new Location(style.getRange().getStart().getLine(),
                                    startIndex + location.getEnd());


                            Range range = hasRange ? style.getRange() : new Range(start, end);

                            items.add(new Item(originalValue.substring(location.getStart(), location.getEnd()),
                                    actionUtilities.converts(token, range)));
                        }

                        return items;
                    }

                    if (possibleValues.contains(token.getValue())) {
                        items.add(new Item(originalValue, actionUtilities.converts(token,
                                getValueSpecificRange(originalValue, style, rawContent))));

                        return items;
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        if (result.isEmpty() == false) {
            return Recommendation.builder()
                    .input(style)
                    .items(result).build();
        } else {
            return null;
        }
    }

    boolean containsProperties(Style style, DesignToken token) {
        return token.getCssProperties().stream()
                .anyMatch(cssProperty -> match(style, cssProperty));
    }

    public boolean filter(Style style, List<String> properties) {
        return  style != null &&
                properties.stream().anyMatch(property -> match(style, property));
    }

    private boolean match(Style style, String value) {
        String regex = value.replaceAll("\\*", "(?:\\.*)?");
        Pattern p = Pattern.compile("^" + regex + "$");
        Matcher matcher = p.matcher(style.getProperty());
        return matcher.find();
    }

    /**
     * getValueSpecificRange()
     * Gets specific location range of value instead of entire line. Helps with more focused text highlighting
     * @param originalValue
     * @param style
     * @param rawContent
     * @return Range
     */

    public Range getValueSpecificRange(String originalValue, Style style, List<String> rawContent){

        Location start = new Location(style.getRange().getStart().getLine(),
                rawContent.get(style.getRange().getStart().getLine()).indexOf(originalValue));
        Location end = new Location(style.getRange().getEnd().getLine(),
                start.getColumn() + originalValue.length());

        return new Range(start, end);
    }

    /**
     * Return design token based on name
     * @param name
     * @param tokens
     * @return
     */
    public DesignToken getDesignTokenByName(String name, List<DesignToken> tokens){

        for (DesignToken token: tokens){
            if(token.getName().equals(name)){
                return token;
            }
        }

        return null;
    }

    public Set<DesignToken> getApplicableTokens(Style style, List<DesignToken> tokens) {

        return tokens.stream()
                .filter(token -> token.getTokenType() == TokenType.TOKEN)
                .filter(token -> containsProperties(style, token))
                .collect(Collectors.toSet());

    }

    public String extractTokenNameFromComment(String comment){
        String[] comments = comment.split(" ");
        for (String c: comments){
            if (c.contains("_")){
                return c;
            }
        }

        return null;
    }

    public String convertTokenName(String token){
        if (token == null) return null;

        String tokenName = Arrays.stream(token.split("\\_"))
                .map(String::toLowerCase)
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining());

        char c[] = tokenName.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        tokenName = new String(c);

        return tokenName;
    }

    public String getTokenNameFromComment(String comment){
        return convertTokenName(extractTokenNameFromComment(comment));
    }
}