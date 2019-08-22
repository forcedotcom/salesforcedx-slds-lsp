/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters.numbers;

import com.google.common.collect.ImmutableSet;
import com.salesforce.slds.shared.converters.Converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.salesforce.slds.shared.RegexPattern.NUMBER_WITH_UNIT_PATTERN;

public class UnitType extends NumberType {

    private static final Pattern pattern = Pattern.compile(NUMBER_WITH_UNIT_PATTERN);

    private static final ImmutableSet<String> SUPPORTED_UNIT =
            ImmutableSet.of("cm", "mm", "in", "px", "pt", "pc", "em", "ex", "ch", "rem",
                    "vw", "vh", "vmin", "vmax");

    @Override
    public Matcher match(Converter.State state) {
        return pattern.matcher(state.getInput());
    }

    @Override
    public Converter.State process(Matcher matcher, Converter.State state) {
        String sign = matcher.group("sign");
        String number = matcher.group("number");
        String unit = matcher.group("unit");

        if (SUPPORTED_UNIT.contains(unit.toLowerCase())) {
            for (String value : generateNumbers(sign, number, unit)) {
                state = state.addValues(matcher, value);
            }
        }

        return state;
    }
}
