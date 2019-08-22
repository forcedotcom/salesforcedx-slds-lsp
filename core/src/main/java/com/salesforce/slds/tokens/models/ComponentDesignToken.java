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

public class ComponentDesignToken {

    private String auraTokenName;
    private List<String> cssProperties;
    private List<String> cssSelectors;
    private String sassTokenName;
    private String value;
    private String yamlTokenName;
    private String scope;

    public String getAuraTokenName() {
        return auraTokenName;
    }

    public void setAuraTokenName(String auraTokenName) {
        this.auraTokenName = auraTokenName;
    }

    public List<String> getCssProperties() {
        return cssProperties;
    }

    public void setCssProperties(List<String> cssProperties) {
        this.cssProperties = cssProperties;
    }

    public List<String> getCssSelectors() {
        return cssSelectors;
    }

    public void setCssSelectors(List<String> cssSelectors) {
        this.cssSelectors = cssSelectors;
    }

    public String getSassTokenName() {
        return sassTokenName;
    }

    public void setSassTokenName(String sassTokenName) {
        this.sassTokenName = sassTokenName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getYamlTokenName() {
        return yamlTokenName;
    }

    public void setYamlTokenName(String yamlTokenName) {
        this.yamlTokenName = yamlTokenName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ComponentDesignToken that = (ComponentDesignToken) o;

        return new EqualsBuilder()
                .append(getAuraTokenName(), that.getAuraTokenName())
                .append(getCssProperties(), that.getCssProperties())
                .append(getCssSelectors(), that.getCssSelectors())
                .append(getSassTokenName(), that.getSassTokenName())
                .append(getValue(), that.getValue())
                .append(getYamlTokenName(), that.getYamlTokenName())
                .append(getScope(), that.getScope())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getAuraTokenName())
                .append(getCssProperties())
                .append(getCssSelectors())
                .append(getSassTokenName())
                .append(getValue())
                .append(getYamlTokenName())
                .append(getScope())
                .toHashCode();
    }
}
