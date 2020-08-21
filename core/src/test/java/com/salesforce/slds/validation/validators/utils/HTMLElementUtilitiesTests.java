package com.salesforce.slds.validation.validators.utils;

import com.google.common.collect.Lists;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.parsers.markup.MarkupParser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class HTMLElementUtilitiesTests {

    private static final HTMLElementUtilities utilities = new HTMLElementUtilities();
    private static final String COMPONENT_NAME = "component";

    @Nested
    @DisplayName("Cleanse Selectors with Component Name")
    class SelectorWithComponentName {

        @Test
        void simple() {
            String result = utilities.cleanseComponentName(".cComponent .class", COMPONENT_NAME);
            assertThat(result, Matchers.is(".class"));
        }

        @Test
        void joinSelector() {
            String result = utilities.cleanseComponentName(".cComponent.class", COMPONENT_NAME);
            assertThat(result, Matchers.is(".class"));
        }

        @Test
        void similarSelectors() {
            String result = utilities.cleanseComponentName(".cComponent .cComponentItem .class", COMPONENT_NAME);
            assertThat(result, Matchers.is(".cComponentItem .class"));
        }
    }

    @Nested
    @DisplayName("HTML Query Selectors")
    class QuerySelector {

        @Test
        void basic() {
            List<String> html = Collections.singletonList("<aura:component><div>Test</div><span>SPAN</span></aura:component>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);

            Entry entry = Mockito.mock(Entry.class);
            Mockito.when(entry.getComponentName()).thenReturn("test");
            Mockito.when(entry.getEntityType()).thenReturn(Entry.EntityType.AURA);

            List<HTMLElement> results = utilities.select(entry,".THIS div", elements);
            assertThat(results, Matchers.iterableWithSize(1));
            assertThat(results.get(0).getContent().tagName(), Matchers.is("div"));
        }

        @Test
        void thisQuerySelector() {
            List<String> html = Collections.singletonList("<aura:component><div>Test<span>Inner Child</span></div><span>SPAN</span></aura:component>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);

            Entry entry = Mockito.mock(Entry.class);
            Mockito.when(entry.getComponentName()).thenReturn("test");
            Mockito.when(entry.getEntityType()).thenReturn(Entry.EntityType.AURA);

            List<HTMLElement> results = utilities.select(entry,".THIS", elements);
            assertThat(results, Matchers.iterableWithSize(2));
            assertThat(results.get(0).getContent().tagName(), Matchers.is("div"));
            assertThat(results.get(1).getContent().tagName(), Matchers.is("span"));
        }

        @Test
        void firstChild() {
            List<String> html = Collections.singletonList("<aura:component><div>FIRST</div><div>SECOND</div></aura:component>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);

            Entry entry = Mockito.mock(Entry.class);
            Mockito.when(entry.getComponentName()).thenReturn("test");
            Mockito.when(entry.getEntityType()).thenReturn(Entry.EntityType.AURA);

            List<HTMLElement> results = utilities.select(entry,".THIS div:first-child", elements);
            assertThat(results, Matchers.iterableWithSize(1));
            assertThat(results.get(0).getContent().text(), Matchers.is("FIRST"));
        }

        @Test
        void dynamic() {
            List<String> html = Collections.singletonList("<aura:component><aura:iteration><div>FIRST</div></aura:iteration></aura:component>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);

            Entry entry = Mockito.mock(Entry.class);
            Mockito.when(entry.getComponentName()).thenReturn("test");
            Mockito.when(entry.getEntityType()).thenReturn(Entry.EntityType.AURA);

            List<HTMLElement> results = utilities.select(entry,".THIS div:first-child", elements);
            assertThat(results, Matchers.iterableWithSize(0));
        }

        @Test
        void lwcLoop() {
            List<String> html = Lists.newArrayList(
                    "<template><ui>",
                    "<template for:each={contacts} for:item=\"contact\">",
                    "<div>{contact.Name}</div></template></ui>",
                    "</template>");
            List<HTMLElement> elements = MarkupParser.parse("test.cmp", html);

            Entry entry = Mockito.mock(Entry.class);
            Mockito.when(entry.getComponentName()).thenReturn("test");
            Mockito.when(entry.getEntityType()).thenReturn(Entry.EntityType.LWC);

            List<HTMLElement> results = utilities.select(entry,"ui div:first-child", elements);
            assertThat(results, Matchers.iterableWithSize(0));
        }
    }
}
