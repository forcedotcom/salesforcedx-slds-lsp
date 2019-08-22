/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public abstract class Converter {

    public abstract State process(State state);

    public static class State {

        private final String input;
        private final Map<Location, Set<String>> values = new LinkedHashMap<>();

        public State(String input) {
            this.input = input;
        }

        public String getInput() {
            return input;
        }

        public Map<Location, Set<String>> getValues() {
            List<Location> locations =
                    values.keySet().stream()
                            .sorted(Comparator.comparingInt(Converter.State.Location::getStart))
                            .collect(Collectors.toList());

            Location lastLocation = null;

            for (Location location : locations) {
                if (lastLocation == null) {
                    lastLocation = location;
                } else {
                    if (lastLocation.within(location)) {
                        values.remove(location);
                    } else {
                        lastLocation = location;
                    }
                }
            }

            return values;
        }

        public State addValues(Matcher matcher, String value) {
            Location location = new Location(matcher);

            Set<String> values = this.getValues().getOrDefault(location, new LinkedHashSet<>());
            values.add(value);

            this.getValues().put(location, values);
            return this;
        }

        public static StateBuilder builder() {
            return new StateBuilder();
        }

        public static class StateBuilder {
            private String input;

            public StateBuilder input(String input) {
                this.input = input;
                return this;
            }

            public State build() {
                return new State(input);
            }
        }

        public static class Location {
            private final int start;
            private final int end;

            public static Location create(int start, int end) {
                return new Location(start, end);
            }

            private Location(int start, int end) {
                this.start = start;
                this.end = end;
            }

            private Location(Matcher matcher) {
                this.start = matcher.start();
                this.end = matcher.end();
            }

            boolean within(Location location) {
                return this.getStart() < location.getStart() &&
                        this.getEnd() > location.getEnd();
            }

            public int getStart() {
                return start;
            }

            public int getEnd() {
                return end;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;

                if (o == null || getClass() != o.getClass()) return false;

                Location location = (Location) o;

                return new EqualsBuilder()
                        .append(getStart(), location.getStart())
                        .append(getEnd(), location.getEnd())
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(17, 37)
                        .append(getStart())
                        .append(getEnd())
                        .toHashCode();
            }
        }
    }
}
