/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters.colors;

import com.salesforce.slds.shared.converters.Converter;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.salesforce.slds.shared.RegexPattern.RGB_COLOR_PATTERN;

public class RGBColorType extends ColorType {

    private static final Pattern PATTERN = Pattern.compile(RGB_COLOR_PATTERN);

    @Override
    public Matcher match(Converter.State state) {
        return PATTERN.matcher(state.getInput());
    }

    @Override
    public Converter.State process(Matcher matcher, Converter.State state) {
        String red = matcher.group("red");
        String green = matcher.group("green");
        String blue = matcher.group("blue");

        Color color = new Color(
                Math.min(255, Integer.parseInt(red)),
                Math.min(255, Integer.parseInt(green)),
                Math.min(255, Integer.parseInt(blue)));
        return addColor(matcher, color, state);
    }
}
