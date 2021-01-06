/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.parsers;

import com.google.common.collect.Lists;
import com.salesforce.slds.shared.parsers.markup.MarkupParser;
import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.validation.validators.MarkupValidationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

class MarkupParserTest {

    @Nested
    class TagLocation {

        private List<HTMLElement> elements = new ArrayList<>();

        @BeforeEach
        void setup() throws IOException {
            URL resource = MarkupValidationTest.class.getResource("/components/location.cmp");
            File f = new File(resource.getFile());

            elements = MarkupParser.parse(f.getPath(), Files.readAllLines(f.toPath()));
        }

        @Test
        void testCount() {
            assertThat(elements.size(), Matchers.is(15));
        }

        @Test
        void testTwoElementsOnSameRow() {
            List<HTMLElement> result = elements.stream()
                    .filter(htmlElement -> htmlElement.getRange().getStart().getLine() == 8)
                    .sorted(HTMLElement::compareTo)
                    .collect(Collectors.toList());

            assertThat(result.size(), Matchers.is(2));
            assertThat(result.get(0).getContent().text(), Matchers.is("Mood"));
            assertThat(result.get(0).getRange(),
                    Matchers.is(new Range(new Location(8, 12), new Location(8, 29))));

            assertThat(result.get(1).getContent().text(), Matchers.is("Happy"));
            assertThat(result.get(1).getRange(),
                    Matchers.is(new Range(new Location(8, 29), new Location(8, 47))));
        }

        @Test
        void testLessThanSymbol() {
            List<HTMLElement> result = elements.stream()
                    .filter(htmlElement -> htmlElement.getRange().getStart().getLine() == 10)
                    .sorted(HTMLElement::compareTo)
                    .collect(Collectors.toList());

            assertThat(result.size(), Matchers.is(1));
            assertThat(result.get(0).getContent().attr("isTrue"), Matchers.is("{!v.happy < 5}"));
            assertThat(result.get(0).getRange(),
                    Matchers.is(new Range(new Location(10, 16), new Location(12, 26))));
        }

        @Test
        void testGreaterThanSymbol() {
            List<HTMLElement> result = elements.stream()
                    .filter(htmlElement -> htmlElement.getRange().getStart().getLine() == 7)
                    .sorted(HTMLElement::compareTo)
                    .collect(Collectors.toList());

            assertThat(result.size(), Matchers.is(1));
            assertThat(result.get(0).getContent().attr("isTrue"), Matchers.is("{!v.happy > 6}"));
            assertThat(result.get(0).getRange(),
                    Matchers.is(new Range(new Location(7, 8), new Location(14, 18))));
        }

        @Test
        void testTable() {
            List<HTMLElement> tds = elements.stream()
                    .filter(htmlElement -> htmlElement.getContent().tagName().contentEquals("td"))
                    .collect(Collectors.toList());
            assertThat(tds, Matchers.iterableWithSize(2));
            List<HTMLElement> trs = elements.stream()
                    .filter(htmlElement -> htmlElement.getContent().tagName().contentEquals("tr"))
                    .collect(Collectors.toList());
            assertThat(trs, Matchers.iterableWithSize(1));
        }
    }

    @Nested
    class Markup {

        @Test
        void basic() throws IOException {
            URL resource = MarkupValidationTest.class.getResource("/components/lwc.html");
            File f = new File(resource.getFile());
            List<HTMLElement> elements = MarkupParser.parse(f.getPath(), Files.readAllLines(f.toPath()));
            assertThat(elements.size(), Matchers.is(14));
        }

