/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.utils;

import com.salesforce.slds.shared.models.core.HTMLElement;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class HTMLElementUtilities {

    public List<HTMLElement> select(String componentName, String selector,  List<HTMLElement> elements) {
        final String query = cleanse(selector);

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
                                .collect(Collectors.toList());
                    } catch (Selector.SelectorParseException ex) {
                        return new ArrayList<HTMLElement>();
                    }
                })
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    String cleanseComponentName(String selector, String componentName) {
        return selector
                .replaceFirst("(?i)\\.c"+ StringUtils.capitalize(componentName), "")
                .trim();
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
}
