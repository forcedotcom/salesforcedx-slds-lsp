/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.aggregators;

import com.google.common.collect.Sets;
import com.salesforce.slds.shared.models.core.Style;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

public class SimpleAggregatorTests {

    private SimpleAggregator aggregator = new SimpleAggregator();

    private Recommendation rec1, rec2, rec3, rec4;
    private Item item1, item2, item3;

    @BeforeEach
    public void setup() {
        Set<Action> actionsForItem1 = new TreeSet<>();
        actionsForItem1.add(Action.builder()
                .name("bcd").value("abc").actionType(ActionType.REPLACE).range(Range.EMPTY_RANGE).build());
        actionsForItem1.add(Action.builder()
                .name("bcd").actionType(ActionType.REMOVE).range(Range.EMPTY_RANGE).build());
        actionsForItem1.add(Action.builder()
                .name("abc").actionType(ActionType.REMOVE).range(Range.EMPTY_RANGE).build());


        Set<Action> actionsForItem2 = new TreeSet<>();
        actionsForItem2.add(Action.builder()
                        .name("abc").value("bcd").actionType(ActionType.REPLACE).range(Range.EMPTY_RANGE).build());
        actionsForItem2.add(Action.builder()
                        .name("abc").actionType(ActionType.REMOVE).range(Range.EMPTY_RANGE).build());
        actionsForItem2.add(Action.builder()
                        .name("efg").actionType(ActionType.REMOVE).range(Range.EMPTY_RANGE).build());
        actionsForItem2.add(Action.builder()
                        .name("bcd").actionType(ActionType.REMOVE).range(Range.EMPTY_RANGE).build());

        item1 = new Item("100px");
        item1.setActions(actionsForItem1);

        item2 = new Item("100px");
        item2.setActions(actionsForItem2);

        item3 = new Item("6.25em",
                Action.builder().name("bcd").value("100px").actionType(ActionType.REPLACE)
                        .range(Range.EMPTY_RANGE).build());

        rec1 = Recommendation.builder()
                .input(Style.builder().property("weight").value("100px").build())
                .items(Sets.newHashSet(item1, item2))
                .build();

        rec2 = Recommendation.builder()
                .input(Style.builder().property("height").value("100px").build())
                .items(Sets.newHashSet(item1))
                .build();

        rec3 = Recommendation.builder()
                .input(Style.builder().property("height").value("100px").build())
                .items(Sets.newHashSet(item2))
                .build();

        rec4 = Recommendation.builder()
                .input(Style.builder().property("height").value("100px").build())
                .items(Sets.newHashSet(item3))
                .build();
    }

    @Test
    public void combineWithoutDuplicate() {
        List<Recommendation> list1 = new ArrayList<>();
        list1.add(rec1);

        List<Recommendation> list2 = new ArrayList<>();
        list2.add(rec2);

        List<Recommendation> results = aggregator.combine(list1, list2);
        assertThat(results, Matchers.iterableWithSize(2));

        Recommendation sortedRec = results.get(0);
        assertThat(sortedRec.getItems(), Matchers.iterableWithSize(1));
        Item item = sortedRec.getItems().iterator().next();

        assertThat(item.getActions(), Matchers.iterableWithSize(3));
        assertThat(item.getActions().stream()
                        .filter(action -> action.getActionType() == ActionType.REPLACE)
                        .map(Action::getName).collect(Collectors.toList()),
                Matchers.containsInAnyOrder("bcd"));
    }

