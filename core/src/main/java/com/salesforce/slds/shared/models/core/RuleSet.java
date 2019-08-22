/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

import com.salesforce.omakase.Omakase;
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.Rule;
import com.salesforce.omakase.ast.Syntax;
import com.salesforce.omakase.ast.collection.SyntaxCollection;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.plugin.core.SyntaxTree;
import com.salesforce.slds.shared.models.annotations.Annotation;
import com.salesforce.slds.shared.models.annotations.AnnotationScope;
import com.salesforce.slds.shared.models.annotations.AnnotationType;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.locations.RangeProvider;
import org.springframework.util.StringUtils;

import java.util.*;

public class RuleSet extends Input implements Comparable<RuleSet>, RangeProvider {

    private final Rule rule;
    private final Range range;
    private final List<String> raw;
    private final List<Style> styles;

    private RuleSet(Rule rule, Range range, List<String> raw) {
        this.rule = rule;
        this.range = range;
        this.raw = raw;
        this.styles = convertToStyles();
    }

    public Optional<AnnotationType> getAnnotationType() {
        return getAnnotationType(getRule());
    }

    public List<Style> getStyles() {
        return this.styles;
    }

    public List<Style> getStylesWithAnnotationType() {
        Optional<AnnotationType> ruleAnnotationType = getAnnotationType();
        List<Style> annotatedStyles = new ArrayList<>();

        for (Style style : getStyles()) {
            Style.StyleBuilder styleBuilder = Style.builder();
            styleBuilder
                    .property(style.getProperty())
                    .value(style.getValue())
                    .range(style.getRange())
                    .declaration(style.getDeclaration())
                    .condition(style.getCondition());

            if (style.getAnnotationType() == null) {
                styleBuilder.annotationType(ruleAnnotationType.orElse(AnnotationType.NONE));
            } else {
                styleBuilder.annotationType(style.getAnnotationType());
            }

            annotatedStyles.add(styleBuilder.build());
        }

        return annotatedStyles;
    }

    public List<Annotation> getAnnotations() {
        final List<Annotation> annotations = new ArrayList<>();

        Optional<AnnotationType> annotationType = getAnnotationType();

        if (annotationType.isPresent()) {
            annotations.add(Annotation
                    .builder()
                    .scope(AnnotationScope.BLOCK)
                    .type(annotationType.get())
                    .rule(getRule())
                    .build());
        }

        getStyles().stream().map(this::getAnnotation)
                .filter(Objects::nonNull)
                .forEachOrdered(annotation -> annotations.add(annotation));

        return annotations;
    }

    public String getSelectorsAsString(){

        StringBuilder selectorString = new StringBuilder();

        int size = this.rule.selectors().size();
        int index = 0;
        Iterator<Selector> selectors = this.rule.selectors().iterator();

        while (selectors.hasNext()) {
            Selector selector = selectors.next();

            selectorString.append(selector.toString(false));
            if (size != index + 1) {
                selectorString.append(", ");
                index++;
            }
        }

        return selectorString.toString().trim();

    }

    public Rule getRule() {
        return this.rule;
    }

    @Override
    public Range getRange() {
        return this.range;
    }

    @Override
    public int compareTo(RuleSet o) {
        return this.getRule().toString()
                .compareTo(o.getRule().toString());
    }

    @Override
    public String toString() {
        return this.getRule().toString(false);
    }

    private List<Style> convertToStyles() {
        List<Style> styles = new ArrayList<>();

        Style.StyleBuilder builder = Style.builder().declaration(getSelectorsAsString());
        final int anchor = getRule().line();

        SyntaxCollection<Rule, Declaration> declarations = getRule().declarations();
        declarations.stream().forEach(declaration -> {
            Optional<RawSyntax> rawValue = declaration.rawPropertyValue();
            Optional<RawSyntax> rawName = declaration.rawPropertyName();

            String name = rawName.isPresent() && rawName.get().content().startsWith("--lwc") ?
                    rawName.get().content() : declaration.propertyName().name();
            String value = rawValue.isPresent() ?
                    rawValue.get().content() : declaration.toString(true);

            Optional<AnnotationType> annotationType = getAnnotationType(declaration);
            Location start = new Location(declaration.line() - 1, declaration.column() - 1);
            Location end = findEndLocation(raw, declaration.line(), anchor, value);

            styles.add(builder
                    .property(name)
                    .value(value)
                    .range(new Range(start, end))
                    .annotationType(annotationType.orElse(null))
                    .build());

        });

        return styles;
    }

    private Optional<AnnotationType> getAnnotationType(Syntax syntax) {
        for (AnnotationType type : AnnotationType.values()) {
            if (type.value() != null && syntax.hasAnnotation(type.value())) {
                return Optional.of(type);
            }
        }

        return Optional.empty();
    }

    private Annotation getAnnotation(Style style) {

        AnnotationType annotationType = style.getAnnotationType();

        if (annotationType != null) {
            return Annotation.builder()
                    .type(annotationType)
                    .style(style)
                    .scope(AnnotationScope.INLINE).build();
        }

        return null;
    }

    private Location findEndLocation(List<String> lines, int declaration, int anchor,  String content) {
        int startingIndex = declaration - anchor;
        if (content.contains("\n")) {
            content = content.substring(content.lastIndexOf("\n") + 1);
        }

        for (int index = startingIndex ; index  < lines.size() ; index ++) {
            String line = lines.get(index);

            if (line.endsWith(content) || line.contains(content)) {
                return new Location(index + anchor - 1, line.lastIndexOf(content) + content.length() + 1);
            }
        }

        throw new RuntimeException("Can't find content: " + content);
    }


    public static RuleSetBuilder builder() {
        return new RuleSetBuilder();
    }

    @Override
    public Type getType() {
        return Type.STYLE;
    }


    public static class RuleSetBuilder {
        private Rule rule;
        private Range range;
        private List<String> raw;

        public RuleSetBuilder rule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public RuleSetBuilder range(Range range) {
            this.range = range;
            return this;
        }

        public RuleSetBuilder raw(List<String> raw) {
            this.raw = raw;
            return this;
        }

        public RuleSetBuilder content(String content) {
            SyntaxTree tree = new SyntaxTree();
            Omakase.source(content).use(tree).process();

            this.rule = tree.stylesheet().rules().get(0);
            this.raw = Arrays.asList(StringUtils.delimitedListToStringArray(content, System.lineSeparator()));
            return this;
        }

        public RuleSet build() {
            return new RuleSet(rule, range, raw);
        }
    }
}
