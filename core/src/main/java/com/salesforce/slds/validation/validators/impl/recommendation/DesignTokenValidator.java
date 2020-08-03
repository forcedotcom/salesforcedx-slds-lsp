/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

import com.salesforce.slds.shared.RegexPattern;
import com.salesforce.slds.shared.converters.Converter;
import com.salesforce.slds.shared.converters.TypeConverters;
import com.salesforce.slds.shared.converters.tokens.TokenType;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.core.Style;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.shared.utils.ResourceUtilities;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.validation.utils.CSSValidationUtilities;
import com.salesforce.slds.validation.validators.SLDSValidator;
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import com.salesforce.slds.validation.validators.models.Properties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DesignTokenValidator extends SLDSValidator implements RecommendationValidator, InitializingBean {

    private Pattern VAR_FUNCTION_PATTERN = Pattern.compile(RegexPattern.VAR_FUNCTION, Pattern.CASE_INSENSITIVE);
    private Map<String, String> DEPRECATED_TOKENS_FROM_RESOURCES = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        buildEntries().asTokens().forEach(designToken ->
                DEPRECATED_TOKENS_FROM_RESOURCES.put(designToken.getValue(), designToken.getName())
        );
    }

    @Autowired
    CSSValidationUtilities cssValidationUtilities;

    @Override
    public List<Recommendation> matches(Entry entry, Context context) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (context.isEnabled(ContextKey.DESIGN_TOKEN) && (context.isEnabled(ContextKey.DEPRECATED) ||
                context.isEnabled(ContextKey.INVALID) )) {

            List<Input> inputs = entry.getInputs().stream()
                    .filter(input -> input.getType() == Input.Type.STYLE).collect(Collectors.toList());

            for (Input input : inputs) {
                List<Style> styles = input.asRuleSet().getStyles();

                recommendations.addAll(styles.stream()
                        .map(style -> process(style, context, entry.getEntityType(), entry.getRawContent()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
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

    /**
     * Provide recommendations
     * @param style
     * @param rawContents
     * @return
     */
    private Set<Item> provideRecommendations(Style style, Context context, Entry.EntityType entityType, List<String> rawContents) {
        Set<Item> items = new LinkedHashSet<>();

        TypeConverters converters = new TypeConverters(TokenType.get());
        final Converter.State state = converters.process(Converter.State.builder().input(style.getValue()).build());

        state.getValues().forEach((location, values) -> {
            String originalValue = state.getInput().substring(location.getStart(), location.getEnd());

            for (String value : values) {

                DesignToken designToken = DESIGN_TOKENS.get(value);
                if (designToken == null || designToken.getDeprecated() != null) {
                    Range range = cssValidationUtilities.getValueSpecificRange(originalValue, style, rawContents);
                    Action.ActionBuilder actionBuilder = Action.builder().range(range).fileType(Input.Type.STYLE);
                    Optional<DesignToken> updatedToken = getUpdatedToken(value);

                    Matcher varFunctionMatcher = VAR_FUNCTION_PATTERN.matcher(originalValue);

                    if (varFunctionMatcher.find()) {
                        if (context.isEnabled(ContextKey.DEPRECATED) && updatedToken.isPresent()) {
                            StringBuilder builder = new StringBuilder();
                            builder.append(originalValue, 0, varFunctionMatcher.start("token"));
                            builder.append(updatedToken.get().getName());
                            builder.append(originalValue.substring(varFunctionMatcher.end("token")));

                            actionBuilder.name(updatedToken.get().getName()).value(builder.toString())
                                    .description(updatedToken.get().getComment())
                                    .actionType(ActionType.REPLACE);
                        }

                        if (context.isEnabled(ContextKey.INVALID) && updatedToken.isPresent() == false) {
                            String fallback = varFunctionMatcher.group("fallback");

                            actionBuilder.name(value).value(fallback == null ? null : fallback)
                                    .actionType(ActionType.REMOVE);
                        }

                    } else {

                        if (context.isEnabled(ContextKey.DEPRECATED) && updatedToken.isPresent()) {
                            String updatedValue = entityType == Entry.EntityType.LWC ?
                                    "var(--lwc-"+updatedToken.get().getName()+", " + updatedToken.get().getValue()+")"
                                    : entityType == Entry.EntityType.AURA ? "t(" + updatedToken.get().getName() +")" :
                                    updatedToken.get().getName();

                            actionBuilder.name(updatedToken.get().getName()).value(updatedValue)
                                    .description(updatedToken.get().getComment())
                                    .actionType(ActionType.REPLACE);
                        }

                        if (context.isEnabled(ContextKey.INVALID) && updatedToken.isPresent() == false) {
                            actionBuilder.name(value).actionType(ActionType.REMOVE);
                        }
                    }

                    items.add(new Item(value, actionBuilder.range(range).build()));
                }
            }
        });

        return items;
    }

    private Optional<DesignToken> getUpdatedToken(String token) {
        DesignToken designToken = DESIGN_TOKENS.get(token);
        String updatedToken = null;

        if (designToken != null && designToken.getDeprecated() != null && designToken.getComment() != null) {
            updatedToken = cssValidationUtilities.getTokenNameFromComment(designToken.getComment());
        }

        if (updatedToken == null && DEPRECATED_TOKENS_FROM_RESOURCES.containsKey(token)) {
            updatedToken = DEPRECATED_TOKENS_FROM_RESOURCES.get(token);
        }

        if (updatedToken != null) {
            return Optional.ofNullable(DESIGN_TOKENS.get(updatedToken));
        }

        return Optional.empty();
    }

    Properties buildEntries() {
        Properties properties = new Properties();
        List<Properties.Item> items = new ArrayList<>();
        Yaml yaml = new Yaml();


        ResourceUtilities.getResources(PriorityValidator.class,"/validation/validators/designTokens")
                .forEach(path -> {
                    try (InputStream inputStream = PriorityValidator.class.getResourceAsStream(path)) {
                        Properties prop = yaml.loadAs(inputStream, Properties.class);

                        items.addAll(prop.getItems());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

        properties.setItems(items);

        return properties;
    }
}