/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

import com.google.common.collect.ImmutableList;
import com.salesforce.slds.shared.RegexPattern;
import com.salesforce.slds.shared.converters.Converter;
import com.salesforce.slds.shared.converters.TypeConverters;
import com.salesforce.slds.shared.converters.tokens.VarTokenType;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.core.*;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.shared.utils.CssFontShortHandUtilities;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.registry.TokenRegistry;
import com.salesforce.slds.validation.utils.CSSValidationUtilities;
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MobileSLDS_CSSValidator implements RecommendationValidator {
    public static final String USE_FONT_SIZE_4_OR_LARGER = "For best readability on mobile devices, consider using fontSize4 or larger.";
    public static final String USE_FONT_SIZE_14PX_OR_LARGER = "For best readability on mobile devices, consider using 14px or larger.";
    public static final String AVOID_TRUNCATION = "On a mobile device, a long label can exceed the screen width if it's prevented from wrapping.";

    private final String ACTION_NAME = "Mobile SLDS CSS";
    private final String FONT_SIZE_SLDS_PREFIX = "fontSize";
    private final String PX = "px";

    @Autowired
    CSSValidationUtilities cssValidationUtilities;

    @Autowired
    TokenRegistry tokenRegistry;

    @Override
    public List<Recommendation> matches(Entry entry, Bundle  bundle, Context context) {
        List<Recommendation> recommendations = new ArrayList<>();

        List<Input> inputs = entry.getInputs().stream()
                .filter(input -> input.getType() == Input.Type.STYLE).collect(Collectors.toList());

        for (Input input : inputs) {
            List<Style> styles = input.asRuleSet().getStylesWithAnnotationType();
            recommendations.addAll(styles.stream()
                    .filter(Style::validate)
                    .map(style -> process(style, context, entry.getEntityType(), entry.getRawContent()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        return recommendations;
    }

    /**
     * Process CSS RuleSet
     * @param style
     * @param rawContents
     * @return Recommendation
     */
    private Recommendation process(Style style, Context context, Entry.EntityType entityType, List<String> rawContents) {
        Set<Item> items = provideRecommendations(style, context, entityType, rawContents);
        if (items.isEmpty() == false) {
            Recommendation.RecommendationBuilder builder = Recommendation.builder();
            builder.input(style).items(items);
            return builder.build();
        }
        return null;
    }

    private boolean isWhiteSpaceNowrap(Style style) {
        if (style.getProperty().equals("white-space") &&
                style.getValue().equals("nowrap")) {
            return true;
        }
        return false;
    }

    private boolean isTextOverflowEllipsis(Style style) {
        if (style.getProperty().equals("text-overflow") &&
                style.getValue().equals("ellipsis")) {
            return true;
        }
        return false;
    }

    /**
     * Provide recommendations
     * @param style
     * @param rawContents
     * @return
     */
    private Set<Item> provideRecommendations(Style style, Context context, Entry.EntityType entityType, List<String> rawContents) {
        Set<Item> items = new LinkedHashSet<>();
        Action.ActionBuilder actionBuilder = Action.builder().fileType(Input.Type.STYLE);
        String styleValue = style.getValue();

        // Check that SLDS token font size smaller than 14px(fontSize4) is not used.
        TypeConverters converters = new TypeConverters(ImmutableList.of(new VarTokenType()));
        Converter.State state = converters.process(Converter.State.builder().input(style.getValue()).build());
        state.getValues().forEach((location, values) -> {
            for (String value : values) {
                Optional<DesignToken> designToken = tokenRegistry.getDesignToken(value);

                String fontSizePattern = "fontSize(?<size>[.\\d+])";
                Pattern pattern = Pattern.compile(fontSizePattern);
                Matcher matcher = pattern.matcher(value);

                if (matcher.find() &&
                        designToken.isPresent() == true &&
                        designToken.get().getDeprecated() == null
                ) {
                    String size = matcher.group("size");
                    int fontSize = Integer.parseInt(size);
                    if (fontSize > 0 && fontSize < 4) {
                        Range range = cssValidationUtilities.getValueSpecificRange(value, style, rawContents);
                        actionBuilder.name(ACTION_NAME)
                                .description(USE_FONT_SIZE_4_OR_LARGER)
                                .actionType(ActionType.NONE);
                        items.add(new Item(style.getValue(), actionBuilder.range(range).build()));
                    }
                }
            }
        });

        // Check that font size smaller than 14px is not used.
        if (style.getProperty().equals("font-size")) {
            addActionItemsForSmallFonts(styleValue, style, rawContents, actionBuilder, items);
        }

        if (style.getProperty().equals("font")) {
            CssFontShortHandUtilities fontShortHandUtilities = new CssFontShortHandUtilities(styleValue);
            String fontSize = fontShortHandUtilities.getFontSize();
            if (fontSize != null) {
                addActionItemsForSmallFonts(fontSize, style, rawContents, actionBuilder, items);
            }
        }

        // Check that word wrapping is not explicitly specified.
        if (isTextOverflowEllipsis(style) || isWhiteSpaceNowrap(style)) {
            Range range = cssValidationUtilities.getValueSpecificRange(styleValue, style, rawContents);
            actionBuilder.name(ACTION_NAME)
                    .description(AVOID_TRUNCATION)
                    .actionType(ActionType.NONE);
            items.add(new Item(style.getValue(), actionBuilder.range(range).build()));
        }

        return items;
    }

    private void addActionItemsForSmallFonts(String cssValue, Style style, List<String> rawContents, Action.ActionBuilder actionBuilder, Set<Item> items) {
        Range range;
        String fontAbsoluteSize = "((?:xx?-)?small)";
        Pattern pattern = Pattern.compile(fontAbsoluteSize);
        Matcher matcher = pattern.matcher(cssValue);

        if (matcher.find()) {
            range = cssValidationUtilities.getValueSpecificRange(matcher.group(0), style, rawContents);
        } else {
            range = getRangeForSmallFonts(cssValue, style, rawContents);
        }

        if (!range.equals(Range.EMPTY_RANGE)) {
            actionBuilder.name(ACTION_NAME)
                    .description(USE_FONT_SIZE_14PX_OR_LARGER)
                    .actionType(ActionType.NONE);
            items.add(new Item(style.getValue(), actionBuilder.range(range).build()));
        }
    }
    private Range getRangeForSmallFonts(String cssValue, Style style, List<String> rawContents) {
        Pattern pattern = Pattern.compile(RegexPattern.FONT_SIZE_PATTERN);
        Matcher matcher = pattern.matcher(cssValue);
        Range range = Range.EMPTY_RANGE;
        if (matcher.find()) {
            String value = matcher.group("value");
            String unit = matcher.group("unit");
            if (value != null &&
                    !value.trim().isEmpty() &&
                    unit != null &&
                    !unit.trim().isEmpty()) {
                try {
                    AtomicReference<Double> pxValue = new AtomicReference<>(0.0);
                    if (!unit.contentEquals(PX)) {
                        TypeConverters converters = new TypeConverters();
                        Converter.State state = converters.process(Converter.State.builder().input(style.getValue()).build());
                        state.getValues().forEach((location, valuesWithUnit) -> {
                            for (String valueWithUnit : valuesWithUnit) {
                                if (!valueWithUnit.contains(PX)) {
                                    continue;
                                }
                                String valueWithoutUnit = valueWithUnit.substring(valueWithUnit.length(), valueWithUnit.length() - PX.length());
                                pxValue.set(Double.parseDouble(valueWithoutUnit));
                                break;
                            }
                        });
                    } else {
                        pxValue.set(Double.parseDouble(value));
                    }

                    if (pxValue.get() < 14) {
                        range = cssValidationUtilities.getValueSpecificRange(matcher.group("value"), style, rawContents);
                    }
                } catch(Exception e) {
                    System.out.println(String.format("Failed to convert font size value: %s", e.getMessage()));
                }
            }
        }
        return range;
    }
}