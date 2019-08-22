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

import static com.salesforce.slds.shared.RegexPattern.HEX_COLOR_PATTERN;

public class HexColorType extends ColorType {

    private static final Pattern PATTERN = Pattern.compile(HEX_COLOR_PATTERN);

    @Override
    public Matcher match(Converter.State state) {
        return PATTERN.matcher(state.getInput());
    }

    @Override
    public Converter.State process(Matcher matcher, Converter.State state) {
        String hex = matcher.group();

        Color color = Color.decode(hex);
        return addColor(matcher, color, state);
    }
}
