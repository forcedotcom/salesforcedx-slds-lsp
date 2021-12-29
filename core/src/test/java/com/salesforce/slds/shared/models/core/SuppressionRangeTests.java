package com.salesforce.slds.shared.models.core;

import com.google.common.collect.Lists;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.parsers.css.CSSParser;
import com.salesforce.slds.shared.parsers.javascript.JavascriptParser;
import com.salesforce.slds.shared.parsers.markup.MarkupParser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class SuppressionRangeTests {

    @Nested
    class StyleAnnotation {

        @Test
        public void blockAnnotation() {
            StringBuilder builder = new StringBuilder();
            builder.append("/* @sldsValidatorIgnore */ .THIS { padding: 0; } /* @sldsValidatorAllow */")
                    .append(System.lineSeparator())
                    .append(".THIS thead { display: block; }");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            RuleSet suppressedRuleSet = entry.getInputs().get(0).asRuleSet();
            RuleSet unsuppressedRuleSet = entry.getInputs().get(1).asRuleSet();

            assertThat(suppressionRange.within(suppressedRuleSet.getRange()), Matchers.is(true));
            assertThat(suppressionRange.within(unsuppressedRuleSet.getRange()), Matchers.is(false));
        }

        @Test
        public void ignoreBlocksWithoutClosing() {
            StringBuilder builder = new StringBuilder();
            builder.append("/* @sldsValidatorIgnore */")
                    .append(".THIS {  padding: 0;")
                    .append(System.lineSeparator())
                    .append("padding: 0; }")
                    .append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(".THIS thead { display: block; }");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            RuleSet firstRule = entry.getInputs().get(0).asRuleSet();
            RuleSet secondRule = entry.getInputs().get(1).asRuleSet();

            assertThat(suppressionRange.within(firstRule.getRange()), Matchers.is(true));
            assertThat(suppressionRange.within(secondRule.getRange()), Matchers.is(true));
        }

        @Test
        public void inlineAnnotation() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS { /* @sldsValidatorIgnore */ padding: 0;")
                    .append(System.lineSeparator())
                    .append("padding: 0; }");

            Entry entry = create(builder.toString());
            assertThat(entry.getRecommendationSuppressionRanges(), Matchers.emptyIterable());
        }

        @Test
        public void ignoreNextLineAnnotation() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS { /* @sldsValidatorIgnoreNextLine */ ")
                    .append(System.lineSeparator())
                    .append("padding: 0;")
                    .append(System.lineSeparator())
                    .append("padding: 0; }");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            RuleSet ruleSet = entry.getInputs().get(0).asRuleSet();
            Style suppressedStyle = ruleSet.getStyles().get(0);
            Style unsuppressedStyle = ruleSet.getStyles().get(1);

            assertThat(suppressionRange.within(suppressedStyle.getRange()), Matchers.is(true));
            assertThat(suppressionRange.within(unsuppressedStyle.getRange()), Matchers.is(false));
        }

        private Entry create(String content) {
            List<Input> inputs = Lists.newArrayList();
            Entry result = createEntry(CSS_PATH, Entry.EntityType.AURA, content);
            inputs.addAll(CSSParser.parse(result.getRawContent()));
            result.setInputs(inputs);

            return result;
        }
    }


    @Nested
    class HTMLAnnotation {
        @Test
        public void blockAnnotation() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template>")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorIgnore -->")
                    .append(System.lineSeparator())
                    .append("<lightning-datatable></lightning-datatable>")
                    .append(System.lineSeparator())
                    .append("<lightning-flow-support></lightning-flow-support>")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorAllow -->")
                    .append(System.lineSeparator())
                    .append("<lightning-accordion></lightning-accordion>")
                    .append(System.lineSeparator())
                    .append("</template>");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            HTMLElement element = entry.getInputs().get(1).asElement();
            assertThat(suppressionRange.within(element.getRange()), Matchers.is(true));
        }

        @Test
        public void ignoreBlocksWithoutClosing() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template>")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorIgnore -->")
                    .append(System.lineSeparator())
                    .append("<lightning-datatable></lightning-datatable>")
                    .append(System.lineSeparator())
                    .append("<lightning-flow-support></lightning-flow-support>")
                    .append(System.lineSeparator())
                    .append("</template>");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            HTMLElement firstElement = entry.getInputs().get(1).asElement();
            HTMLElement secondElement = entry.getInputs().get(2).asElement();

            assertThat(suppressionRange.within(firstElement.getRange()), Matchers.is(true));
            assertThat(suppressionRange.within(secondElement.getRange()), Matchers.is(true));
        }

        @Test
        public void ignoreNextLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template>")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorIgnoreNextLine -->")
                    .append(System.lineSeparator())
                    .append("<lightning-datatable></lightning-datatable>")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorIgnoreNextLine -->")
                    .append("<lightning-accordion>")
                    .append("</lightning-accordion>")
                    .append(System.lineSeparator())
                    .append("</template>");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            /** Ignore entire line **/
            HTMLElement firstElement = entry.getInputs().get(1).asElement();
            /** Ignore own content in the next line **/
            HTMLElement secondElement = entry.getInputs().get(2).asElement();

            assertThat(suppressionRange.within(firstElement.getRange()), Matchers.is(true));
            assertThat(suppressionRange.within(secondElement.getRange()), Matchers.is(false));
        }

        private Entry create(String content) {
            List<Input> inputs = Lists.newArrayList();
            Entry result = createEntry(CMP_PATH, Entry.EntityType.AURA, content);
            inputs.addAll(MarkupParser.parse(CMP_PATH, result.getRawContent()));
            result.setInputs(inputs);

            return result;
        }
    }

    @Nested
    class JavascriptAnnotation {
        @Test
        public void blockAnnotation() {
            StringBuilder builder = new StringBuilder();
            builder.append("({")
                    .append(System.lineSeparator())
                    .append("test: function(a) {")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorIgnore -->")
                    .append(System.lineSeparator())
                    .append(" var item = 'slds-item';")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorIgnore -->")
                    .append(System.lineSeparator())
                    .append("}")
                    .append(System.lineSeparator())
                    .append("})");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            Block block = entry.getInputs().get(0).asBlock();
            assertThat(suppressionRange.within(new Range(new Location(block.getLineNumber() + 1, 0),
                    new Location(block.getLineNumber() + 1,Integer.MAX_VALUE))), Matchers.is(true));
        }

        @Test
        public void ignoreBlocksWithoutClosing() {
            StringBuilder builder = new StringBuilder();
            builder.append("({")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorIgnore -->")
                    .append(System.lineSeparator())
                    .append("test: function(a) {")
                    .append(System.lineSeparator())
                    .append(" var item = 'slds-item';")
                    .append("}")
                    .append(System.lineSeparator())
                    .append("test2: function(a) {")
                    .append(System.lineSeparator())
                    .append(" var item2 = 'slds-item2';")
                    .append("}")
                    .append(System.lineSeparator())
                    .append("})");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            Block block1 = entry.getInputs().get(0).asBlock();
            assertThat(suppressionRange.within(new Range(new Location(block1.getLineNumber() + 1, 0),
                    new Location(block1.getLineNumber() + 1,Integer.MAX_VALUE))), Matchers.is(true));

            Block block2 = entry.getInputs().get(0).asBlock();
            assertThat(suppressionRange.within(new Range(new Location(block2.getLineNumber() + 1, 0),
                    new Location(block2.getLineNumber() + 1,Integer.MAX_VALUE))), Matchers.is(true));
        }

        @Test
        public void ignoreNextLine() {
            StringBuilder builder = new StringBuilder();
            builder.append("({")
                    .append(System.lineSeparator())
                    .append("test: function(a) {")
                    .append(System.lineSeparator())
                    .append("<!-- sldsValidatorIgnoreNextLine -->")
                    .append(System.lineSeparator())
                    .append("var item = 'slds-item';")
                    .append(System.lineSeparator())
                    .append("}, ")
                    .append(System.lineSeparator())
                    .append("test2: function(a) {")
                    .append(System.lineSeparator())
                    .append("var item2 = 'slds-item2';")
                    .append(System.lineSeparator())
                    .append("}")
                    .append(System.lineSeparator())
                    .append("})");

            Entry entry = create(builder.toString());
            Range suppressionRange = entry.getRecommendationSuppressionRanges().get(0);

            Block block1 = entry.getInputs().get(0).asBlock();
            assertThat(suppressionRange.within(new Range(new Location(block1.getLineNumber() + 1, 0),
                    new Location(block1.getLineNumber() + 1,Integer.MAX_VALUE))), Matchers.is(true));

            Block block2 = entry.getInputs().get(1).asBlock();
            assertThat(suppressionRange.within(new Range(new Location(block2.getLineNumber() + 1, 0),
                    new Location(block2.getLineNumber() + 1,Integer.MAX_VALUE))), Matchers.is(false));
        }

        private Entry create(String content) {
            List<Input> inputs = Lists.newArrayList();
            Entry result = createEntry(JS_PATH, Entry.EntityType.AURA, content);
            inputs.addAll(JavascriptParser.convert(JS_PATH, result.getRawContent()));
            result.setInputs(inputs);

            return result;
        }
    }

    private Entry createEntry(String path, Entry.EntityType type, String content) {
        return Entry.builder().path(path).entityType(type).rawContent(
                Arrays.asList(StringUtils.delimitedListToStringArray(content, System.lineSeparator()))).build();
    }

    private static final String CSS_PATH = "test.css";
    private static final String CMP_PATH = "test.cmp";
    private static final String JS_PATH = "test.js";
}
