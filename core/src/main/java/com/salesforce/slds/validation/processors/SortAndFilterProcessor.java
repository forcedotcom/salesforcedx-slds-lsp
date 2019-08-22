/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.processors;

import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Lazy
public class SortAndFilterProcessor implements Processor {

    @Override
    public List<Recommendation> process(List<Recommendation> recommendations) {
        List<Range> ranges = extractUtilitiesRange(recommendations);

        return recommendations.stream()
                .filter(recommendation -> containItems(recommendation) && filter(recommendation, ranges))
                .map(this::sort)
                .sorted(Recommendation::compareTo)
                .collect(Collectors.toList());
    }

    boolean filter(final Recommendation recommendation, List<Range> ranges) {
        if (recommendation.getStyle() == null) {
            return true;
        }

        Range target = recommendation.getStyle().getRange();
        return ranges.stream().anyMatch(range -> range.within(target)) == false;
    }

    List<Range> extractUtilitiesRange(List<Recommendation> recommendations) {
        return recommendations.stream().filter(recommendation -> recommendation.getRuleSet() != null)
                .map(Recommendation::getItems)
                .flatMap(Set::stream).map(Item::getActions).flatMap(Set::stream)
                .map(Action::getRange)
                .collect(Collectors.toList());
    }

    boolean containItems(Recommendation recommendation) {
        return recommendation.getItems().isEmpty() == false;
    }

    Recommendation sort(Recommendation recommendation) {
        Set<Item> items = new TreeSet<>(Comparator.comparing(Item::getValue));
        items.addAll(recommendation.getItems());

        recommendation.setItems(items);
        return recommendation;
    }
}
