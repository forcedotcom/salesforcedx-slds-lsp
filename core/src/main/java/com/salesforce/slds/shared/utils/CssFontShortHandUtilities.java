/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.utils;

import com.salesforce.slds.shared.RegexPattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CssFontShortHandUtilities {
    private final String FONT_SHORTHAND_PATTERN = "^\\s*" +
            RegexPattern.FONT_STYLE_PATTERN +
            RegexPattern.FONT_VARIANT_PATTERN +
            RegexPattern.FONT_WEIGHT_PATTERN +
            RegexPattern.FONT_SIZE_PATTERN +
            RegexPattern.LINE_HEIGHT_PATTERN +
            "\\s*" +
            RegexPattern.FONT_FAMILY_PATTERN +
            "\\s*";

    private String fontStyle;
    private String fontVariant;
    private String fontWeight;
    private String fontSize;
    private String lineHeight;
    private String fontFamily;

    public CssFontShortHandUtilities(String fontShorthandValue) {
        Pattern pattern = Pattern.compile(FONT_SHORTHAND_PATTERN);
        Matcher matcher = pattern.matcher(fontShorthandValue);
        if (matcher.find()) {
            fontStyle = matcher.group(1);
            fontVariant = matcher.group(2);
            fontWeight = matcher.group(3);
            fontSize = matcher.group(4);
            lineHeight = matcher.group(7);
            fontFamily = matcher.group(8);
        }
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public String getFontVariant() {
        return fontVariant;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public String getFontSize() {
        return fontSize;
    }

    public String getLineHeight()  { return lineHeight; }

    public String getFontFamily() {
        return fontFamily;
    }
}