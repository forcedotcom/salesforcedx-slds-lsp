/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.parsers.markup.MarkupParser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

public class HTMLElementTests {

    @Test
    public void basic() {
        List<HTMLElement> elements = MarkupParser.parse("component.cmp",
                Arrays.asList("<div class=\"slds-happy\"/>"));

        HTMLElement element = elements.get(0);
        Map<String, Range> results = element.getClasses();

        assertThat(results, Matchers.aMapWithSize(1));
        assertThat(results.get("slds-happy"),
                Matchers.is(createRange(0, 12, 0, 22)));
    }

    @Test
    public void classWithExpression() {
        List<HTMLElement> elements = MarkupParser.parse("component.cmp",
                Arrays.asList("<div class=\"{# v.status ? 'slds-happy' : 'sad'}\"/>"));

        HTMLElement element = elements.get(0);
        Map<String, Range> results = element.getClasses();

        assertThat(results, Matchers.aMapWithSize(4));
        assertThat(results.get("{#"),
                Matchers.is(createRange(0, 12, 0, 14)));
        assertThat(results.get("v.status"),
                Matchers.is(createRange(0, 15, 0, 23)));
        assertThat(results.get("slds-happy"),
                Matchers.is(createRange(0, 27, 0, 37)));
        assertThat(results.get("'sad'}"),
                Matchers.is(createRange(0, 41, 0, 47)));
    }

    @Test
    @DisplayName("Element with multiple attributes that use expression")
    public void manyExpression() {
        List<HTMLElement> elements = MarkupParser.parse("component.cmp",
                Arrays.asList("<div type=\"{# v.trigger ? 'jump' : 'hide'}\" " +
                        "class=\"{# v.status ? 'slds-happy' : 'sad'}\"/>"));

        HTMLElement element = elements.get(0);
        Map<String, Range> results = element.getClasses();

        assertThat(results, Matchers.aMapWithSize(4));
        assertThat(results.get("{#"),
                Matchers.is(createRange(0, 51, 0, 53)));
    }

    @Test
    @DisplayName("Child elements with class attribute")
    public void nestedClass() {
        List<HTMLElement> elements = MarkupParser.parse("component.cmp",
                Arrays.asList("<div class=\"{# v.status ? 'slds-happy' : 'sad'}\">" +
                        "<span class=\"slds-happy\"/></div>"));

        HTMLElement element = elements.get(0);
        Map<String, Range> results = element.getClasses();

        assertThat(results.get("slds-happy"),
                Matchers.is(createRange(0, 27, 0, 37)));
    }

    private Range createRange(int startLine, int startColumn, int endLine, int endColumn) {
        return new Range(new Location(startLine, startColumn),
                new Location(endLine, endColumn));
    }
}
