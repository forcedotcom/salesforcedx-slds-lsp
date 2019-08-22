/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.override;

import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.slds.shared.models.core.RuleSet;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.Item;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Set;

public class ComponentOverride {

    private RuleSet rule;
    private Selector selector;
    private String sldsComponentClass;
    private Action action;

    public String getSldsComponentClass() {
        return sldsComponentClass;
    }

    public void setSldsComponentClass(String sldsComponentClass) {
        this.sldsComponentClass = sldsComponentClass;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public RuleSet getRuleSet() {
        return rule;
    }

    public void setRuleSet(RuleSet rule) {
        this.rule = rule;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Range getRange() {
        String selectorString = selector.toString(false);
        int indexOfComponentClass = selectorString.indexOf(this.sldsComponentClass);

        int indexOfBeginningSpace = selectorString.lastIndexOf(" ", indexOfComponentClass);
        int indexOfEndingSpace = selectorString.indexOf(" ", indexOfComponentClass);

        int startColumn = indexOfBeginningSpace == -1 ? 0 : selector.column() + indexOfBeginningSpace;
        int endColumn = indexOfEndingSpace == -1 ? selectorString.length() : selector.column() - 1 + indexOfEndingSpace;

        Location start = new Location(selector.line() - 1, startColumn);
        Location end = new Location(selector.line() - 1, endColumn);

        return new Range(start, end);
    }

    public static ComponentOverrideBuilder builder() {
        return new ComponentOverrideBuilder();
    }

    public static class ComponentOverrideBuilder {
        private RuleSet rule;
        private Selector selector;
        private String sldsComponentClass;
        private Action action;

        public ComponentOverrideBuilder rule(RuleSet rule) {
            this.rule = rule;
            return this;
        }

        public ComponentOverrideBuilder selector(Selector selector) {
            this.selector = selector;
            return this;
        }

        public ComponentOverrideBuilder sldsComponentClass(String sldsComponentClass) {
            this.sldsComponentClass = sldsComponentClass;
            return this;
        }

        public ComponentOverrideBuilder action(Action action) {
            this.action = action;
            return this;
        }

        public ComponentOverride build() {
            ComponentOverride result = new ComponentOverride();
            result.setRuleSet(rule);
            result.setSelector(selector);
            result.setSldsComponentClass(sldsComponentClass);
            result.setAction(action);
            return result;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("rule", rule)
                .append("selector", selector)
                .append("sldsComponentClass", sldsComponentClass)
                .append("action", action)
                .toString();
    }

}