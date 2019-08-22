/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters.colors;

import com.salesforce.slds.shared.converters.Converter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.salesforce.slds.shared.RegexPattern.RGBA_COLOR_PATTERN;

public class RGBAColorType extends ColorType {

    private static final Pattern PATTERN = Pattern.compile(RGBA_COLOR_PATTERN);

    @Override
    public Matcher match(Converter.State state) {
        return PATTERN.matcher(state.getInput());
    }

    @Override
    public Converter.State process(Matcher matcher, Converter.State state) {
        int red = Integer.parseInt(matcher.group("red"));
        int green = Integer.parseInt(matcher.group("green"));
        int blue = Integer.parseInt(matcher.group("blue"));
        float alpha = Float.parseFloat(matcher.group("alpha"));

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setRoundingMode(RoundingMode.HALF_DOWN);

        StringBuilder value = new StringBuilder("rgba(")
                .append(red).append(", ")
                .append(green).append(", ")
                .append(blue).append(", ")
                .append(decimalFormat.format(alpha)).append(")");

        return state.addValues(matcher, value.toString());
    }
}
