/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.models.core.*;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.tokens.registry.TokenRegistry;
import com.salesforce.slds.validation.utils.JavascriptValidationUtilities;
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import com.salesforce.slds.validation.validators.models.ProcessingItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handle Javascript and Markup Invalid Token Only
 * Invalid CSS is processed within @{@link DesignTokenValidator}
 */
@Component
public class InvalidValidator implements RecommendationValidator {

    @Autowired
    JavascriptValidationUtilities javascriptValidationUtilities;

    @Autowired
    TokenRegistry tokenRegistry;

    @Override
    public List<Recommendation> matches(Entry entry, Bundle bundle, Context context) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (context.isEnabled(ContextKey.INVALID)) {
            recommendations.addAll(entry.getInputs().stream()
                    .map(input -> this.process(input, entry.getRawContent()))
                    .flatMap(List::stream)
                    .filter(Objects::nonNull).collect(Collectors.toList()));
        }

        return recommendations;
    }

    protected List<Recommendation> process(Input input, List<String> rawContents) {
        List<Recommendation> result = new ArrayList<>();

        Recommendation recommendation;
        switch (input.getType()) {
            case MARKUP:
                recommendation = process(input.asElement());
                if (recommendation !=null) {
                    result.add(recommendation);
                }
                break;
            case JAVASCRIPT:
                recommendation = process(input.asBlock(), rawContents);
                if (recommendation !=null) {
                    result.add(recommendation);
                }
                break;
        }

        return result;
    }

    /**
     * Process HTML Element
     * @param element
     * @return Recommendation
     */
    private Recommendation process(HTMLElement element) {
        if (element.getContent().hasAttr("class")) {
            final Set<ProcessingItem> tokens = new LinkedHashSet<>();
            element.getClasses().forEach((className, range) ->
                    tokens.add(new ProcessingItem(className, range))
            );

            Set<Item> items = extractInvalidTokens(tokens);
            if (items.isEmpty() == false) {
                Recommendation.RecommendationBuilder builder = Recommendation.builder();
                builder.input(element).items(items);
                return builder.build();
            }
        }

        return null;
    }

    /**
     * Process JS Block
     * @param block
     * @param rawContent
     * @return Recommendation
     */
    private Recommendation process(Block block,  List<String> rawContent) {
        Set<Item> items = extractInvalidTokens(
                javascriptValidationUtilities.getStyleValues(block, rawContent));
        if (items.isEmpty() == false) {
            Recommendation.RecommendationBuilder builder = Recommendation.builder();
            builder.input(block).items(items);
            return builder.build();
        }

        return null;
    }

    public Set<Item> extractInvalidTokens(Set<ProcessingItem> tokens) {
        Map<String, List<ProcessingItem>> filtered = tokens.stream().filter(s -> {
            Matcher iconMatcher = SLDS_ICON.matcher(s.getValue());
            if (iconMatcher.matches()) {
                String type = iconMatcher.group("type");

                if (VALID_ICON_TYPES.contains(type)) {
                    return false;
                }
            }

            return PASS_THROUGH.matcher(s.getValue()).matches();
        }).collect(Collectors.groupingBy(ProcessingItem::getValue));

        return Sets.difference(filtered.keySet(), tokenRegistry.getValidUtilityClasses())
                .stream()
                .map(s -> {
                    List<ProcessingItem> items = filtered.get(s);
                    return items.stream()
                            .map(item -> {
                                Action.ActionBuilder actionBuilder = Action.builder()
                                        .name(item.getValue()).actionType(ActionType.REMOVE);

                                return new Item(StringUtils.collectionToCommaDelimitedString(filtered.keySet()),
                                        actionBuilder.range(item.getRange()).build());
                            })
                            .collect(Collectors.toList());
                })
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }



    static final List<String> VALID_ICON_TYPES = ImmutableList.of("utility", "action", "custom", "doctype", "standard");

    static final Pattern SLDS_ICON = Pattern.compile("slds-icon-(?<type>[a-z]*)-[^\\s,\\[:\\]\\.\";]*");
    static final Pattern PASS_THROUGH = Pattern.compile("^slds.*[^-_]$");
}