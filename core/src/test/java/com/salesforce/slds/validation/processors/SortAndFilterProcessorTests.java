/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.processors;

import com.salesforce.slds.configuration.SldsConfiguration;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.RuleSet;
import com.salesforce.slds.shared.models.core.Style;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.runners.ValidateRunner;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SldsConfiguration.class)
class SortAndFilterProcessorTests {

    @Autowired
    private ValidateRunner runner;

    Context context = new Context();

    private final Processor processor = new SortAndFilterProcessor();

    @Test
    void sort() {
        Set<Item> items = createItemSet("{padding: 0;}", Action.builder().build());

        Recommendation rec1 = Recommendation.builder()
                .input(RuleSet.builder().content("b {padding: 0;}").build())
                .items(items).build();

        Recommendation rec2 = Recommendation.builder()
                .input(RuleSet.builder().content("a {padding: 0;}").build())
                .items(items).build();

        List<Recommendation> recommendations = new ArrayList<>();
        recommendations.add(rec1);
        recommendations.add(rec2);

        List<Recommendation> results = processor.process(context, Entry.builder().build(), recommendations);
        assertThat(results, Matchers.iterableWithSize(2));
        assertThat(results.get(0).getInput().toString(),
                Matchers.is(RuleSet.builder().content("a {padding: 0;}").build().toString()));
        assertThat(results.get(1).getInput().toString(),
                Matchers.is(RuleSet.builder().content("b {padding: 0;}").build().toString()));
    }

    @Test
    @DisplayName("Filter recommendation that doesn't contain item")
    void filter() {
        Recommendation rec = Recommendation.builder()
                .input(RuleSet.builder().content("a {padding: 0;}").build())
                .items(new LinkedHashSet<>()).build();

        List<Recommendation> recommendations = new ArrayList<>();
        recommendations.add(rec);

        List<Recommendation> results = processor.process(context, Entry.builder().build(), recommendations);
        assertThat(results, Matchers.iterableWithSize(0));
    }

    @Test
    @DisplayName("Simple Ranking when Style Recommendation is within Range of Utility Recommendation")
    void rankingWithinRange() {
        RuleSet ruleSet = RuleSet.builder().content("a {padding: 0;}").build();

        Action utilityAction = Action.builder()
                .range(new Range(new Location(1, 3), new Location(3, 8)))
                .actionType(ActionType.REPLACE).relatedInformation(new LinkedList<>()).build();

        Recommendation rule = Recommendation.builder()
                .input(ruleSet)
                .items(createItemSet("{padding: 0;}", utilityAction)).build();

        Action designAction = Action.builder()
                .actionType(ActionType.REPLACE).build();

        Recommendation style = Recommendation.builder()
                .input(Style.builder().property("padding").value("0").declaration("a").range(
                        new Range(new Location(2, 3), new Location(2, 8))
                ).build())
                .items(createItemSet("{padding: 0;}", designAction)).build();

        List<Recommendation> recommendations = Arrays.asList(rule, style);

        List<Recommendation> results = processor.process(context, Entry.builder().build(), recommendations);
        assertThat(results, Matchers.iterableWithSize(1));
        assertThat(results.get(0).getInput(), Matchers.is(ruleSet));
    }

    @Test
    @DisplayName("Simple Ranking when Style Recommendation is not within Range of Utility Recommendation")
    void rankingNegative() {
        RuleSet ruleSet = RuleSet.builder().content("a {padding: 0;}").build();

        Action utilityAction = Action.builder()
                .range(new Range(new Location(1, 3), new Location(3, 8)))
                .actionType(ActionType.REPLACE).relatedInformation(new LinkedList<>()).build();

        Recommendation rule = Recommendation.builder()
                .input(ruleSet)
                .items(createItemSet("{padding: 0;}", utilityAction)).build();

        Action designAction = Action.builder()
                .actionType(ActionType.REPLACE).build();

        Recommendation style = Recommendation.builder()
                .input(Style.builder().property("margin").value("0")
                        .range(new Range(new Location(1, 0), new Location(1, 2)))
                        .declaration("a").build())
                .items(createItemSet("{padding: 0;}", designAction)).build();

        List<Recommendation> recommendations = Arrays.asList(rule, style);

        List<Recommendation> results = processor.process(context, Entry.builder().build(), recommendations);
        assertThat(results, Matchers.iterableWithSize(2));
        assertThat(results.get(0).getInput(), Matchers.is(ruleSet));
    }

    @Test
    @DisplayName("Filter recommendations that should be suppressed")
    void suppress() throws IOException {
            URL resource = SortAndFilterProcessorTests.class.getResource("/components/mobileComponentExperience3.html");
            File f = new File(resource.getFile());

            Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();

            Bundle bundle = new Bundle(entry);
            runner.setBundle(bundle);

            runner.run();

            List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                    .flatMap(List::stream).collect(Collectors.toList());

            assertThat(recommendations, Matchers.hasSize(1));

            Map<String, List<Range>> results = new LinkedHashMap<>();
            Map<String, String> messages = new HashMap<>();
            recommendations.forEach(recommendation -> {
            recommendation.getItems().stream()
                    .forEach(item -> {
                            for (Action action : item.getActions()) {
                                List<Range> ranges = results.getOrDefault(action.getName(), new ArrayList<>());
                                ranges.add(action.getRange());
                                results.put(action.getName(), ranges);
                                messages.put(action.getName(), action.getDescription());
                        }
                     });
            });

            assertThat(results.get("lightning-datatable"),
                    Matchers.hasItem(new Range(new Location(18, 4), new Location(18, 47))));
    }

    private Set<Item> createItemSet(String value, Action ... actions) {
        Set<Action> actionSet = new LinkedHashSet<>(Arrays.asList(actions));

        Item item = new Item(value);
        item.setActions(actionSet);

        Set<Item> items = new LinkedHashSet<>();
        items.add(item);

        return items;
    }
}
