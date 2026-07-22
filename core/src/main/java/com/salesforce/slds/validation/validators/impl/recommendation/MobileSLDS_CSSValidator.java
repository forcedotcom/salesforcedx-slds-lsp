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
import com.salesforce.slds.shared.models.annotations.AnnotationType;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.models.core.*;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.shared.models.recommendation.RelatedInformation;
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

        if (!context.isEnabled(ContextKey.SLDS_MOBILE_VALIDATION)) {
            return recommendations;
        }

        List<Input> inputs = entry.getInputs().stream()
                .filter(input -> input.getType() == Input.Type.STYLE).collect(Collectors.toList());

        for (Input input : inputs) {
            List<Style> styles = input.asRuleSet().getStylesWithAnnotationType();
            recommendations.addAll(styles.stream()
                    .filter(style -> style.validate(context))
                    .map(style -> process(style, context, entry.getRawContent()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        return recommendations;
    }

    /**
     * Process CSS RuleSet
     * @param style
     * @param context
     * @param rawContents
     * @return Recommendation
     */
    private Recommendation process(Style style, Context context, List<String> rawContents) {
        Set<Item> items = provideRecommendations(style, context, rawContents);
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
     * @param context
     * @param rawContents
     * @return
     */
    private Set<Item> provideRecommendations(Style style, Context context, List<String> rawContents) {
        Set<Item> items = new LinkedHashSet<>();
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
                        addActionItemsForSmallTokenFont(value, styleValue, style, rawContents, items);
                    }
                }
            }
        });

        // Check that font size smaller than 14px is not used.
        if (style.getProperty().equals("font-size")) {
            addActionItemsForSmallFonts(styleValue, style, rawContents, items);
        }

        if (style.getProperty().equals("font")) {
            CssFontShortHandUtilities fontShortHandUtilities = new CssFontShortHandUtilities(styleValue);
            String fontSize = fontShortHandUtilities.getFontSize();
            if (fontSize != null) {
                addActionItemsForSmallFonts(fontSize, style, rawContents, items);
            }
        }

        // Check that word wrapping is not explicitly specified.
        if (isTextOverflowEllipsis(style) || isWhiteSpaceNowrap(style)) {
            addActionItemsForWordWrapping(styleValue, style, rawContents, items);
        }

        return items;
    }

    private void addActionItemsForSmallTokenFont(String value, String cssValue, Style style, List<String> rawContents, Set<Item> items) {
        Range range = cssValidationUtilities.getValueSpecificRange(value, style, rawContents);
        addActionItems(cssValue, style, rawContents, items, USE_FONT_SIZE_4_OR_LARGER, range);
    }

    private void addActionItemsForSmallFonts(String cssValue, Style style, List<String> rawContents, Set<Item> items) {
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
            addActionItems(cssValue, style, rawContents, items, USE_FONT_SIZE_14PX_OR_LARGER, range);
        }
    }

    private void addActionItemsForWordWrapping(String cssValue, Style style, List<String> rawContents, Set<Item> items) {
            Range range = cssValidationUtilities.getValueSpecificRange(cssValue, style, rawContents);
            addActionItems(cssValue, style, rawContents, items, AVOID_TRUNCATION, range);
    }

    private void addActionItems(String cssValue, Style style, List<String> rawContents, Set<Item> items, String description, Range range) {
        List<RelatedInformation> fullRangeInfo = new ArrayList<>();
        fullRangeInfo.add(RelatedInformation.builder().range(style.getRange()).build());

        Action action = Action.builder()
                .value(cssValue)
                .range(range)
                .description(description)
                .name(ACTION_NAME)
                .relatedInformation(fullRangeInfo)
                .actionType(ActionType.NONE)
                .fileType(Input.Type.STYLE)
                .build();
        Item item = new Item(cssValue, action);
        items.add(item);
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