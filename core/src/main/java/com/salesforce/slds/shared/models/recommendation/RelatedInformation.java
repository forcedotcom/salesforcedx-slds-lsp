/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.recommendation;

import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.locations.RangeProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class RelatedInformation implements RangeProvider {

    private final String path;
    private final Range range;
    private final String original;
    private final String value;

    RelatedInformation(String path, Range range, String original, String value) {
        this.path = path;
        this.range = range;
        this.original = original;
        this.value = value;
    }

    public String getPath() {
        return this.path;
    }

    @Override
    public Range getRange() {
        return this.range;
    }

    public String getValue() {
        return this.value;
    }

    public String getOriginal() {
        return this.original;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RelatedInformation that = (RelatedInformation) o;

        return new EqualsBuilder()
                .append(getPath(), that.getPath())
                .append(getRange(), that.getRange())
                .append(getOriginal(), that.getOriginal())
                .append(getValue(), that.getValue())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getPath())
                .append(getRange())
                .append(getOriginal())
                .append(getValue())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("path", path)
                .append("range", range)
                .append("original", original)
                .append("value", value)
                .toString();
    }

    public static RelatedInformationBuilder builder() {
        return new RelatedInformationBuilder();
    }

    public static class RelatedInformationBuilder {
        private String path;
        private Range range;
        private String original;
        private String value;

        public RelatedInformationBuilder path(String path) {
            this.path = path;
            return this;
        }

        public RelatedInformationBuilder range(Range range) {
            this.range = range;
            return this;
        }

        public RelatedInformationBuilder original(String original) {
            this.original = original;
            return this;
        }

        public RelatedInformationBuilder value(String value) {
            this.value = value;
            return this;
        }

        public RelatedInformation build() {
            return new RelatedInformation(path, range, original, value);
        }
    }
}