        @Test
        void oddTags() {
            List<String> html = Lists.newArrayList("<template>", "<img>", "</br>", "</template>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(3));
        }

        @Test
        void comments() {
            List<String> html = Lists.newArrayList("<template>", "<!-- <img/> -->", "</template>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(1));

            assertThat(elements.get(0).getContent().tagName(), Matchers.is("template"));
        }

        @Test
        void hrTag() {
            List<String> html = Lists.newArrayList("<template>", "<hr>", "</template>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(2));
        }

        @Test
        void brTag() {
            List<String> html = Lists.newArrayList("<template>", "</br>", "</template>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(2));
        }

        @Test
        void doubleQuote() {

            List<String> html = Lists.newArrayList(
                    " <div class='who'>",
                    "<aura:if isTrue=\"{!v.record.who > 1}\">",
                    " <ui:outputRichText aura:id=\"secondary\" ",
                    " value=\"{!v.record.who > 1 ? ('&nbsp;+ ' + (v.record.who -1)) : ''}\"/>",
                    "</aura:if>",
                    "</div>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(3));
        }

        @Test
        void issue2() {
            StringBuilder builder = new StringBuilder()
                    .append("<ui:inputText aura:id=\"customInput\"")
                    .append(" class=\"{!'customInput' + v.idx}\" ")
                    .append("label=\"{!$Label.custom.input}\"")
                    .append(" labelPosition=\"hidden\" ")
                    .append("placeholder=\"{!$Label.custom.placeholder}\" ")
                    .append("value=\"{!v.limit >= 0 ? v.limit : ''}\"")
                    .append("disabled=\"{!v.limit == -1 ? true : false}\" />\n");


            List<String> html = Lists.newArrayList(builder.toString());
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(1));
        }

        @Test
        void issue3() {
            StringBuilder builder = new StringBuilder()
                    .append("<aura:method name=\"selection\"")
                    .append("description=\"Get selection (fieldName -> status),")
                    .append(" passed to API\">");

            List<String> html = Lists.newArrayList(builder.toString());
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(1));
        }

        @Test
        void issue4() {
            List<String> html = Lists.newArrayList(
                    " <ui:outputRichText aura:id=\"who\"",
                    "value=\"{!v.record.who ",
                    "> 1 ? ('&nbsp;+ ' + (v.record.who -1)) : ''}\"/>"
            );
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(1));
        }

        @Test
        void issue5() {
            StringBuilder builder = new StringBuilder()
                    .append("<aura:attribute name=\"context\" type=\"Map\"")
                    .append(" description=\"Context in format of {entity:{fieldName:fieldValue,...},...}")
                    .append(" to read information\"")
                    .append("/>");

            List<String> html = Lists.newArrayList(builder.toString());
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(1));
        }

        @Test
        void issue6() {
            List<String> html = Lists.newArrayList("<script>", "{", "<div>", "</div>", "</script>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(0));
        }

        @Test
        void issue7() {
            List<String> html = Lists.newArrayList("<div>", ">", "</div>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(1));
        }

        @Test
        void issue8() {
            List<String> html = Lists.newArrayList(
                    "<aura:event type=\"COMPONENT\" description=\"Indicate it's offline\" support=\"BETA\">",
                    "</aura:event>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
            assertThat(elements.size(), Matchers.is(1));
        }
    }


    @Test
    void brTagWithNoTrailingTagsOrText() {
        List<String> html = Lists.newArrayList("<template><tbody><tr><td><i>This</i> is a sample text <br>");
        List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
        String parsedContent = elements.get(0).getContent().text();
        // Test passing:
        // obtained parsedContent = "This is a sample text"
        assertThat(parsedContent, Matchers.is("This is a sample text"));
    }
    @Test
    void brTagWithTrailingTags() {
        List<String> html = Lists.newArrayList("<template><tbody><tr><td><i>This</i> is a <br>sample <b>text</b></td></tr></tbody></template>");
        List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
        String parsedContent = elements.get(0).getContent().text();
        // Test failing:
        // obtained parsedContent = "This"
        assertThat(parsedContent, Matchers.is("This is a sample text"));
    }
    @Test
    void brTagWithTrailingText() {
        List<String> html = Lists.newArrayList("<template><tbody><tr><td><i>This</i> is a <br>sample text");
        List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);
        String parsedContent = elements.get(0).getContent().text();
        // Test failing:
        // obtained parsedContent = "This is a"
        assertThat(parsedContent, Matchers.is("This is a sample text"));
    }
}
