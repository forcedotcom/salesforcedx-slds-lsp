/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.processors;

import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Lazy
public class SortAndFilterProcessor implements Processor {

    @Override
    public List<Recommendation> process(Entry entry, List<Recommendation> recommendations) {
        List<Range> ranges = extractUtilitiesRange(recommendations);
        List<Range> recommendationSuppressionRanges = entry.getRecommendationSuppressionRanges();

        return recommendations.stream()
                .filter(recommendation -> containItems(recommendation) && filter(recommendation, ranges))
                .filter(recommendation -> !shouldSkipRecommendation(recommendationSuppressionRanges, recommendation))
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

    private boolean shouldSkipRecommendation(List<Range> recommendationSuppressionRanges, Recommendation recommendation) {
        HTMLElement element = recommendation.getElement();

        // For now we only apply the filtering to inputs of type Markup. This is because CSS
        // files are filtered differently (using annotations) at the time when recommendations
        // are being created, and that filtering logic is a bit different. So if we applied the
        // filtering to CSS files here as well, then it would break backwards compatibility.
        // Once we figure out a good way to address backwards compatibility, we should update
        // this here to include all file types using the exact filtering logic used here 
        // in order to keep things consistent.
        if (element == null) {
            return false;
        }

        Range elementRange = element.getRange();
        return recommendationSuppressionRanges.stream().anyMatch(range ->
            range.within(elementRange) || // completely contained withing the ignore range
            (
                    // next line is meant to be ignored and the element indeed starts as the next line
                    range.getStart().getLine() == range.getEnd().getLine() &&
                    range.getStart().getLine() == elementRange.getStart().getLine()
            )
        );
    }
}
