/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.tokens.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class DesignToken {

    @JsonProperty("auraToken")
    private String name;
    private String comment;
    private String value;

    private String category;
    private String type;
    private String specificity;
    private String scope;
    private String release;

    private List<String> cssProperties;

    private TokenStatus status;
    private TokenType tokenType;
    private TokenPriority tokenPriority;

    private String sassToken;
    private String yamlToken;
    private String deprecated;

    public DesignToken() {}

    public DesignToken(String name, String comment, String value, String category,
                       String type, String specificity, String scope, String release,
                       List<String> cssProperties, TokenStatus status, TokenType tokenType,
                       TokenPriority tokenPriority, String deprecated,
                       String sassToken, String yamlToken) {
        this.name = name;
        this.comment = comment;
        this.value = value;
        this.category = category;
        this.type = type;
        this.specificity = specificity;
        this.scope = scope;
        this.release = release;
        this.cssProperties = cssProperties;
        this.status = status;
        this.tokenType = tokenType;
        this.tokenPriority = tokenPriority;
        this.sassToken = sassToken;
        this.yamlToken = yamlToken;
        this.deprecated = deprecated;
    }

    public TokenPriority getTokenPriority(){
        if (tokenPriority == null) {
            return TokenPriority.NORMAL;
        }

        return this.tokenPriority;
    }

    public TokenType getTokenType() {
        if (tokenType == null) {
            return TokenType.TOKEN;
        }

        return this.tokenType;
    }

    public List<String> getCssProperties() {
        if (this.cssProperties == null) {
            this.cssProperties = new ArrayList<>();
        }
        return this.cssProperties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecificity() {
        return specificity;
    }

    public void setSpecificity(String specificity) {
        this.specificity = specificity;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public void setCssProperties(List<String> cssProperties) {
        this.cssProperties = cssProperties;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setTokenPriority(TokenPriority tokenPriority) {
        this.tokenPriority = tokenPriority;
    }

    public String getSassToken() {
        return sassToken;
    }

    public void setSassToken(String sassToken) {
        this.sassToken = sassToken;
    }

    public String getYamlToken() {
        return yamlToken;
    }

    public void setYamlToken(String yamlToken) {
        this.yamlToken = yamlToken;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DesignToken token = (DesignToken) o;

        return new EqualsBuilder()
                .append(getName(), token.getName())
                .append(getValue(), token.getValue())
                .append(getStatus(), token.getStatus())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getValue())
                .append(getStatus())
                .toHashCode();
    }
}
