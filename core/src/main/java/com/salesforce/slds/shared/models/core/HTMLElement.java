/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

import com.google.common.xml.XmlEscapers;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.locations.RangeProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class HTMLElement extends Input implements Comparable<HTMLElement>, RangeProvider {

    private final Element element;
    private final Range range;
    private final List<String> raw;

    private HTMLElement(Range range, Element element, List<String> raw) {
        this.element = element;
        this.range = range;
        this.raw = raw;
    }

    @Override
    public Range getRange() {
        return this.range;
    }

    public Element getContent() {
        return this.element;
    }

    public Range getTagRange() {
        String tag = getContent().tagName();
        return findRange(tag, 0, 0);
    }

    public Map<String, Range> getClasses() {
        Map<String, Range> classes = new LinkedHashMap<>();
        List<String> classNames = getContent().classNames()
                .stream()
                .map(s -> Arrays.asList(s.split("[,:\\(\\)?\\s]")))
                .flatMap(List::stream)
                .filter(s -> s.isEmpty() == false)
                .collect(Collectors.toList());

        if (classNames.isEmpty() == false) {
            OptionalInt rowIndex = IntStream.range(0, raw.size())
                    .filter(index -> raw.get(index).matches("class\\s*="))
                    .findFirst();

            int lineIndex = rowIndex.orElse(0);
            int column = raw.get(lineIndex).indexOf("class");

            for (String className : classNames) {
                String cleansed = className.contains("slds-") &&
                        className.contains(".") == false ? cleanse(className) : className;

                Range range = findRange(cleansed, lineIndex, column);
                if (range == null) {
                    range = findRange(cleansed, 0, 0);
                }

                classes.put(cleansed, range);

                lineIndex = range.getStart().getLine() - getRange().getStart().getLine();
                column = lineIndex == 0 ?
                        range.getStart().getColumn() - getRange().getStart().getColumn() :
                        range.getStart().getColumn();
            }
        }

        return classes;
    }

    private Range findRange(String text, int line, int column) {
        Range range = null;
        String target = text.toLowerCase();

        do {
            String content = raw.get(line).toLowerCase();

            column = content.indexOf(target, column);

            if (column == -1) {
                column = content.indexOf(xmlEscape(target), column);
            }

            if (column == -1) {
                column = content.indexOf(escapeCharacter(target), column);
            }

            if (column != -1) {
                int columnPosition = line == 0 ? getRange().getStart().getColumn() + column : column;
                int linePosition = line + getRange().getStart().getLine();

                range = new Range(
                        new Location(linePosition, columnPosition),
                        new Location(linePosition, columnPosition + text.length()));
            }

            if (column == -1) {
                line++;
            }
        } while (range == null && raw.size() > line);

        return range;
    }

    private String cleanse(String value) {
        return EXPRESSION.matcher(value).replaceAll("")
                .replaceAll("v\\.[a-zA-Z]+", "");
    }

    private String xmlEscape(String content) {
        return XmlEscapers.xmlAttributeEscaper().escape(content);
    }

    private String escapeCharacter(String content) {
        return content.replaceAll("--", "&#45;&#45;");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        HTMLElement that = (HTMLElement) o;

        return new EqualsBuilder()
                .append(range, that.range)
                .append(element, that.element)
                .append(raw, that.raw)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(element)
                .append(range)
                .append(raw)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("element", element)
                .append("range", range)
                .append("raw", raw)
                .toString();
    }

    public static HTMLElementBuilder builder() {
        return new HTMLElementBuilder();
    }

    @Override
    public int compareTo(HTMLElement o) {
        if (this.equals(o)) {
            return 0;
        }

        return this.getRange().compareTo(o.getRange());
    }

    @Override
    public Type getType() {
        return Type.MARKUP;
    }

    public static class HTMLElementBuilder {
        Element element;
        Range range;
        List<String> raw;

        public HTMLElementBuilder element(Element element) {
            this.element = element;
            return this;
        }

        public HTMLElementBuilder range(Range range) {
            this.range = range;
            return this;
        }

        public HTMLElementBuilder raw(List<String> raw) {
            this.raw = raw;
            return this;
        }

        public HTMLElement build() {
            return new HTMLElement(range, element, raw);
        }
    }

    static final Pattern EXPRESSION = Pattern.compile("[\\{\\}!#\\+\\?\"\':\\(\\),]");
}
