/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.slds.shared.parsers.markup;

import com.google.common.collect.ImmutableSet;
import com.salesforce.slds.shared.RegexPattern;
import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkupParser {

    public static List<HTMLElement> parse(String path, List<String> lines) {
        return new MarkupProcessor(path, lines).parse();
    }

    private static class MarkupProcessor {
        final List<HTMLElement> htmlElements = new ArrayList<>();

        final String path;
        final List<String> lines;

        MarkupProcessor(String path, List<String> lines) {
            this.path = path;
            this.lines = lines;
        }


        List<TagInformation> createTagInformation() {
            final Stack<String> commitBlocks = new Stack<>();
            final Stack<String> quotes = new Stack<>();

            List<TagInformation> result = new ArrayList<>();
            Stack<TagInformation> processing = new Stack<>();

            for (int index = 0; index < lines.size() ; index ++) {
                String line = lines.get(index);

                Matcher matcher = TAG.matcher(line);

                while(matcher.find()) {
                    boolean isComment = matcher.group("startComment") != null ||
                            matcher.group("endComment") != null;

                    boolean isStartTag = matcher.group("start") != null;

                    Location start = new Location(index, matcher.start());
                    Location end = new Location(index, matcher.end());
                    String tag = matcher.group(isStartTag ? "startTag" : "endTag");


                    if (processingComment(isComment, tag, isStartTag, commitBlocks)) {
                        continue;
                    }

                    String skipStart  = matcher.group("startSkip");
                    String skipEnd = matcher.group("endSkip");

                    if (skipStart != null || skipEnd != null) {
                        String content = skipStart != null ? skipStart : skipEnd;

                        boolean withinTag = processing.isEmpty() == false;

                        if (content.startsWith("\\") == false && withinTag ) {
                            for (char character : content.toCharArray()) {
                                if (QUOTES.contains(character)) {
                                    String top = quotes.empty() ? "" : quotes.peek();
                                    String inQuestion = String.valueOf(character);

                                    boolean popQuote = (top.contentEquals("{") && inQuestion.contentEquals("}"))
                                            || (inQuestion.equalsIgnoreCase("{") == false && inQuestion.equalsIgnoreCase(top));


                                    if (quotes.empty() == false && popQuote) {
                                        quotes.pop();
                                    } else {
                                        if (quotes.isEmpty() && inQuestion.equalsIgnoreCase("}") == false) {
                                            quotes.push(inQuestion);
                                        }
                                    }
                                }
                            }

                        }

                        continue;
                    }

                    if (quotes.isEmpty() == false) { continue;}

                    if (isStartTag) {
                        processing.push(new TagInformation(start, end, tag, TagInformation.TagType.INCOMPLETE));
                    } else {
                        if (tag != null) {
                            TagInformation tagInformation =
                                    new TagInformation(start, end, tag, TagInformation.TagType.CLOSE);

                            result.add(tagInformation);

                        } else if (processing.empty() == false) {
                            TagInformation startTag = processing.pop();

                            TagInformation.TagType type = matcher.group("end").length() == 1 ?
                                    TagInformation.TagType.OPEN : TagInformation.TagType.SELF_CLOSING;

                            TagInformation tagInformation =
                                    new TagInformation(startTag.start, end, startTag.tag, type);

                            result.add(tagInformation);
                        }
                    }
                }
            }

            return result;
        }


        List<HTMLElement> parse() {
            List<TagInformation> tags = createTagInformation();

            final Stack<TagInformation> internal = new Stack<>();
            final Stack<String> names = new Stack<>();

            for (int index = 0 ; index < tags.size() ; index ++) {
                TagInformation tag = tags.get(index);

                switch(tag.type) {
                    case INCOMPLETE:
                        continue;
                    case OPEN:
                        internal.push(tag);
                        names.push(tag.tag);
                        break;
                    case SELF_CLOSING:
                        process(tag, internal);
                        break;
                    case CLOSE:
                        break;
                }

                if (tag.type == TagInformation.TagType.CLOSE) {
                    TagInformation open = internal.empty()? null : internal.pop();
                    String name = names.empty() ? null : names.pop();

                    if (open == null) {
                        process(tag, null);
                        continue;
                    }

                    if ((open.tag.equalsIgnoreCase(tag.tag) == false &&  names.contains(tag.tag) == false)) {
                        process(tag, internal);

                        internal.push(open);
                        names.push(name);
                        continue;
                    }

                    if (open.tag.equalsIgnoreCase(tag.tag) == false && names.contains(tag.tag)) {
                        process(open, internal);
                        index --;
                        continue;
                    }

                    if (open.tag.equalsIgnoreCase(tag.tag)) {
                        TagInformation newTag = new TagInformation(open.start, tag.end, open.tag,
                                TagInformation.TagType.CLOSE);
                        newTag.children.addAll(open.children);

                        process(newTag, internal);
                    }
                }

            }

            if (internal.empty() == false) {
                process(internal.pop(), null);
            }

            htmlElements.sort(HTMLElement::compareTo);

            return htmlElements;
        }

        private void process(TagInformation tag, Stack<TagInformation> stack) {
            List<String> raw = extract(lines, tag.start, tag.end);

            HTMLElement htmlElement = HTMLElement.builder()
                    .raw(raw)
                    .element(createElement(raw, tag, path))
                    .range(new Range(tag.start, tag.end))
                    .build();

            htmlElements.add(htmlElement);

            if (stack != null && stack.size() > 0) {
                stack.peek().children.add(htmlElement);
            }
        }

        private boolean processingComment(boolean isComment, String tag, boolean isStartTag, Stack<String> stack) {
            // add Tag to support

            boolean process = isComment || (tag != null && tag.equalsIgnoreCase("script"));

            if (process) {
                String top = stack.empty() ? "" : stack.peek();
                String identifier = tag == null ? "" : tag;

                if (isStartTag) {
                    stack.push(identifier);
                } else {
                    if (top.equals(identifier)) {
                        stack.pop();
                    }
                }

                return true;
            }

            return stack.empty() == false;
        }

        private List<String> extract(List<String> lines, Location start, Location end) {
            List<String> raw = new ArrayList<>();

            for (int index = start.getLine() ;
                 index <= end.getLine() ;
                 index++
            ) {
                String line = lines.get(index);
                int startIndex = start.getLine() == index ?
                        start.getColumn() : 0;

                int endIndex = end.getLine() == index ?
                        end.getColumn() : line.length();

                raw.add(line.substring(startIndex, endIndex));
            }

            return raw;
        }

        private Element createElement(List<String> lines, TagInformation tagInformation, String baseUri) {
            String html = StringUtils.collectionToDelimitedString(lines, System.lineSeparator());
            if (html.startsWith("</")) {
                html = html.replaceFirst("</", "<");
            }

            Document document = Jsoup.parse(html, baseUri, Parser.xmlParser());

            Element result = document.child(0);
            List<HTMLElement> children = tagInformation.children;

            //handle self closing tag within custom element
            if (result.tagName().equalsIgnoreCase("head") == false && result.children().size() != children.size()) {
                Document htmlVersion = Jsoup.parse(html, baseUri, Parser.htmlParser());
                Element firstChild = htmlVersion.body().child(0);

                result.replaceWith(firstChild);
                result = document.child(0);
            }

            if (children.size() == result.children().size()) {
                for (int index = 0; index < children.size(); index++) {
                    Element childElement = result.child(index);
                    childElement.replaceWith(children.get(index).getContent());
                }
            }

            return result;
        }
    }

    static final Set<Character> QUOTES = ImmutableSet.of('{', '}', '\'', '"');
    static final Pattern TAG = Pattern.compile(RegexPattern.START_TAG_PATTERN + "|" + RegexPattern.END_TAG_PATTERN);
}
