/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters;

import com.salesforce.slds.shared.converters.numbers.NumberType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

public class NumberConverterTests {

    private TypeConverters converters = new TypeConverters(NumberType.get());

    @Test
    public void testZero() {
        String value = "0 t(spacingXSmall) 0 t(spacingXSmall)";
        Converter.State state = Converter.State.builder().input(value).build();
        state = converters.process(state);

        assertThat(state.getValues().size(), Matchers.is(2));
    }

    @Test
    public void testRem() {
        Converter.State state = Converter.State.builder().input(".82500 rem").build();

        state = converters.process(state);
        Set<String> values = state.getValues().values().iterator().next();
        assertThat(values, Matchers.containsInAnyOrder("0.825rem", "13.2px"));
    }

    @Test
    public void testPX() {
        Converter.State state = Converter.State.builder().input("13.00000 px").build();

        state = converters.process(state);
        Set<String> values = state.getValues().values().iterator().next();
        assertThat(values, Matchers.containsInAnyOrder("0.812rem", "13px"));
    }

    @Test
    public void testPercent() {
        Converter.State state = Converter.State.builder().input("0.123%").build();

        state = converters.process(state);
        Set<String> values = state.getValues().values().iterator().next();
        assertThat(values, Matchers.containsInAnyOrder("0.123%"));
    }

    @Test
    public void testUnitLess() {
        Converter.State state = Converter.State.builder().input("123.2200").build();

        state = converters.process(state);
        Set<String> values = state.getValues().values().iterator().next();
        assertThat(values, Matchers.containsInAnyOrder("123.22"));
    }

    @Test
    public void testMix() {
        Converter.State state = Converter.State.builder().input("12 16px").build();

        state = converters.process(state);

        Iterator<Set<String>> results = state.getValues().values().iterator();

        Set<String> values = results.next();
        assertThat(values, Matchers.containsInAnyOrder("1rem", "16px"));

        values = results.next();
        assertThat(values, Matchers.containsInAnyOrder("12"));
    }
}
