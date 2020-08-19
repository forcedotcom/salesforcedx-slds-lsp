/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.utils;

import com.google.common.collect.ImmutableList;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.HTMLElement;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class HTMLElementUtilities {

    public List<HTMLElement> select(Entry entry, String selector, List<HTMLElement> elements) {
        final String query = cleanse(selector);
        final String componentName = entry.getComponentName();

        Map<Element, HTMLElement> mapping = elements.stream().collect(Collectors.toMap(HTMLElement::getContent, s -> s));

        return elements.stream()
                .filter(element ->
                        element.getContent().parent().outerHtml().contentEquals(element.getContent().outerHtml()))
                .map(element -> {
                    String internalQuery = cleanseComponentName(query, componentName);

                    if (internalQuery.isEmpty()) {
                        return element.getContent().children().stream()
                                .filter(child -> child.tagName().startsWith("aura:") == false)
                                .map(mapping::get)
                                .filter(Objects::nonNull).collect(Collectors.toList());
                    }

                    try {
                        Elements queryResult = element.getContent().select(internalQuery);
                        return queryResult.stream().map(mapping::get).filter(Objects::nonNull)
                                .filter(item -> this.filterDynamicElement(entry.getEntityType(), item, internalQuery))
                                .collect(Collectors.toList());
                    } catch (Selector.SelectorParseException ex) {
                        return new ArrayList<HTMLElement>();
                    }
                })
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    boolean filterDynamicElement(Entry.EntityType type, HTMLElement element, String selector) {
        if (selector.contains(":") == false) {
            return true;
        }

        boolean containsChildOrTypeSelectors = Stream.of(selector.split("\\s"))
                .anyMatch(this::containsChildOrTypeSelector);

        boolean containDynamicElements = element
                .getContent().parents().stream()
                .anyMatch(parent -> this.wrappedInDynamicElement(type, parent));

        return !containsChildOrTypeSelectors || !containDynamicElements;
    }

    String cleanseComponentName(String selector, String componentName) {
        return selector
                .replaceFirst("(?i)\\.c"+ StringUtils.capitalize(componentName), "")
                .trim();
    }

    boolean wrappedInDynamicElement(Entry.EntityType type, Element element) {
        if (type == Entry.EntityType.AURA) {
            return element.tagName().toLowerCase().contentEquals("aura:iteration");
        } else if (type == Entry.EntityType.LWC) {
            return (element.tagName().toLowerCase().contentEquals("template")
            && ((element.hasAttr("for:each") && element.hasAttr("for:item"))
                    || element.hasAttr("iterator:it")));
        }

        return false;
    }

    boolean containsChildOrTypeSelector(String selector) {
        return CHILD_OR_TYPE_PSEUDO_SELECTORS.stream().anyMatch(clazz -> selector.contains(clazz));
    }

    String cleanse(String selector) {
        return selector
                .replaceAll("(?i)::?-ms[-\\w]+", "")
                .replaceAll("(?i)::?-webkit[-\\w]+", "")
                .replaceAll("(?i)::?-moz[-\\w]+", "")
                .replaceAll("(?i)\\.this", "")
                .replaceAll("(?i):active", "")
                .replaceAll("(?i):hover", "")
                .replaceAll("(?i):disabled", "")
                .replaceAll("(?i):focus", "")
                .replaceAll("(?i):checked", "")
                .replaceAll("(?i):link", "")
                .replaceAll("(?i):visited", "")
                .replaceAll("(?i)::?before", "")
                .replaceAll("(?i)::?after", "")
                .replaceAll("(?i)::selection", "")
                .replaceAll("(?i):vertical", "")
                .replaceAll("(?i):horizontal", "")
                .replaceAll("(?i):first-letter", "")
                .replaceAll("\n", "")
                .replaceAll("(?i):not\\([:\\w]*\\)", "").trim();
    }

    static final List<String> CHILD_OR_TYPE_PSEUDO_SELECTORS = ImmutableList.of(
       ":first-child", ":first-of-type", ":last-child", ":last-of-type",
       ":nth-child", ":nth-last-child", ":nth-last-of-type", ":nth-of-type",
            ":only-of-type", ":only-child");
}
