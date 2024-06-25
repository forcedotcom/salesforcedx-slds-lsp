/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.annotations;

import com.salesforce.omakase.ast.Rule;
import com.salesforce.slds.shared.models.core.Style;
import com.salesforce.slds.shared.models.locations.Range;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class Annotation {
    private AnnotationScope scope;
    private AnnotationType type;
    private Style style;
    private Rule rule;
    private Range range;

    public AnnotationScope getScope() {
        return scope;
    }

    public void setScope(AnnotationScope scope) {
        this.scope = scope;
    }

    public AnnotationType getType() {
        return type;
    }

    public void setType(AnnotationType type) {
        this.type = type;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public static AnnotationBuilder builder() {
        return new AnnotationBuilder();
    }

    public static class AnnotationBuilder {
        private AnnotationScope scope;
        private AnnotationType type;
        private Style style;
        private Rule rule;
        private Range range;

        public AnnotationBuilder scope(AnnotationScope scope) {
            this.scope = scope;
            return this;
        }

        public AnnotationBuilder type(AnnotationType type) {
            this.type = type;
            return this;
        }

        public AnnotationBuilder style(Style style) {
            this.style = style;
            return this;
        }

        public AnnotationBuilder range(Range range) {
            this.range = range;
            return this;
        }

        public AnnotationBuilder rule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Annotation build() {
            Annotation result = new Annotation();
            result.setScope(scope);
            result.setType(type);
            result.setRule(rule);
            result.setStyle(style);
            result.setRange(range);

            return result;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("scope", scope)
                .append("type", type)
                .append("rule", rule)
                .append("style", style)
                .append("range", range)
                .toString();
    }

}