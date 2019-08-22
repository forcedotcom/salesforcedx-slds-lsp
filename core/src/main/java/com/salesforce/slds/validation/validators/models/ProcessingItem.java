/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.models;

import com.salesforce.slds.shared.models.locations.Range;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class ProcessingItem {

    private final String value;
    private final Range range;

    public ProcessingItem(String value, Range range) {
        this.value = value;
        this.range = range;
    }

    public String getValue() {
        return this.value;
    }

    public Range getRange() {
        return this.range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProcessingItem that = (ProcessingItem) o;

        return new EqualsBuilder()
                .append(getValue(), that.getValue())
                .append(getRange(), that.getRange())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getValue())
                .append(getRange())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("value", value)
                .append("range", range)
                .toString();
    }
}
