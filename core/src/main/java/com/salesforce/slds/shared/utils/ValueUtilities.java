/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.utils;

import com.salesforce.slds.shared.converters.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ValueUtilities {

    private final List<Converter> converters;

    @Autowired
    public ValueUtilities(List<Converter> converters) {
        this.converters = converters;
    }

    public List<String> possibleValues(String value) {
        Converter.State state = getState(value);

        Set<String> possibleValues = new LinkedHashSet<>();
        possibleValues.addAll(generatePossibleValues(value, state));
        return new ArrayList<>(possibleValues);
    }

    public Converter.State getState(String value) {
        Converter.State state = new Converter.State(value);

        for (Converter converter : converters) {
            state = converter.process(state);
        }

        return state;
    }

    public Set<String> generatePossibleValues(String value, Converter.State state) {
        List<String> possibleValues = new ArrayList<>();

        List<Converter.State.Location> locations =
                state.getValues().keySet().stream()
                        .sorted(Comparator.comparingInt(Converter.State.Location::getStart))
                        .collect(Collectors.toList());

        Converter.State.Location lastLocation = null;

        for (Converter.State.Location location : locations) {
            List<String> newPossibleValues = new ArrayList<>();
            Set<String> tokens = state.getValues().get(location);

            if (possibleValues.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                sb.append(value, 0, location.getStart());

                for (String token : tokens) {
                    newPossibleValues.add(sb.toString() + token);
                }

            } else {

                for (String v : possibleValues) {
                    StringBuilder sb = new StringBuilder();

                    sb.append(v);

                    if (lastLocation != null) {
                        sb.append(value, lastLocation.getEnd(), location.getStart());
                    }

                    for (String token : tokens) {
                        newPossibleValues.add(sb.toString() + token);
                    }
                }
            }

            possibleValues = newPossibleValues;

            lastLocation = location;
        }

        if (lastLocation != null && lastLocation.getEnd() < value.length()) {
            final Converter.State.Location location = lastLocation;
            possibleValues = possibleValues.stream()
                    .map(s -> s + value.substring(location.getEnd()))
                    .collect(Collectors.toList());
        }

        return new LinkedHashSet<>(possibleValues);
    }
}
