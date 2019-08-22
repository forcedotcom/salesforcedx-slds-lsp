/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.slds.shared.parsers.markup;

import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.locations.Location;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

class TagInformation implements Comparable<TagInformation> {

    enum TagType {INCOMPLETE, OPEN, SELF_CLOSING, CLOSE}

    final Location start;
    final Location end;
    final String tag;
    final TagType type;
    final List<HTMLElement> children = new ArrayList<>();

    TagInformation(Location start, Location end, String tag, TagType type) {
        this.start = start;
        this.end = end;
        this.tag = tag;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TagInformation that = (TagInformation) o;

        return new EqualsBuilder()
                .append(start, that.start)
                .append(end, that.end)
                .append(tag, that.tag)
                .append(type, that.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(start)
                .append(end)
                .append(type)
                .append(tag)
                .toHashCode();
    }

    @Override
    public int compareTo(TagInformation o) {
        int compare = start.getLine() - o.start.getLine();

        if (compare != 0) {
            return compare;
        }

        compare = start.getColumn() - o.start.getColumn();
        if (compare != 0){
            return  compare;
        }

        return 0;
    }
}
