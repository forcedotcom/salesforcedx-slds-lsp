/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.models.core.*;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.*;
import com.salesforce.slds.shared.utils.TokenUtilities;
import com.salesforce.slds.tokens.models.UtilityClass;
import com.salesforce.slds.tokens.registry.TokenRegistry;
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import com.salesforce.slds.validation.validators.utils.HTMLElementUtilities;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UtilityClassValidator implements RecommendationValidator, InitializingBean {

    @Autowired
    TokenRegistry tokenRegistry;

    @Autowired
    TokenUtilities tokenUtilities;

    @Autowired
    HTMLElementUtilities utilities;

    private List<UtilityClass> classes;

    @Override
    public void afterPropertiesSet() {
        classes = tokenRegistry.getUtilityClasses().stream()
                .map(this::cleanseName)
                .filter(utilityClass -> utilityClass.getName().contains(" ") == false)
                .filter(utilityClass -> utilityClass.getName().contains("*") == false)
                .collect(Collectors.toList());
    }

    private UtilityClass cleanseName(UtilityClass utilityClass) {
        Set<String> names = StringUtils.commaDelimitedListToSet(utilityClass.getName())
                .stream().map(String::trim).collect(Collectors.toSet());
        List<String> possibleBemNames = names.stream().filter(name -> name.contains("--")).collect(Collectors.toList());

        for (String bemName : possibleBemNames) {
            if (names.contains(bemName.replace("--", "_"))) {
                names.remove(bemName);
            }
        }

        UtilityClass results = new UtilityClass();
        results.setName(StringUtils.collectionToDelimitedString(names, ","));
        results.setSettings(utilityClass.getSettings());

        return results;
    }

    @Override
    public List<Recommendation> matches(Entry entry, Context context) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (context.isEnabled(ContextKey.UTILITY_CLASS)) {
            final List<HTMLElement> elements = entry.getBundle().getInputs().stream()
                    .sequential().map(Input::asElement).filter(Objects::nonNull)
                    .collect(Collectors.toList());

            recommendations.addAll(entry.getInputs().stream()
                    .filter(input -> input.getType() == Input.Type.STYLE)
                    .map(input -> match(entry, input.asRuleSet(), elements))
                    .filter(Objects::nonNull).collect(Collectors.toList()));

        }

        return recommendations;
    }

    Recommendation match(Entry entry, RuleSet ruleSet, List<HTMLElement> elements) {
        /**
         * For ruleSet,
         * - determines which utility classes are applicable
         * - determines which selector have corresponding elements
         * - generates the corresponding action
         *      - if all selectors are consume, remove styles, or rule sets
         *      - if not all selectors are consume, remove selectors
         *      - update elements with corresponding update
         */

        final Map<Selector, List<HTMLElement>> selectedElements = sort(entry, ruleSet, elements);

        Set<Item> items =
                classes.stream()
                        .map(utilityClass -> {
                            List<Style> styles = process(utilityClass, ruleSet);

                            if (styles.isEmpty() || selectedElements.isEmpty()) {
                                return null;
                            }

                            List<RelatedInformation> relatedInformation = convertToRelatedInformation(
                                    utilityClass, selectedElements.values());

                            Action.ActionBuilder builder = Action.builder()
                                    .name(utilityClass.getName())
                                    .value(utilityClass.getName())
                                    .actionType(ActionType.REPLACE)
                                    .relatedInformation(relatedInformation);

                            boolean allSelectorsCovered = selectedElements.size() ==
                                    ruleSet.getRule().selectors().size();
                            boolean allDeclarationCovered = styles.size() ==
                                    ruleSet.getStylesWithAnnotationType().size();

                            Set<Action> actions = new LinkedHashSet<>();

                            if (allSelectorsCovered && allDeclarationCovered) {

                                actions.add(builder.range(ruleSet.getRange()).build());

                            } else if (allSelectorsCovered) {

                                styles.forEach(style ->
                                        actions.add(builder.range(style.getRange()).build())
                                );

                            } else {

                                selectedElements.keySet()
                                        .forEach(selector -> {
                                            Location start = new Location(selector.line() -1 , selector.column() -1);
                                            Location end = new Location(start.getLine(), start.getColumn() +
                                                    selector.toString(false).length());

                                            actions.add(builder.range(new Range(start, end)).build());
                                        });
                            }

                            Item item = new Item(displayAsBlock(styles));
                            item.setActions(actions);

                            return item;

                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        if (items.isEmpty()) {
            return null;
        }

        return Recommendation.builder().input(ruleSet)
                .items(items).build();
    }

    List<RelatedInformation> convertToRelatedInformation(UtilityClass utilityClass,
                                                         Collection<List<HTMLElement>> selectedElements) {
        final List<RelatedInformation> information = new ArrayList<>();

        selectedElements.forEach(elements ->
                elements.forEach(element -> {
                    String classes = utilityClass.getName().replace(".", "");

                    Map<String, Range> classNames = element.getClasses();
                    RelatedInformation.RelatedInformationBuilder builder = RelatedInformation.builder()
                            .path(element.getContent().baseUri())
                            .original("");

                    if (classNames.isEmpty()) {
                        Range tagRange = element.getTagRange();
                        Location location = tagRange.getEnd();

                        builder.range(new Range(location, location))
                                .value(" class=\"" + classes + "\"");
                    } else {
                        List<String> existingClassNames = new ArrayList<>(classNames.keySet());

                        if (existingClassNames.get(0).startsWith("{") &&
                                existingClassNames.get(existingClassNames.size() - 1).endsWith("}")){

                            if (existingClassNames.size() > 1 &&
                                    existingClassNames.get(1).startsWith("\'")) {

                                // {!'clazz' + v.class}
                                Range indexRange = classNames.get(existingClassNames.get(1));
                                builder.range(new Range(indexRange.getEnd(), indexRange.getEnd()))
                                        .value(classes);

                            } else {

                                Optional<String> results = existingClassNames.stream()
                                        .filter(name -> name.contains("!") || name.contains("#"))
                                        .filter(Objects::nonNull)
                                        .findFirst();

                                if (results.isPresent()) {
                                    String ref = results.get();
                                    int index = ref.indexOf("!");

                                    if (index == -1) {
                                        index = ref.indexOf("#");
                                    }

                                    Range refRange = classNames.get(ref);
                                    Location location = new Location(refRange.getStart().getLine(),
                                            refRange.getStart().getColumn() + index  + 1);

                                    builder.range(new Range(location, location))
                                            .value("\'" + classes + "\' + ");
                                }
                            }
                        } else {
                            Range classRange = classNames.get(existingClassNames.get(0));
                            Location location = classRange.getStart();

                            builder.range(new Range(location, location))
                                    .value(classes + " ");
                        }
                    }
                    information.add(builder.build());
                })
        );

        return information;
    }

    private List<Style> process(UtilityClass utilityClass, RuleSet ruleSet) {
        final List<UtilityClass.Setting> utilitySettings = utilityClass.getSettings();

        List<Style> styles = ruleSet.getStylesWithAnnotationType()
                .stream().filter(style -> style.getAnnotationType().validate())
                .filter(style -> utilitySettings.stream().anyMatch(setting -> {
                    String name = style.getProperty();
                    String value = style.getValue();

                    String propertyName = setting.getProperty();

                    if (name.contentEquals(propertyName)) {
                        return tokenUtilities.generatePatterns(setting.getValue())
                                .stream().anyMatch(pattern -> value.matches(pattern));
                    }

                    return false;
                }))
                .collect(Collectors.toList());


        if (utilitySettings.size() !=  styles.size()) {
            return new ArrayList<>();
        }

        return styles;
    }

    protected Map<Selector, List<HTMLElement>> sort(Entry entry, RuleSet ruleSet, List<HTMLElement> elements) {
        Map<Selector, List<HTMLElement>> results = new LinkedHashMap<>();

        for (Selector selector : ruleSet.getRule().selectors()) {
            List<HTMLElement> selectedElements =
                    utilities.select(entry, selector.toString(false), elements);

            if (selectedElements.isEmpty() == false) {
                results.put(selector, selectedElements);
            }
        }

        return results;
    }

    static String displayAsBlock(List<Style> styles) {
        UtilityClass display = new UtilityClass();
        display.setSettings(styles.stream()
                .map(UtilityClassValidator::convert).collect(Collectors.toList()));

        return display.displayAsBlock();
    }

    private static UtilityClass.Setting convert(Style style) {
        UtilityClass.Setting setting = new UtilityClass.Setting();
        setting.setProperty(style.getProperty());
        setting.setValue(style.getValue());
        return setting;
    }


}
