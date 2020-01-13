/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.slds.shared.utils;

import com.google.common.collect.Lists;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.parsers.css.CSSParser;
import com.salesforce.slds.shared.parsers.markup.MarkupParser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class EntryUtilitiesTests {

    private static final String COMPONENT_NAME = "component";

    static String generatePath(String ... parts) {
        return String.join(File.separator, parts);
    }

    @Nested
    @DisplayName("GetComponentName for Other Entity Type")
    class OtherEntityType {

        @Test
        void fileNameOnly() {
            verify(COMPONENT_NAME, COMPONENT_NAME + ".txt");
        }

        @Test
        void dotPath() {
            verify("e", generatePath("file:",  "a.c", "b", "c", "d", "e", COMPONENT_NAME + ".app"));
        }

        @Test
        void randomPath() {
            verify(COMPONENT_NAME + "-NAMESPACE",
                    File.separator + generatePath(COMPONENT_NAME + "-NAMESPACE",  COMPONENT_NAME + ".cmp"));
        }

        private void verify(String expected, String path) {
            Entry entry = Entry.builder().path(path).entityType(Entry.EntityType.OTHER).build();
            assertThat(EntryUtilities.getComponentName(entry), Matchers.is(expected));
        }
    }

    @Nested
    @DisplayName("GetComponentName for Component")
    class ComponentType {

        @Test
        void componentWithoutPath() {
            List<Input> inputs = new ArrayList<>();
            inputs.addAll(MarkupParser.parse( COMPONENT_NAME + ".cmp",
                    Lists.newArrayList("<aura:component></aura:component")));

            Entry entry = Entry.builder().inputs(inputs).entityType(Entry.EntityType.AURA)
                    .path(COMPONENT_NAME + ".cmp").build();

            assertThat(EntryUtilities.getComponentName(entry), Matchers.is(COMPONENT_NAME));
        }

        @Test
        void componentWithPath() {
            List<Input> inputs = new ArrayList<>();
            inputs.addAll(MarkupParser.parse( "/" + COMPONENT_NAME + "/test.html",
                    Lists.newArrayList("<template></template>")));

            Entry entry = Entry.builder().inputs(inputs).entityType(Entry.EntityType.LWC)
                    .path(File.separator + generatePath(COMPONENT_NAME, "test.html")).build();

            assertThat(EntryUtilities.getComponentName(entry), Matchers.is(COMPONENT_NAME));
        }

        @Test
        void bundleWithMarkup() {
            List<Input> inputs = new ArrayList<>();
            inputs.addAll(MarkupParser.parse( "/" + COMPONENT_NAME + "/test.html",
                    Lists.newArrayList("<template></template>")));

            Entry bundledEntry = Entry.builder().inputs(inputs).path(File.separator +
                    generatePath(COMPONENT_NAME, "test.html")).build();

            Bundle bundle = new Bundle(bundledEntry);
            Entry entry = Entry.builder().inputs(new ArrayList<>()).entityType(Entry.EntityType.LWC).build();
            entry.setBundle(bundle);

            assertThat(EntryUtilities.getComponentName(entry), Matchers.is(COMPONENT_NAME));
        }

        @Test
        void bundleWithStyles() {
            List<Input> inputs = new ArrayList<>();
            inputs.addAll(CSSParser.parse(
                    Lists.newArrayList(".c-test { display: block; }")));

            Entry bundledEntry = Entry.builder().inputs(inputs).path(COMPONENT_NAME + ".css").build();

            Bundle bundle = new Bundle(bundledEntry);
            Entry entry = Entry.builder().inputs(new ArrayList<>()).entityType(Entry.EntityType.LWC).build();
            entry.setBundle(bundle);

            assertThat(EntryUtilities.getComponentName(entry), Matchers.is(COMPONENT_NAME));
        }
    }

    @Nested
    @DisplayName("Entry Type")
    class EntryType {

        @Test
        void lwcJavascript() {
            List<String> content = Lists.newArrayList();
            content.add("export default class LwcTestComponent extends LightningElement {");
            content.add("}");

            Entry entry = Entry.builder().rawContent(content).path("component.js").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.LWC));
        }

        @Test
        void javascript() {
            List<String> content = Lists.newArrayList();
            content.add("function test() {}");

            Entry entry = Entry.builder().inputs(new ArrayList<>()).rawContent(content).path("component.js").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.OTHER));
        }

        @Test
        void lwcMarkup() {
            List<String> content = Lists.newArrayList();
            content.add("<template></template>");

            List<Input> inputs = new ArrayList<>();
            inputs.addAll(MarkupParser.parse( COMPONENT_NAME + ".html", content));

            Entry entry = Entry.builder().inputs(inputs).rawContent(content).path(COMPONENT_NAME + ".html").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.LWC));
        }

        @Test
        void auraLibrary() {
            List<String> content = Lists.newArrayList();
            content.add("<aura:library></aura:library>");

            List<Input> inputs = new ArrayList<>();
            inputs.addAll(MarkupParser.parse( COMPONENT_NAME + ".html", content));

            Entry entry = Entry.builder().inputs(inputs).rawContent(content).path(COMPONENT_NAME + ".html").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.AURA));
        }

        @Test
        void auraComponent() {
            List<String> content = Lists.newArrayList();
            content.add("<aura:component></aura:component>");

            List<Input> inputs = new ArrayList<>();
            inputs.addAll(MarkupParser.parse( COMPONENT_NAME + ".html", content));

            Entry entry = Entry.builder().inputs(inputs).rawContent(content).path(COMPONENT_NAME + ".html").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.AURA));
        }

        @Test
        void auraCSS() {
            List<String> content = Lists.newArrayList();
            content.add(".THIS .fun { display: block; }");

            List<Input> inputs = new ArrayList<>();
            inputs.addAll(CSSParser.parse(content));

            Entry entry = Entry.builder().inputs(inputs).rawContent(content).path(COMPONENT_NAME + ".css").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.AURA));
        }

        @Test
        void auraCSSWithComponentName() {
            List<String> content = Lists.newArrayList();
            content.add(".c" + COMPONENT_NAME+ " .fun { display: block; }");

            List<Input> inputs = new ArrayList<>();
            inputs.addAll(CSSParser.parse(content));

            Entry entry = Entry.builder().inputs(inputs).componentName(COMPONENT_NAME)
                    .rawContent(content).path(COMPONENT_NAME + ".css").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.AURA));
        }


        @Test
        void lwcCSSWithHost() {
            List<String> content = Lists.newArrayList();
            content.add(":host { display: block; }");

            List<Input> inputs = new ArrayList<>();
            inputs.addAll(CSSParser.parse(content));

            Entry entry = Entry.builder().inputs(inputs).rawContent(content)
                    .componentName(COMPONENT_NAME)
                    .path(COMPONENT_NAME + ".css").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.LWC));
        }

        @Test
        void lwcCSSWithComponentName() {
            List<String> content = Lists.newArrayList();
            content.add(".c-" + COMPONENT_NAME + " { display: block; }");

            List<Input> inputs = new ArrayList<>();
            inputs.addAll(CSSParser.parse(content));

            Entry entry = Entry.builder().inputs(inputs).rawContent(content)
                    .componentName(COMPONENT_NAME)
                    .path(COMPONENT_NAME + ".css").build();
            assertThat(EntryUtilities.getType(entry), Matchers.is(Entry.EntityType.LWC));
        }
    }
}