    @Test
    public void combineWithDuplicate() {
        List<Recommendation> list1 = new ArrayList<>();
        list1.add(rec2);

        List<Recommendation> list2 = new ArrayList<>();
        list2.add(rec3);

        List<Recommendation> results = aggregator.combine(list1, list2);
        assertThat(results, Matchers.iterableWithSize(1));

        assertThat(results, Matchers.iterableWithSize(1));
        Recommendation sortedRec = results.get(0);

        assertThat(sortedRec.getItems(), Matchers.iterableWithSize(1));
        Item item = sortedRec.getItems().iterator().next();

        List<String> removeList = new ArrayList<>();
        List<String> replaceList = new ArrayList<>();

        item.getActions().forEach(action -> {
            if (action.getActionType() == ActionType.REMOVE) {
                removeList.add(action.getName());
            } else {
                replaceList.add(action.getName());
            }
        });

        assertThat(replaceList, Matchers.iterableWithSize(2));
        assertThat(replaceList, Matchers.containsInAnyOrder("abc", "bcd"));

        assertThat(removeList, Matchers.iterableWithSize(3));
        assertThat(removeList, Matchers.containsInAnyOrder("abc", "bcd", "efg"));
    }

    @Test
    public void accumulateWithEmptyList() {
        List<Recommendation> results = new ArrayList<>();
        aggregator.accumulate(results, rec1);

        assertThat(results, Matchers.iterableWithSize(1));
        Recommendation sortedRec = results.get(0);

        assertThat(sortedRec.getItems(), Matchers.iterableWithSize(1));
        Item item = sortedRec.getItems().iterator().next();

        List<String> removeList = new ArrayList<>();
        List<String> replaceList = new ArrayList<>();

        item.getActions().forEach(action -> {
            if (action.getActionType() == ActionType.REMOVE) {
                removeList.add(action.getName());
            } else {
                replaceList.add(action.getName());
            }
        });

        assertThat(replaceList, Matchers.iterableWithSize(2));
        assertThat(replaceList, Matchers.containsInAnyOrder("abc", "bcd"));

        assertThat(removeList, Matchers.iterableWithSize(3));
        assertThat(removeList, Matchers.containsInAnyOrder("abc", "bcd", "efg"));
    }

    @Test
    public void accumulateWithDuplicate() {
        List<Recommendation> results = new ArrayList<>();
        results.add(rec2);
        aggregator.accumulate(results, rec3);

        assertThat(results, Matchers.iterableWithSize(1));
        Recommendation sortedRec = results.get(0);

        assertThat(sortedRec.getItems(), Matchers.iterableWithSize(1));
        Item item = sortedRec.getItems().iterator().next();

        List<String> removeList = new ArrayList<>();
        List<String> replaceList = new ArrayList<>();

        item.getActions().forEach(action -> {
            if (action.getActionType() == ActionType.REMOVE) {
                removeList.add(action.getName());
            } else {
                replaceList.add(action.getName());
            }
        });

        assertThat(replaceList, Matchers.iterableWithSize(2));
        assertThat(replaceList, Matchers.containsInAnyOrder("abc", "bcd"));

        assertThat(removeList, Matchers.iterableWithSize(3));
        assertThat(removeList, Matchers.containsInAnyOrder("abc", "bcd", "efg"));
    }

    @Test
    public void accumulateWithOutDuplicate() {
        List<Recommendation> results = new ArrayList<>();
        results.add(rec1);
        aggregator.accumulate(results, rec3);

        assertThat(results, Matchers.iterableWithSize(2));
    }

    @Test
    public void accumulateWithDiffItems() {
        List<Recommendation> results = new ArrayList<>();
        results.add(rec4);
        aggregator.accumulate(results, rec3);

        assertThat(results, Matchers.iterableWithSize(1));
        Recommendation sortedRec = results.get(0);

        assertThat(sortedRec.getItems(), Matchers.iterableWithSize(2));
        Item item = sortedRec.getItems().iterator().next();

        assertThat(item.getActions(), Matchers.iterableWithSize(1));
        assertThat(item.getActions().stream()
                        .filter(action -> action.getActionType() == ActionType.REPLACE)
                        .map(Action::getName).collect(Collectors.toList()),
                        Matchers.containsInAnyOrder("bcd"));

        assertThat(item.getActions().stream()
                .filter(action -> action.getActionType() == ActionType.REMOVE)
                .count(), Matchers.is(0L));
    }
}
