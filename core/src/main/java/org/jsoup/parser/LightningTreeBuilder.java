/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package org.jsoup.parser;

import com.google.common.collect.Lists;
import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Stack;

public class LightningTreeBuilder extends XmlTreeBuilder {

    public static class ElementWithPosition extends Element {

        private int startPosition = -1;
        private int endPosition = -1;
        private Location start = new Location(-1, -1);
        private Location end = new Location(-1, -1);
        private List<String> raw;

        private ElementWithPosition(Tag tag, String baseUri, Attributes attributes) {
            super(tag, baseUri, attributes);
        }

        public HTMLElement toHTMLElement() {
            return HTMLElement.builder().element(this).raw(this.raw).range(new Range(start, end)).build();
        }
    }

    private final List<String> raw;
    private final String html;
    private final Stack<Token> skipStack = new Stack<>();

    public LightningTreeBuilder(List<String> raw) {
        this.raw = raw;
        this.html = StringUtils.collectionToDelimitedString(raw, System.lineSeparator());
    }

    @Override
    protected boolean process(Token token) {
        Token.TokenType type = token.type;

        if (type == Token.TokenType.StartTag) {
            insert(token.asStartTag());
        } else if (type == Token.TokenType.EndTag){
            popStackToClose(token.asEndTag());
        } else {
            super.process(token);
        }

        return true;
    }

    private void popStackToClose(Token.EndTag endTag) {
        String elName = settings.normalizeTag(endTag.tagName);
        Element firstFound = null;

        if (skipStack.isEmpty() == false) {
            if (elName.equalsIgnoreCase("script")) {
                skipStack.pop();
            }
            return;
        }

        for (int pos = stack.size() -1; pos >= 0; pos--) {
            Element next = stack.get(pos);
            if (next.nodeName().equals(elName)) {
                firstFound = next;
                break;
            }
        }
        if (firstFound == null) {
            Tag tag = Tag.valueOf(endTag.name(), settings);
            ElementWithPosition el = new ElementWithPosition(tag, null, settings.normalizeAttributes(endTag.attributes));
            updateLocation(el, endTag);
            insertNode(el);
            return; // not found, skip
        }

        for (int pos = stack.size() -1; pos >= 0; pos--) {
            ElementWithPosition next = (ElementWithPosition)stack.get(pos);
            stack.remove(pos);
            updateLocation(next, endTag);
            if (next == firstFound)
                break;
        }
    }


    Element insert(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name(), settings);
        if (startTag.attributes != null)
            startTag.attributes.deduplicate(settings);

        String elName = settings.normalizeTag(startTag.tagName);
        if (elName.equalsIgnoreCase("script") && startTag.isSelfClosing() == false) {
            skipStack.push(startTag);
        }

        if (skipStack.isEmpty() == false) {
            return null;
        }

        ElementWithPosition el = new ElementWithPosition(tag, null, settings.normalizeAttributes(startTag.attributes));
        updateLocation(el, startTag);
        insertNode(el);
        if (startTag.isSelfClosing()) {
            if (!tag.isKnownTag()) // unknown tag, remember this is self closing for output. see above.
                tag.setSelfClosing();
        } else {
            stack.add(el);
        }
        return el;
    }

    private void updateLocation(ElementWithPosition element, Token.Tag tag) {
        int position = this.reader.pos();

        if (tag instanceof Token.StartTag) {
            int marker = this.html.lastIndexOf(tag.name(), position);
            int startTagSymbol = this.html.lastIndexOf("<", marker);

            element.startPosition = startTagSymbol;
            element.start = convertPositionToLocation(element.startPosition);
        }

        if (tag instanceof Token.EndTag || tag.isSelfClosing()) {
            element.endPosition = position;
            element.end = convertPositionToLocation(element.endPosition);

            if (element.startPosition == -1) {
                updateLocation(element, new Token.StartTag().name(settings.normalizeTag(tag.tagName)));
            }

            element.raw = Lists.newArrayList(
                    this.html.substring(element.startPosition, element.endPosition).split(System.lineSeparator()));
        }
    }

    private Location convertPositionToLocation(int position) {
        int line = 0;
        int col = 0;

        while (position > 0) {
            int lineLength = this.raw.get(line).length();

            if (lineLength >= position) {
                col = position;
            } else {
                line++;
                position -= System.lineSeparator().length();
            }

            position -= lineLength;
        }

        return new Location(line, col);
    }

    private void insertNode(Node node) {
        currentElement().appendChild(node);
    }
}
