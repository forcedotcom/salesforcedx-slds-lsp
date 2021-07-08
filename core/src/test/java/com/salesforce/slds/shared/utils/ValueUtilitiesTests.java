/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.utils;

import com.salesforce.slds.shared.configuration.SharedConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SharedConfiguration.class})
public class ValueUtilitiesTests {

    @Autowired
    private ValueUtilities valueUtilities;

    @Test
    public void testColor() {
        List<String> values = valueUtilities.possibleValues("#005FB2");
        assertThat(values, Matchers.containsInAnyOrder("#005FB2", "#005fb2", "rgb(0, 95, 178)", "hsl(208, 100%, 34.9%)"));
    }

    @Test
    public void testRem() {
        List<String> values = valueUtilities.possibleValues("-0.825rem");
        assertThat(values, Matchers.containsInAnyOrder("-0.825rem", "-13.2px", "-9.9pt", "-82.5%"));
    }

    @Test
    public void testPX() {
        List<String> values = valueUtilities.possibleValues("16px");
        assertThat(values, Matchers.containsInAnyOrder("16px", "1rem", "1em", "100%", "12pt"));
    }

    @Test
    public void testValue() {
        List<String> values = valueUtilities.possibleValues(".850");
        assertThat(values, Matchers.containsInAnyOrder("0.85"));

        values = valueUtilities.possibleValues("1.0em");
        assertThat(values, Matchers.containsInAnyOrder("1em", "16px", "12pt", "100%"));

        values = valueUtilities.possibleValues("t(spacingSmall) 0");
        assertThat(values, Matchers.containsInAnyOrder("spacingSmall 0"));
    }

    @Test
    public void testMix() throws IOException {
        URL resource = ValueUtilitiesTests.class.getResource("/utils/mixResults.txt");
        File f = new File(resource.getFile());
        List<String> allPossibleProperties = Files.readAllLines(f.toPath());

        List<String> values = valueUtilities.possibleValues("16px -25% #005FB2");
        assertThat(values, Matchers.containsInAnyOrder(allPossibleProperties.toArray()));
    }
}
