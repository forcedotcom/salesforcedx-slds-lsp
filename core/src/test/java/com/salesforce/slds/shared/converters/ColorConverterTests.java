/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters;

import com.salesforce.slds.shared.converters.colors.ColorType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

public class ColorConverterTests {

    private TypeConverters converters = new TypeConverters(ColorType.get());

    private static final String[] EXPECTED_VALUES = {"#c81e1e", "#C81E1E", "rgb(200, 30, 30)", "hsl(0, 73.91%, 45.1%)"};

    private static final String[] AQUA_EXPECTED_VALUES = {"#00ffff", "#00FFFF", "rgb(0, 255, 255)", "hsl(180, 100%, 50%)"};

    @Test
    public void testRGB() {
        Converter.State state = Converter.State.builder().input("rgb(200,   30, 30)").build();

        state = converters.process(state);
        Set<String> values = state.getValues().values().iterator().next();
        assertThat(values, Matchers.containsInAnyOrder(EXPECTED_VALUES));
    }

    @Test
    public void testRGBA() {
        Converter.State state = Converter.State.builder().input("rgba( 200, 23, 12, .3 )").build();

        state = converters.process(state);
        Set<String> values = state.getValues().values().iterator().next();
        assertThat(values, Matchers.containsInAnyOrder("rgba(200, 23, 12, 0.3)"));
    }

    @Test
    public void testNamedColor() {
        Converter.State state = Converter.State.builder().input("aqua").build();

        state = converters.process(state);
        Set<String> values = state.getValues().values().iterator().next();
        assertThat(values, Matchers.containsInAnyOrder(AQUA_EXPECTED_VALUES));
    }

    @Test
    public void testHSL() {
        Converter.State state = Converter.State.builder().input("hsl( 180,  100.0%, 50% )").build();

        state = converters.process(state);
        Set<String> values = state.getValues().values().iterator().next();
        assertThat(values, Matchers.containsInAnyOrder(AQUA_EXPECTED_VALUES));
    }

    @Test
    public void testMix() {
        Converter.State state = Converter.State.builder().input("rgb(200,   30, 30) aqua").build();

        state = converters.process(state);
        Iterator<Set<String>> actual = state.getValues().values().iterator();

        Set<String> values = actual.next();
        assertThat(values, Matchers.containsInAnyOrder(EXPECTED_VALUES));

        values = actual.next();
        assertThat(values, Matchers.containsInAnyOrder(AQUA_EXPECTED_VALUES));
    }
}
