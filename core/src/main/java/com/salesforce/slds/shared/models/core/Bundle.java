/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class Bundle {

    private final List<Entry> entries;

    public Bundle() {
        this(new ArrayList<>());
    }

    public Bundle(Entry ... entries) {
        this.entries = new ArrayList<>();
        for (Entry entry : entries) {
            this.entries.add(entry);
        }
    }

    public Bundle(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Entry> getEntries() {
        return this.entries;
    }

    public List<Input> getInputs() {
        return getInputs(input -> true);
    }

    public List<Input> getInputs(Predicate<Input> filter) {
        return this.entries.stream().map(Entry::getInputs)
                .flatMap(List::stream).filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("entries", entries)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Bundle bundle = (Bundle) o;

        return new EqualsBuilder()
                .append(getEntries(), bundle.getEntries())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getEntries())
                .toHashCode();
    }
}
