/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters.numbers;

import com.salesforce.slds.shared.RegexPattern;
import com.salesforce.slds.shared.converters.Converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitLessType extends NumberType {

    private static final Pattern pattern  = Pattern.compile(RegexPattern.NUMBER_PATTERN);

    @Override
    public Matcher match(Converter.State state) {
        return pattern.matcher(state.getInput());
    }

    @Override
    public Converter.State process(Matcher matcher, Converter.State state) {
        String sign = matcher.group("sign");
        String number = matcher.group("number");

        String input = state.getInput();

        int length = input.length();
        int numberEnd = matcher.end("number");
        int numberStart = matcher.start("number");

        if ((numberEnd == length || length > numberEnd && input.charAt(numberEnd) == ' ')
            && (numberStart == 0 || sign != null || sign == null && input.charAt(numberStart - 1) == ' ')){
            for (String value : generateNumbers(sign, number)) {
                state = state.addValues(matcher, value);
            }
        }

        return state;
    }
}
