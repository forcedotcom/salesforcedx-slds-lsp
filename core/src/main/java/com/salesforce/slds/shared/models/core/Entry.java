/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

import com.salesforce.slds.shared.models.annotations.Annotation;
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
        return inputs.stream().filter(input -> input.getType() == Input.Type.STYLE)
                .filter(input -> input instanceof RuleSet)
                .map(Input::asRuleSet)
                .map(RuleSet::getAnnotations)
                .flatMap(List::stream).collect(Collectors.toList());
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
