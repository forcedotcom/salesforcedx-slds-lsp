/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.utils;

import com.salesforce.slds.shared.utils.ValueUtilities;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.shared.models.core.Style;
import com.salesforce.slds.tokens.registry.TokenRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CSSValidationUtilitiesTests {

    private CSSValidationUtilities utilities;

    @BeforeEach
    public void setup() {
        utilities = new CSSValidationUtilities(
                new ValueUtilities(new ArrayList<>()),
                new ActionUtilities());
    }

    @Test
    public void testExact() {
        DesignToken token = new DesignToken();
        token.setCssProperties(Arrays.asList("font"));

        Style style = Style.builder().property("font").build();

        assertTrue(utilities.containsProperties(style, token));
    }

    @Test
    public void testPrefix() {
        DesignToken token = new DesignToken();
        token.setCssProperties(Arrays.asList("*width"));

        Style style = Style.builder().property("max-width").build();

        assertTrue(utilities.containsProperties(style, token));
    }

    @Test
    public void testPrefixExact() {
        DesignToken token = new DesignToken();
        token.setCssProperties(Arrays.asList("*width"));

        Style style = Style.builder().property("width").build();

        assertTrue(utilities.containsProperties(style, token));
    }

    @Test
    public void testPrefixNeg() {
        DesignToken token = new DesignToken();
        token.setCssProperties(Arrays.asList("width*", "width"));

        Style style = Style.builder().property("max-width").build();

        assertFalse(utilities.containsProperties(style, token));
    }

    @Test
    public void testSuffix() {
        DesignToken token = new DesignToken();
        token.setCssProperties(Arrays.asList("padding*"));

        Style style = Style.builder().property("padding-left").build();

        assertTrue(utilities.containsProperties(style, token));
    }

    @Test
    public void testSuffixExact() {
        DesignToken token = new DesignToken();
        token.setCssProperties(Arrays.asList("padding*"));

        Style style = Style.builder().property("padding").build();

        assertTrue(utilities.containsProperties(style, token));
    }

    @Test
    public void testSuffixNeg() {
        DesignToken token = new DesignToken();
        token.setCssProperties(Arrays.asList("padding", "*padding"));

        Style style = Style.builder().property("padding-left").build();

        assertFalse(utilities.containsProperties(style, token));
    }

    @Test
    public void testFilter() {
        Style style = Style.builder().property("border-bottom").value("12px").build();
        assertTrue(utilities.filter(style, Arrays.asList("border*")));
    }

}
