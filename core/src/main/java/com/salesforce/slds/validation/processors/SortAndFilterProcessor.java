/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.processors;

import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
@Lazy
public class SortAndFilterProcessor implements Processor {

    @Override
    public List<Recommendation> process(Context context, Entry entry, List<Recommendation> recommendations) {
        List<Range> ranges = extractUtilitiesRange(recommendations);
        List<Range> recommendationSuppressionRanges = entry.getRecommendationSuppressionRanges();

        return recommendations.stream()
                .filter(recommendation -> containItems(recommendation) && filter(recommendation, ranges))
                .filter(recommendation -> !shouldSkipRecommendation(context,
                        recommendationSuppressionRanges, recommendation))
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

    private boolean shouldSkipRecommendation(Context context,
                                             List<Range> recommendationSuppressionRanges,
                                             Recommendation recommendation) {
        Set<Item> items = recommendation.getItems();
        Input.Type inputType  = recommendation.getInput().getType();

        if (!context.isEnabled(ContextKey.V2_ANNOTATION) && inputType != Input.Type.MARKUP) {
            return false;
        }

        return recommendationSuppressionRanges.stream().anyMatch(range ->
                        items.stream().anyMatch( item -> item.getActions().stream().anyMatch( action ->

            range.within(action.getRange()) || // completely contained withing the ignore range
            (
                    // next line is meant to be ignored and the element indeed starts as the next line
                    range.getStart().getLine() == range.getEnd().getLine() &&
                    range.getStart().getLine() == action.getRange().getStart().getLine()
            )))
        );
    }
}
