/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.aggregators;

import com.google.common.base.Equivalence;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class Aggregator {

    public Collector<Recommendation, ?, List<Recommendation>> toList() {
        return Collector.of(() -> new ArrayList<>(), this::accumulate, this::combine);
    }

    protected abstract Equivalence<Recommendation> getEquivalence();

    protected List<Recommendation> combine(List<Recommendation> list1, List<Recommendation> list2) {
        List<Recommendation> results = new ArrayList();

        for (Recommendation recommendation : list2) {
            int indexOf = find(list1, recommendation, getEquivalence());

            if (indexOf != -1) {
                Recommendation current = merge(list1.remove(indexOf), recommendation);
                results.add(current);
            } else {
                Set<Item> items = mergeItems(generateMapping(recommendation));
                recommendation.setItems(items);
                results.add(recommendation);
            }
        }

        results.addAll(list1);

        return results;
    }

    protected void accumulate(List<Recommendation> list, Recommendation recommendation) {
        int indexOf = find(list, recommendation, getEquivalence());

        if (indexOf != -1) {
            Recommendation current = merge(list.remove(indexOf), recommendation);
            list.add(current);
        } else {
            Set<Item> items = mergeItems(generateMapping(recommendation));
            recommendation.setItems(items);
            list.add(recommendation);
        }
    }

    protected int find(List<Recommendation> items, Recommendation target,
                       Equivalence<Recommendation> equivalence) {
        Optional<Recommendation> found = items.stream().filter(i ->
                equivalence.equivalent(i, target)).findFirst();
        return found.isPresent()? items.indexOf(found.get()) : -1;
    }

    protected Map<String, List<Item>> generateMapping(Recommendation recommendation) {
        return recommendation.getItems().stream().collect(Collectors.groupingBy(Item::getValue));
    }

    protected Item merge(Item item, List<Item> additionalItems) {
        Set<Action> actions = new LinkedHashSet<>();
        actions.addAll(item.getActions());

        for (Item rec2Item : additionalItems) {
            actions.addAll(rec2Item.getActions());
        }

        item.setActions(actions);

        return item;
    }

    protected Set<Item> mergeItems(Map<String, List<Item>> mapping) {
        Set<Item> results = new LinkedHashSet();

        mapping.forEach((s, items) -> {
            Item newItem = new Item(s);
            newItem = merge(newItem, items);

            results.add(newItem);
        });

        return results;
    }

    protected Recommendation merge(Recommendation rec1, Recommendation rec2) {
        Map<String, List<Item>> mapping = generateMapping(rec2);

        Set<Item> recItem = new LinkedHashSet();

        for(Item item : rec1.getItems()) {
            if (mapping.containsKey(item.getValue())) {
                List<Item> rec2Items = mapping.remove(item.getValue());
                Item newItem = merge(item, rec2Items);
                recItem.add(newItem);
            } else {
                recItem.add(item);
            }
        }

        if (rec1.getRuleSet() !=null  && rec1.getRuleSet().equals(rec2.getRuleSet()) == false) {
            rec1.getRuleSet().getRule().declarations().appendAll(rec2.getRuleSet().getRule().declarations());
        }

        recItem.addAll(mergeItems(mapping));
        rec1.setItems(recItem);
        return rec1;
    }
}
