/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators;

import com.salesforce.slds.configuration.SldsConfiguration;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.runners.ValidateRunner;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SldsConfiguration.class)
public class CSSValidationTest {

    @Autowired
    ValidateRunner runner;

    @Test
    public void recommendationCount() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS { margin: 2rem; }")
                .append(System.lineSeparator())
                .append(".THIS .fun {padding: 0; }")
                .append(System.lineSeparator())
                .append(".THIS #div {font-size: 1rem}")
                .append(".THIS .div {padding: t(blah)}")
                .append(".THIS #div {background: t(brandBandColorBackgroundPrimary)}")
                .append(".THIS {background: t(brandBackgroundDark)}");

        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());
        assertThat(groupedRecommendation, Matchers.aMapWithSize(5));
    }

    @Test
    public void compoundValues() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS {")
                .append("    margin: 0 0 12px;")
                .append("}");

        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());

        List<Recommendation> recommendations = groupedRecommendation.get("0 0 12px");
        assertThat(recommendations, Matchers.iterableWithSize(1));

        assertThat(recommendations.get(0).getItems(), Matchers.iterableWithSize(2));
    }

    @Test
    public void staticValue() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS { padding: 1.5rem;}");

        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());
        assertThat(groupedRecommendation.get("1.5rem"), Matchers.iterableWithSize(1));
    }

    @Test
    public void location() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS .gridContainer {width:100%}");

        Entry css = createEntry(CSS_PATH, builder.toString());
        css.setBundle(new Bundle(getComponentEntry()));

        Map<String, List<Recommendation>> groupedRecommendation = process(css);

        assertThat(groupedRecommendation.get(".THIS .gridContainer {width:100%}"), Matchers.iterableWithSize(1));

        Range expectedRange = new Range(new Location(0, 0), new Location(0, 33));
        Recommendation recommendation = groupedRecommendation.get(".THIS .gridContainer {width:100%}").get(0);

        for (Item item : recommendation.getItems()) {
            for (Action action : item.getActions()) {
                assertThat(action.getRange(), Matchers.is(expectedRange));
            }
        }
    }

    @Test
    public void locationForDuplicateItem() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS {")
                .append(" padding: t(spacingXSmall) 0 t(spacingXSmall) 0;")
                .append("}");

        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());

        assertThat(groupedRecommendation.get("t(spacingXSmall) 0 t(spacingXSmall) 0"), Matchers.iterableWithSize(1));
        Recommendation recommendation = groupedRecommendation.get("t(spacingXSmall) 0 t(spacingXSmall) 0").get(0);

        Set<Item> items = recommendation.getItems();
        assertThat(items, Matchers.iterableWithSize(2));

        Map<String, Set<Range>> actionRanges = new HashMap<>();

        for (Item item : items) {
            assertThat(item.getActions().size() % 2, Matchers.is(0));

            actionRanges.put(item.getValue(),
                    item.getActions().stream().map(Action::getRange).collect(Collectors.toSet()));
        }

        assertThat(actionRanges.get("0"),
                Matchers.containsInAnyOrder(
                        new Range(new Location(0,34), new Location(0, 35)),
                        new Range(new Location(0,53), new Location(0, 54))
                ));

        assertThat(actionRanges.get("t(spacingXSmall)"),
                Matchers.containsInAnyOrder(
                        new Range(new Location(0, 17), new Location(0, 33)),
                        new Range(new Location(0,36), new Location(0, 52))
                ));
    }

    @Test
    public void blockAnnotation() {
        StringBuilder builder = new StringBuilder();
        builder.append("/* @sldsValidatorAllow */ .THIS { padding: 0; }")
                .append(System.lineSeparator())
                .append(".THIS thead { display: block; }");

        Entry css = createEntry(CSS_PATH, builder.toString());
        css.setBundle(new Bundle(getComponentEntry()));

        Map<String, List<Recommendation>> groupedRecommendation = process(css);
        assertThat(groupedRecommendation, Matchers.aMapWithSize(1));
    }


    @Test
    public void inlineAnnotation() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS { /* @sldsValidatorAllow */ padding: 0;")
                .append(System.lineSeparator())
                .append("padding: 0; }");

        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());
        assertThat(groupedRecommendation, Matchers.aMapWithSize(1));
    }

    @Test
    public void preserveOriginalValue() {
        StringBuilder builder = new StringBuilder(".THIS { max-width: 240px; }");

        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());
        assertThat(groupedRecommendation.get("240px"), Matchers.iterableWithSize(1));

        Action action = groupedRecommendation.get("240px").get(0).getItems().iterator().next()
                .getActions().iterator().next();
        assertThat(action.getValue(), Matchers.not(Matchers.equalTo("240px")));
    }

    @Test
    public void varTokens() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS {")
                .append(" padding: t(spacingXSmall) 0 t(spacingXSmall) 0;")
                .append(" margin-right: t(spacingMedium);")
                .append(" margin-left: t(spacingLarge);")
                .append("}")
                .append(System.lineSeparator())
                .append(".THIS .classes {")
                .append(" padding: 0 t(spacingMedium) 0 t(spacingMedium);")
                .append("}");

        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());

        assertThat(groupedRecommendation.get("t(spacingXSmall) 0 t(spacingXSmall) 0"), Matchers.iterableWithSize(1));
        assertThat(groupedRecommendation.get("t(spacingMedium)"), Matchers.iterableWithSize(1));
        assertThat(groupedRecommendation.get("t(spacingLarge)"), Matchers.iterableWithSize(1));
        assertThat(groupedRecommendation.get("0 t(spacingMedium) 0 t(spacingMedium)"), Matchers.iterableWithSize(1));
    }

    @Test
    public void utilityClasses() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS thead, .THIS .hideSpinner")
                .append("{")
                .append("    display: none;")
                .append("}")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(".THIS table, .THIS .block")
                .append("{")
                .append("    display: block;")
                .append("}")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(".THIS thead {")
                .append("   min-width: 0;")
                .append("   padding: 0 0.5rem;")
                .append("}");

        Entry cssEntry = createEntry(CSS_PATH, builder.toString());
        cssEntry.setBundle(new Bundle(getComponentEntry()));

        Map<String, List<Recommendation>> groupedRecommendation = process(cssEntry);

        assertThat(cssEntry.getRecommendation(), Matchers.iterableWithSize(4));
        assertThat(groupedRecommendation.get(".THIS thead, .THIS .hideSpinner {display:none}"), Matchers.iterableWithSize(1));
        assertThat(groupedRecommendation.get(".THIS table, .THIS .block {display:block}"), Matchers.iterableWithSize(1));
        assertThat(groupedRecommendation.get(".THIS table, .THIS .block {display:block}"), Matchers.iterableWithSize(1));
        assertThat(groupedRecommendation.get("0"), Matchers.nullValue());
        assertThat(groupedRecommendation.get("0 0.5rem"), Matchers.iterableWithSize(1));
    }

    @Test
    public void utilityClassesWithMultipleDeclaration() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS thead")
                .append("{")
                .append(System.lineSeparator())
                .append("    margin-left: auto;")
                .append(System.lineSeparator())
                .append("    margin-right: auto;")
                .append(System.lineSeparator())
                .append("    display: table;")
                .append("}");

        Entry cssEntry = createEntry(CSS_PATH, builder.toString());
        cssEntry.setBundle(new Bundle(getComponentEntry()));

        Map<String, List<Recommendation>> groupedRecommendation = process(cssEntry);

        assertThat(cssEntry.getRecommendation(), Matchers.iterableWithSize(1));
        Recommendation recommendation = groupedRecommendation.get(".THIS thead {margin-left:auto; margin-right:auto; display:table}").get(0);

        Optional<Item> item = recommendation.getItems().stream()
                .filter(recommendationItem ->
                    recommendationItem.getValue().equalsIgnoreCase("{\"margin-left\":\"auto\",\"margin-right\":\"auto\"}")
                )
                .findFirst();

        assertThat(item.isPresent(), Matchers.is(true));
        assertThat(item.get().getActions(), Matchers.iterableWithSize(2));
    }

    @Test
    public void literalNameClass() {
        StringBuilder builder = new StringBuilder();
        builder.append(".cModalContainer ")
                .append("{")
                .append("    display: none;")
                .append("}");

        Entry cssEntry = createEntry("modalContainer.css", builder.toString());

        StringBuilder component = new StringBuilder();
        component.append("<aura:component>")
                .append(System.lineSeparator())
                .append("<div class=\"cModalContainer__wrapper\"></div>")
                .append(System.lineSeparator())
                .append("</aura:component>");

        cssEntry.setBundle(new Bundle(createEntry("modalContainer.cmp", component.toString())));

        Map<String, List<Recommendation>> groupedRecommendation = process(cssEntry);

        assertThat(groupedRecommendation.get(".cModalContainer {display:none}"), Matchers.iterableWithSize(1));
    }

    @Test
    public void literalComponentReference() {
        StringBuilder builder = new StringBuilder();
        builder.append(".cModalContainer.cModalContainer__wrapper ")
                .append("{")
                .append("    display: none;")
                .append("}");

        Entry cssEntry = createEntry("modalContainer.css", builder.toString());

        StringBuilder component = new StringBuilder();
        component.append("<aura:component>")
                .append(System.lineSeparator())
                .append("<div class=\"cModalContainer cModalContainer__wrapper\"></div>")
                .append(System.lineSeparator())
                .append("</aura:component>");

        cssEntry.setBundle(new Bundle(createEntry("modalContainer.cmp", component.toString())));

        Map<String, List<Recommendation>> groupedRecommendation = process(cssEntry);

        assertThat(groupedRecommendation.get(".cModalContainer.cModalContainer__wrapper {display:none}"), Matchers.iterableWithSize(1));
    }

    @Test
    void designTokenValidator() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS {background: t(fontSizeSmall); color: t(brandPrimary); padding: t(blah) t(bleh);}");
        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());

        Set<Action> deprecateTokenActions =
                extractActions(groupedRecommendation.get("t(fontSizeSmall)"));

        Action deprecatedAction = deprecateTokenActions.iterator().next();
        assertThat(deprecatedAction.getActionType(), Matchers.is(ActionType.REPLACE));
        assertThat(deprecatedAction.getName(), Matchers.is("fontSizeSmall"));
        assertThat(deprecatedAction.getValue(), Matchers.is("t(fontSize2)"));

        Set<Action> invalidTokenActions =
                extractActions(groupedRecommendation.get("t(blah) t(bleh)"));

        Action invalidAction = invalidTokenActions.iterator().next();
        assertThat(invalidAction.getActionType(), Matchers.is(ActionType.REMOVE));
        assertThat(invalidAction.getName(), Matchers.is("blah"));
    }

    @Test
    void deprecatedDesignTokenFromResource() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS {font-size: var(--lwc-fontSizeSmall);}");
        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());

        Set<Action> deprecateTokenActions = extractActions(groupedRecommendation.get("var(--lwc-fontSizeSmall)"));
        Action action = deprecateTokenActions.iterator().next();
        assertThat(action.getActionType(), Matchers.is(ActionType.REPLACE));
        assertThat(action.getName(), Matchers.is("fontSize2"));
    }

    @Test
    void lwcVarToken() {
        StringBuilder builder = new StringBuilder();
        builder.append(".cssClazz {background: var(--lwc-document, #BAAC93) var(--lwc-iText, #BAAC93);}");
        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());

        List<Recommendation> recommendations = groupedRecommendation.get("var(--lwc-document, #BAAC93) var(--lwc-iText, #BAAC93)");

        assertThat(recommendations, Matchers.iterableWithSize(1));
        assertThat(recommendations.get(0).getItems(), Matchers.iterableWithSize(1));
        recommendations.get(0).getItems().forEach(item -> item.getActions().forEach(action ->
                assertThat(action.getRange(),
                        Matchers.is(new Range(new Location(0, 52), new Location(0, 77))))));
    }

    @Test
    void lwcVarTokenWithDuplicate() {
        StringBuilder builder = new StringBuilder();
        builder.append(".cssClazz {border-radius: var(--lwc-borderRadiusMedium, 0.25rem) var(--lwc-borderRadiusMedium, 0.25rem) 0 0;}");
        Map<String, List<Recommendation>> groupedRecommendation = process(builder.toString());

        List<Recommendation> recommendations = groupedRecommendation.get("var(--lwc-borderRadiusMedium, 0.25rem) var(--lwc-borderRadiusMedium, 0.25rem) 0 0");
        assertThat(recommendations, Matchers.iterableWithSize(1));

        Set<Action> actions = extractActions(recommendations);
        assertThat(actions, Matchers.iterableWithSize(2));

        actions.forEach(action -> {
            assertThat(action.getValue(), Matchers.is("spacingNone"));
            assertThat(action.getRange().getStart().getColumn(), Matchers.greaterThan(100));
        });
    }

    private Entry createEntry(String path, String content) {
        return Entry.builder().path(path).rawContent(
                Arrays.asList(StringUtils.delimitedListToStringArray(content, System.lineSeparator()))).build();
    }

    private Map<String, List<Recommendation>> process(String content) {
        return process(createEntry(CSS_PATH, content));
    }

    private Map<String, List<Recommendation>> process(Entry entry) {
        runner.setEntry(entry);
        runner.run();

        List<Recommendation> recommendations = runner.getEntry().getRecommendation();
        return
                recommendations.stream().collect(Collectors.groupingBy(o ->
                        o.getStyle() != null ? o.getStyle().getValue() : o.getRuleSet().toString()
                ));
    }

    private Set<Action> extractActions(List<Recommendation> recommendations) {
        Set<Action> actions = new LinkedHashSet<>();

        recommendations.forEach(recommendation ->
                recommendation.getItems().forEach(item ->
                        actions.addAll(item.getActions())));

        return actions;
    }

    private Entry getComponentEntry() {
        StringBuilder builder = new StringBuilder();
        builder.append("<aura:component>")
                .append(System.lineSeparator())
                .append("<table>")
                .append("    <thead>Header</thead>")
                .append("</table>")
                .append(System.lineSeparator())
                .append("<div class=\"gridContainer\"></div>")
                .append(System.lineSeparator())
                .append("</aura:component>");

        return createEntry(CMP_PATH, builder.toString());

    }

    private static final String CSS_PATH = "test.css";
    private static final String CMP_PATH = "test.cmp";

}