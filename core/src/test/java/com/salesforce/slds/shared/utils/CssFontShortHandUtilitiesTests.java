/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.utils;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class CssFontShortHandUtilitiesTests {

    @Test
    void parseFontShorthand() {
        CssFontShortHandUtilities fontShortHand = new CssFontShortHandUtilities("italic small-caps bold 10px/40px Georgia, serif");
        assertThat(fontShortHand.getFontStyle(), Matchers.is("italic"));
        assertThat(fontShortHand.getFontVariant(), Matchers.is("small-caps"));
        assertThat(fontShortHand.getFontWeight(), Matchers.is("bold"));
        assertThat(fontShortHand.getFontSize(), Matchers.is("10px"));
        assertThat(fontShortHand.getLineHeight(), Matchers.is("40px"));
        assertThat(fontShortHand.getFontFamily(), Matchers.is("Georgia, serif"));
    }

    @Test
    void parseFontDifferentSizeFormats() {
        CssFontShortHandUtilities fontShortHand = new CssFontShortHandUtilities("10px");
        assertThat(fontShortHand.getFontSize(), Matchers.is("10px"));
        assertThat(fontShortHand.getFontVariant(), Matchers.nullValue());
        assertThat(fontShortHand.getFontWeight(), Matchers.nullValue());
        assertThat(fontShortHand.getLineHeight(), Matchers.nullValue());
        assertThat(fontShortHand.getFontFamily(), Matchers.is(""));

        fontShortHand = new CssFontShortHandUtilities("20pt");
        assertThat(fontShortHand.getFontSize(), Matchers.is("20pt"));

        fontShortHand = new CssFontShortHandUtilities("0.8125em");
        assertThat(fontShortHand.getFontSize(), Matchers.is("0.8125em"));

        fontShortHand = new CssFontShortHandUtilities("135%");
        assertThat(fontShortHand.getFontSize(), Matchers.is("135%"));

        fontShortHand = new CssFontShortHandUtilities("211in");
        assertThat(fontShortHand.getFontSize(), Matchers.is("211in"));

        fontShortHand = new CssFontShortHandUtilities("123cm");
        assertThat(fontShortHand.getFontSize(), Matchers.is("123cm"));

        fontShortHand = new CssFontShortHandUtilities("55mm");
        assertThat(fontShortHand.getFontSize(), Matchers.is("55mm"));

        fontShortHand = new CssFontShortHandUtilities("67ex");
        assertThat(fontShortHand.getFontSize(), Matchers.is("67ex"));

        fontShortHand = new CssFontShortHandUtilities("48pc");
        assertThat(fontShortHand.getFontSize(), Matchers.is("48pc"));

        fontShortHand = new CssFontShortHandUtilities("777");
        assertThat(fontShortHand.getFontSize(), Matchers.nullValue());
    }

    @Test
    void parseEmptyValue() {
        CssFontShortHandUtilities fontShortHand = new CssFontShortHandUtilities("");
        assertThat(fontShortHand.getFontSize(), Matchers.nullValue());
        assertThat(fontShortHand.getFontVariant(), Matchers.nullValue());
        assertThat(fontShortHand.getFontWeight(), Matchers.nullValue());
        assertThat(fontShortHand.getLineHeight(), Matchers.nullValue());
        assertThat(fontShortHand.getFontFamily(), Matchers.nullValue());
    }

    @Test
    void parseValueWithNoFontSize() {
        CssFontShortHandUtilities fontShortHand = new CssFontShortHandUtilities("italic small-caps bold Georgia, serif");
        assertThat(fontShortHand.getFontSize(), Matchers.nullValue());
        assertThat(fontShortHand.getFontVariant(), Matchers.nullValue());
        assertThat(fontShortHand.getFontWeight(), Matchers.nullValue());
        assertThat(fontShortHand.getLineHeight(), Matchers.nullValue());
        assertThat(fontShortHand.getFontFamily(), Matchers.nullValue());
    }

    @Test
    void parseValueWithNoFontFamily() {
        CssFontShortHandUtilities fontShortHand = new CssFontShortHandUtilities("italic small-caps bold 10px");
        assertThat(fontShortHand.getFontStyle(), Matchers.is("italic"));
        assertThat(fontShortHand.getFontVariant(), Matchers.is("small-caps"));
        assertThat(fontShortHand.getFontWeight(), Matchers.is("bold"));
        assertThat(fontShortHand.getFontSize(), Matchers.is("10px"));
        assertThat(fontShortHand.getFontFamily(), Matchers.is(""));
    }

    @Test
    void parseValueWithNoFontSizeNoFontFamily() {
        CssFontShortHandUtilities fontShortHand = new CssFontShortHandUtilities("italic small-caps bold");
        assertThat(fontShortHand.getFontSize(), Matchers.nullValue());
        assertThat(fontShortHand.getFontVariant(), Matchers.nullValue());
        assertThat(fontShortHand.getFontWeight(), Matchers.nullValue());
        assertThat(fontShortHand.getLineHeight(), Matchers.nullValue());
        assertThat(fontShortHand.getFontFamily(), Matchers.nullValue());
    }
}