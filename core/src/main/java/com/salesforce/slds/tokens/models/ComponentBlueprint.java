/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.tokens.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ComponentBlueprint {

    private String description;
    private Map<String, String> annotations;

    private String id;
    private Object docPath;
    private List<String> selectors;

    private List<ComponentBlueprint> restrictees;

    private Map<String, ComponentDesignToken> tokens;

    public Map<String, ComponentDesignToken> getTokens() {
        if (tokens == null) {
            tokens = new TreeMap<>();
        }

        return tokens;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, String> annotations) {
        this.annotations = annotations;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getDocPath() {
        return docPath;
    }

    public void setDocPath(Object docPath) {
        this.docPath = docPath;
    }

    public List<String> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<String> selectors) {
        this.selectors = selectors;
    }

    public List<ComponentBlueprint> getRestrictees() {
        return restrictees;
    }

    public void setRestrictees(List<ComponentBlueprint> restrictees) {
        this.restrictees = restrictees;
    }

    public void setTokens(Map<String, ComponentDesignToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ComponentBlueprint that = (ComponentBlueprint) o;

        return new EqualsBuilder()
                .append(getDescription(), that.getDescription())
                .append(getAnnotations(), that.getAnnotations())
                .append(getId(), that.getId())
                .append(getDocPath(), that.getDocPath())
                .append(getSelectors(), that.getSelectors())
                .append(getRestrictees(), that.getRestrictees())
                .append(getTokens(), that.getTokens())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getDescription())
                .append(getAnnotations())
                .append(getId())
                .append(getDocPath())
                .append(getSelectors())
                .append(getRestrictees())
                .append(getTokens())
                .toHashCode();
    }
}
