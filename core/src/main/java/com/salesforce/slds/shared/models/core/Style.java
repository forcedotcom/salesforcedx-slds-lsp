/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

import com.salesforce.slds.shared.models.annotations.AnnotationType;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.locations.RangeProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class Style extends Input implements RangeProvider {
    final String property;
    final String value;
    final String declaration;
    final String condition;
    final AnnotationType annotationType;
    final Range range;

    private Style(String property, String value, String declaration,
                  String condition, AnnotationType annotationType, Range range) {
        this.property = property;
        this.value = value;
        this.declaration = declaration;
        this.condition = condition;
        this.annotationType = annotationType;
        this.range = range;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }

    public String getDeclaration() {
        return declaration;
    }

    public String getCondition() {
        return condition;
    }

    public AnnotationType getAnnotationType() {return annotationType;}

    public Boolean validate(Context context) {

        if (getAnnotationType() != null) {
            if (context.isEnabled(ContextKey.V2_ANNOTATION)) {
                return getAnnotationType().validate();
            } else {
                return getAnnotationType() != AnnotationType.ALLOW;
            }
        }

        return true;
    }

    @Override
    public Range getRange() {
        return this.range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Style style = (Style) o;

        return new EqualsBuilder()
                .append(getProperty(), style.getProperty())
                .append(getValue(), style.getValue())
                .append(getDeclaration(), style.getDeclaration())
                .append(getCondition(), style.getCondition())
                .append(getAnnotationType(), style.getAnnotationType())
                .append(getRange(), style.getRange())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getProperty())
                .append(getValue())
                .append(getDeclaration())
                .append(getCondition())
                .append(getAnnotationType())
                .append(getRange())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("property", property)
                .append("value", value)
                .append("declaration", declaration)
                .append("condition", condition)
                .append("annotationType", annotationType)
                .append("range", range)
                .toString();
    }

    public static StyleBuilder builder() {
        return new StyleBuilder();
    }

    @Override
    public Type getType() {
        return Type.STYLE;
    }

    public static class StyleBuilder {
        String property;
        String value;
        String declaration;
        String condition;
        AnnotationType annotationType;
        Range range;

        public StyleBuilder annotationType(AnnotationType annotationType) {
            this.annotationType = annotationType;
            return this;
        }

        public StyleBuilder property(String property) {
            this.property = property;
            return this;
        }

        public StyleBuilder value(String value) {
            this.value = value;
            return this;
        }

        public StyleBuilder declaration(String declaration) {
            this.declaration = declaration;
            return this;
        }

        public StyleBuilder condition(String condition) {
            this.condition = condition;
            return this;
        }

        public StyleBuilder range(Range range) {
            this.range = range;
            return this;
        }

        public Style build() {
            return new Style(property, value, declaration, condition, annotationType, range);
        }
    }
}
