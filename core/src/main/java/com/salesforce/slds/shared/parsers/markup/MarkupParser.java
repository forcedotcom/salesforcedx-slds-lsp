/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.slds.shared.parsers.markup;

import com.salesforce.slds.shared.models.core.HTMLElement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.LightningTreeBuilder;
import org.jsoup.parser.Parser;
import org.springframework.util.StringUtils;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.util.*;

public class MarkupParser {


    public static List<HTMLElement> parse(String path, List<String> lines) {
        String html = StringUtils.collectionToDelimitedString(lines, System.lineSeparator());
        Document document = Jsoup.parse(html, path, new Parser(new LightningTreeBuilder(lines)));

        MarkupVisitor visitor = new MarkupVisitor();
        NodeTraversor.traverse(visitor, document.children());

        visitor.htmlElements.sort(HTMLElement::compareTo);
        return visitor.htmlElements;
    }

    private static class MarkupVisitor implements NodeVisitor {
        final List<HTMLElement> htmlElements = new ArrayList<>();

        @Override
        public void head(Node node, int depth) {
            if (node instanceof LightningTreeBuilder.ElementWithPosition) {
                htmlElements.add(((LightningTreeBuilder.ElementWithPosition) node).toHTMLElement());
            }
        }

        @Override
        public void tail(Node node, int depth) { }
    }
}
