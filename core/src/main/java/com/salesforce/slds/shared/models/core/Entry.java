/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

import com.google.common.collect.Lists;
import com.salesforce.slds.shared.models.annotations.Annotation;
import com.salesforce.slds.shared.models.annotations.AnnotationScope;
import com.salesforce.slds.shared.models.annotations.AnnotationType;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.override.ComponentOverride;
import com.salesforce.slds.shared.models.recommendation.Recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Entry {

    public enum EntityType {LWC, AURA, OTHER}

    private List<Recommendation> recommendation;
    private List<ComponentOverride> overrides;

    private List<Input> inputs;

    private String path;
    private final List<String> rawContent;

    private String componentName;
    private EntityType entityType;

    private List<Range> recommendationSuppressionRanges;

    private Entry(
                  List<Input> inputs, String path, List<String> rawContent,
                  EntityType entityType, String componentName) {
        this.inputs = inputs;
        this.path = path;
        this.rawContent = rawContent;
        this.entityType = entityType;
        this.componentName = componentName;
    }

    public List<Recommendation> getRecommendation() {
        if (this.recommendation == null) {
            this.recommendation = new ArrayList<>();
        }

        return this.recommendation;
    }

    public void setRecommendation(List<Recommendation> recommendation) {
        this.recommendation = recommendation;
    }

    public List<ComponentOverride> getOverrides() {
        if (this.overrides == null) {
            this.overrides = new ArrayList<>();
        }

        return this.overrides;
    }

    public void setOverrides(List<ComponentOverride> overrides) {
        this.overrides = overrides;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(List<Input> inputs) {
        this.inputs = inputs;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean hasAnnotations() {
        return this.getAnnotations().isEmpty() == false;
    }

    public List<Annotation> getAnnotations() {
        return inputs != null ? inputs.stream().filter(input -> input.getType() == Input.Type.STYLE)
                .filter(input -> input instanceof RuleSet)
                .map(Input::asRuleSet)
                .map(RuleSet::getAnnotations)
                .flatMap(List::stream).collect(Collectors.toList()) : Lists.newArrayList();
    }

    /*
     * Searches through the content of an entry to determine the ranges (if any) where
     * our linting rules should be ignored. Take the following content for example, with
     * the line numbers (zero-based) in the first column followed by the content of that line:
     *
     * 0  a
     * 1  b
     * 2  sldsValidatorIgnore
     * 3  c
     * 4  d
     * 5  sldsValidatorAllow
     * 6  e
     * 7  f
     * 8  sldsValidatorIgnore
     * 9  g
     * 10 h
     * 11 sldsValidatorAllow
     * 12 i
     * 13 sldsValidatorIgnoreNextLine
     * 14 j
     * 15 k
     *
     * In the above example, any content between sldsValidatorIgnore & sldsValidatorAllow (i.e 3-4, 9-10)
     * should be exempt from validation rules. Also the line immediately following
     * sldsValidatorIgnoreNextLine (i.e 14) should also be exempt from validation rules.
     */
    public List<Range> getRecommendationSuppressionRanges() {
        if (this.recommendationSuppressionRanges != null) {
            return this.recommendationSuppressionRanges;
        }

        List<Range> recommendationSuppressionRanges = new ArrayList<>();
        for (int i = 0; rawContent != null && i < rawContent.size(); i++) {
            String lineStr = rawContent.get(i);
            int startColumnIgnore = lineStr.indexOf(AnnotationType.IGNORE.value());
            int startColumnIgnoreNextLine = lineStr.indexOf(AnnotationType.IGNORE_NEXT_LINE.value());
            if (startColumnIgnore >= 0 && startColumnIgnore != startColumnIgnoreNextLine) {
                // we found a sldsValidatorIgnore
                int j = i;
                int endColumn = -1;
                for (; j < rawContent.size(); j++) {
                    int column = rawContent.get(j).indexOf(AnnotationType.ALLOW.value());
                    if (column >= 0) {
                        endColumn = column + AnnotationType.ALLOW.value().length();
                        break;
                    }
                }
                if (endColumn == -1) {
                    // we found a sldsValidatorIgnore with no sldsValidatorAllow after it
                    // so we should exempt the rest of the lines.
                    recommendationSuppressionRanges.add(
                            new Range(
                                    new Location(i, startColumnIgnore),
                                    new Location(Integer.MAX_VALUE, Integer.MAX_VALUE)
                            )
                    );
                    i = rawContent.size(); // skip to the end
                } else {
                    // we found a sldsValidatorIgnore with sldsValidatorAllow after it
                    // so we should exempt the lines in between.
                    recommendationSuppressionRanges.add(
                            new Range(
                                    new Location(i, startColumnIgnore),
                                    new Location(j, endColumn)
                            )
                    );
                    i = j; // skip to the next block
                }
            } else if (startColumnIgnoreNextLine >= 0) {
                // we found a sldsValidatorIgnoreNextLine so we should exempt the next line.
                recommendationSuppressionRanges.add(
                        new Range(
                                new Location(i + 1, 0),
                                new Location(i + 1, Integer.MAX_VALUE)
                        )
                );
                i++; // skip the next line
            }
        }

        /**
         * Special Processing for CSS Annotation
         */
        List<Annotation> annotations = getAnnotations();
        if (annotations.size() > 0) {
            List<Annotation> inlineAnnotations = annotations.stream().filter(annotation ->
                    annotation.getScope() == AnnotationScope.INLINE).collect(Collectors.toList());

            this.recommendationSuppressionRanges = recommendationSuppressionRanges.stream().filter(suppressionRange ->
                !inlineAnnotations.stream().anyMatch(inlineItem ->
                        inlineItem.getRange().getStart().equals(suppressionRange.getStart()))
            ).collect(Collectors.toList());
        } else {
            this.recommendationSuppressionRanges = recommendationSuppressionRanges;
        }

        return this.recommendationSuppressionRanges;
    }

    public List<String> getRawContent() {
        return this.rawContent;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return this.componentName;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public static EntryBuilder builder() {
        return new EntryBuilder();
    }

    public static class EntryBuilder {
        private List<Input> inputs;
        private String path;
        private List<String> rawContent;
        private EntityType entityType;
        private String componentName;

        public EntryBuilder inputs(List<Input> inputs) {
            this.inputs = inputs;
            return this;
        }

        public EntryBuilder path(String path) {
            this.path = path;
            return this;
        }

        public EntryBuilder rawContent(List<String> rawContent) {
            this.rawContent = rawContent;
            return this;
        }

        public EntryBuilder entityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public EntryBuilder componentName(String componentName) {
            this.componentName = componentName;
            return this;
        }

        public Entry build() {
            return new Entry(inputs, path, rawContent, entityType, componentName);
        }
    }
}
