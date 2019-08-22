/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.recommendation;

import com.salesforce.slds.shared.models.core.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class Recommendation extends Object implements Comparable<Recommendation>{

    private Set<Item> items;
    private Input input;

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    public Style getStyle() {
        return input.asStyle();
    }

    public RuleSet getRuleSet() {
        return input.asRuleSet();
    }

    public HTMLElement getElement() {
        return input.asElement();
    }

    public Block getBlock() {
        return input.asBlock();
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Set<Item> getItems() {
        if (items == null) {
            this.items = new LinkedHashSet<>();
        }

        return this.items;
    }

    public Input getInput() {
        return this.input;
    }

    public static RecommendationBuilder builder() {
        return new RecommendationBuilder();
    }

    @Override
    public int compareTo(Recommendation o) {
        if (this.equals(o)) {
            return 0;
        }

        if (this.getRuleSet() != null) {
            if (o.getRuleSet() != null) {
                return this.getRuleSet().compareTo(o.getRuleSet());
            } else {
                return -1;
            }
        }

        if (this.getStyle() != null) {
            if (o.getStyle() != null) {
                return this.getStyle().getProperty().compareTo(o.getStyle().getProperty());
            } else {
                return o.getRuleSet() != null ? 1 : -1;
            }
        }

        if (this.getElement() != null) {
            if (o.getElement() != null) {
                return this.getElement().getContent().tagName()
                        .compareTo(o.getElement().getContent().tagName());
            } else {
                return o.getBlock() != null ? 1 : -1;
            }
        }

        if (this.getBlock() != null) {
            if (o.getBlock() != null) {
                return this.getBlock().getFunctionName().compareTo(o.getBlock().getFunctionName());
            }
        }

        return 1;
    }

    public static class RecommendationBuilder {
        private Set<Item> items;
        private Input input;

        public RecommendationBuilder items(Set<Item> items) {
            this.items = items;
            return this;
        }

        public RecommendationBuilder input(Input input) {
            this.input = input;
            return this;
        }

        public Recommendation build() {
            Recommendation result =  new Recommendation();
            result.setItems(items);
            result.setInput(input);

            return result;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("input", input)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Recommendation that = (Recommendation) o;

        return new EqualsBuilder()
                .append(getStyle(), that.getStyle())
                .append(getElement(), that.getElement())
                .append(getBlock(), that.getBlock())
                .append(getRuleSet(), that.getRuleSet())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getStyle())
                .append(getElement())
                .append(getBlock())
                .append(getRuleSet())
                .toHashCode();
    }


}
