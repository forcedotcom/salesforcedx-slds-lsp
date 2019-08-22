/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.locations;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.salesforce.slds.shared.models.locations.Location.DEFAULT_LOCATION;
import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class Range implements Comparable<Range> {

    public static final Range EMPTY_RANGE = new Range(DEFAULT_LOCATION, DEFAULT_LOCATION);

    private final Location start;
    private final Location end;

    public Range(Location start, Location end) {
        this.start = start;
        this.end = end;
    }

    public Location getStart() {
        return this.start;
    }

    public Location getEnd() {
        return this.end;
    }

    public boolean within(Range other) {
        if (other.getStart().getLine() < start.getLine() ||
                (start.getLine() == other.getStart().getLine() && start.getColumn() > other.getStart().getColumn())) {
            return false;
        }
        if (end.getLine() < other.getEnd().getLine() ||
                (end.getLine() == other.getEnd().getLine() && end.getColumn() < other.getEnd().getColumn())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        return new EqualsBuilder()
                .append(start, range.start)
                .append(end, range.end)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(start)
                .append(end)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("start", start)
                .append("end", end)
                .toString();
    }

    @Override
    public int compareTo(Range o) {
        if (this.equals(o)) {
            return 0;
        }

        int lineCompare = this.start.getLine() - o.start.getLine();

        if (lineCompare == 0) {
            return this.start.getColumn() - o.start.getColumn();
        }

        return lineCompare;
    }
}