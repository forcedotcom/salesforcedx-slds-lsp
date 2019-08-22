/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.parsers;

import com.salesforce.slds.shared.parsers.css.CSSParser;
import com.salesforce.slds.shared.models.core.RuleSet;
import com.salesforce.slds.shared.models.core.Style;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;

public class CSSParserTest {

    private static List<RuleSet> rules;

    @BeforeAll
    public static void setup() throws IOException {
        URL resource = CSSParserTest.class.getResource("/css/location.css");
        File f = new File(resource.getFile());

        rules = CSSParser.parse(Files.readAllLines(f.toPath()));
    }

    @Test
    public void doubleDeclaration() {
        Optional<RuleSet> rule = rules.stream()
                .filter(ruleSet -> ruleSet.toString().contains("doubleDeclaration"))
                .findFirst();

        assertThat(rule.isPresent(), Matchers.is(true));

        RuleSet actual = rule.get();

        assertThat(actual.getRange(),
                Matchers.is(new Range(new Location(5, 0), new Location(8, 1))));

        List<Style> styles = actual.getStyles();
        assertThat(styles, Matchers.hasSize(2));

        assertThat(styles.get(0).getRange(),
                Matchers.is(new Range(new Location(6, 4), new Location(6, 66))));
    }


    @Test
    public void startLineWithEndBracket() {
        Optional<RuleSet> rule = rules.stream()
                .filter(ruleSet -> ruleSet.toString().contains("testing"))
                .findFirst();

        assertThat(rule.isPresent(), Matchers.is(true));

        RuleSet actual = rule.get();

        assertThat(actual.getRange(),
                Matchers.is(new Range(new Location(14, 2), new Location(14, 35))));
    }

    @Test
    public void brackets() {
        Optional<RuleSet> rule = rules.stream()
                .filter(ruleSet -> ruleSet.toString().contains("brackets"))
                .findFirst();

        assertThat(rule.isPresent(), Matchers.is(true));

        RuleSet actual = rule.get();

        assertThat(actual.getRange(),
                Matchers.is(new Range(new Location(12, 0), new Location(14, 1))));
    }
}
