package com.salesforce.slds.lsp;

import com.salesforce.slds.lsp.codeactions.CodeActionConverter;
import com.salesforce.slds.lsp.configuration.ServerConfiguration;
import com.salesforce.slds.lsp.diagnostics.DiagnosticConverter;
import com.salesforce.slds.lsp.models.DiagnosticResult;
import com.salesforce.slds.lsp.registries.DiagnosticResultRegistry;
import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.shared.models.annotations.AnnotationType;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.validation.runners.ValidateRunner;
import com.salesforce.slds.validation.validators.impl.recommendation.MobileSLDS_MarkupLabelValidator;
import com.salesforce.slds.validation.validators.impl.recommendation.MobileSLDS_CSSValidator;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static com.salesforce.slds.lsp.codeactions.CodeActionConverter.FILE_IGNORE_SLDS_VALIDATION;
import static com.salesforce.slds.lsp.codeactions.CodeActionConverter.LINE_IGNORE_SLDS_VALIDATION;
import static com.salesforce.slds.validation.validators.impl.recommendation.MobileSLDS_MarkupFriendlyValidator.NON_MOBILE_FRIENDLY_MESSAGE_TEMPLATE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ServerConfiguration.class)
public class RecommendationTests {

    static final String DEFAULT_URI = "1234";
    static final Range RANGE = new Range(new Position(0,19), new Position(0, 20));

    @Autowired
    ValidateRunner runner;

    @Autowired
    DiagnosticConverter converter;

    @Autowired
    DiagnosticResultRegistry diagnosticResultRegistry;

    @Autowired
    CodeActionConverter codeActionConverter;

    @Autowired
    TextDocumentRegistry textDocumentRegistry;

    @AfterEach
    void cleanUp() {
        textDocumentRegistry.clear();
    }

    @Nested
    class Javascript {
        @Test
        void markupClass() {
            StringBuilder builder = new StringBuilder();
            builder.append("({ getStyle: function() { return 'slds-border--right'; } })");
            Entry entry = createJavascriptEntry( Entry.EntityType.AURA, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = diagnosticResults.get(0).getDiagnostic().getRange();

            CodeAction action = getCodeAction(range, diagnosticResults).get(0);
            assertAction(action, "Update token to 'slds-border_right'", "slds-border_right");
        }
    }

    @Nested
    class LWC {
        @Test
        void deprecatedTokens() {
            StringBuilder builder = new StringBuilder();
            builder.append(".clazz {font-size: var(--lwc-fontSizeSmall);}");
            Entry entry = createCssEntry( Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            CodeAction action = getCodeAction(RANGE, diagnosticResults).get(0);
            assertAction(action, "Update token to 'fontSize2'", "var(--lwc-fontSize2)");
        }

        @Test
        void staticValueToDesignToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".clazz {font-size: 0.75rem;}");
            Entry entry = createCssEntry( Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            CodeAction action = getCodeAction(RANGE, diagnosticResults).get(0);
            assertAction(action, "Update token to 'fontSize2'",
                    "var(--lwc-fontSize2, 0.75rem)");
        }

        @Test
        void invalidDesignToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".clazz {font-size: var(--lwc-testing, 0.6rem)}");
            Entry entry = createCssEntry( Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            CodeAction action = getCodeAction(RANGE, diagnosticResults).get(0);
            assertAction(action, "Remove design token 'testing'", "0.6rem");
        }

        @Test
        void markupClassRecommendation() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template><div class=\"slds-border--right\">Hello World</div></template>");
            Entry entry = createMarkupEntry( Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = diagnosticResults.get(0).getDiagnostic().getRange();

            CodeAction action = getCodeAction(range, diagnosticResults).get(0);
            assertAction(action, "Update token to 'slds-border_right'", "slds-border_right");
        }
    }

    @Nested
    class AURA {
        @Test
        void deprecatedTokens() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: t(fontSizeSmall);}");
            Entry entry = createCssEntry( Entry.EntityType.AURA, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            CodeAction action = getCodeAction(RANGE, diagnosticResults).get(0);
            assertAction(action, "Update token to 'fontSize2'", "t(fontSize2)");
        }

        @Test
        void staticValueToDesignToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: 0.75rem;}");
            Entry entry = createCssEntry( Entry.EntityType.AURA, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            CodeAction action = getCodeAction(RANGE, diagnosticResults).get(0);
            assertAction(action, "Update token to 'fontSize2'", "t(fontSize2)");
        }

        @Test
        void invalidDesignToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: t(testing);}");
            Entry entry = createCssEntry( Entry.EntityType.AURA, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            CodeAction action = getCodeAction(RANGE, diagnosticResults).get(0);
            assertAction(action, "Remove design token 'testing'", "");
        }
        @Test
        void markupClassRecommendation() {
            StringBuilder builder = new StringBuilder();
            builder.append("<aura:component><div class=\"slds-border--right\">Hello World</div></aura:component>");
            Entry entry = createMarkupEntry( Entry.EntityType.AURA, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = diagnosticResults.get(0).getDiagnostic().getRange();

            CodeAction action = getCodeAction(range, diagnosticResults).get(0);
            assertAction(action, "Update token to 'slds-border_right'", "slds-border_right");
        }
    }

    @Nested
    class MOBILE {
        @Test
        void nonMobileFriendlyToken() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template><lightning-datatable></lightning-datatable></template>");
            Entry entry = createEntry("test.html", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            assertThat(diagnosticResults.size(), Matchers.equalTo(1));

            DiagnosticResult diagnosticResult = diagnosticResults.get(0);
            Diagnostic diagnostic = diagnosticResult.getDiagnostic();
            Range range = diagnostic.getRange();
            assertThat(range, Matchers.equalTo(new Range(new Position(0,10), new Position(0, 53))));
            assertThat(diagnostic.getMessage(), Matchers.equalTo("lightning-datatable" + NON_MOBILE_FRIENDLY_MESSAGE_TEMPLATE));

            List<CodeAction> actions = getCodeAction(range, diagnosticResults);
            assertThat(actions.size(), Matchers.equalTo(2));
            assertAction(actions.get(0), FILE_IGNORE_SLDS_VALIDATION, "<!-- " + AnnotationType.IGNORE.value() + " -->\n");
            assertAction(actions.get(1), LINE_IGNORE_SLDS_VALIDATION, "<!-- " + AnnotationType.IGNORE_NEXT_LINE.value() + " -->\n          ");
        }

        @Test
        void nonMobileFriendlyKebabCaseToken() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template><lightning-tree-grid></lightning-tree-grid></template>");
            Entry entry = createEntry("test.html", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            DiagnosticResult diagnosticResult = diagnosticResults.get(0);
            Diagnostic diagnostic = diagnosticResult.getDiagnostic();
            Range range = diagnostic.getRange();
            assertThat(range, Matchers.equalTo(new Range(new Position(0,10), new Position(0, 53))));

            List<Item> items = diagnosticResults.get(0).getItems();
            assertThat(items.size(), Matchers.equalTo(1));

            List<CodeAction> actions = getCodeAction(range, diagnosticResults);
            assertThat(actions.size(), Matchers.equalTo(2));
            assertAction(actions.get(0), FILE_IGNORE_SLDS_VALIDATION, "<!-- " + AnnotationType.IGNORE.value() + " -->\n");
            assertAction(actions.get(1), LINE_IGNORE_SLDS_VALIDATION, "<!-- " + AnnotationType.IGNORE_NEXT_LINE.value() + " -->\n          ");
        }

        @Test
        void mobileFriendlyToken() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template><lightning-accordion></lightning-accordion></template>");
            Entry entry = createEntry("test.html", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            assertThat(diagnosticResults.size(), Matchers.equalTo(0));
        }

        @Test
        void elementsWithoutLabels() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template>")
                    .append("   <input>")
                    .append("   <a><img></a>")
                    .append("   <lightning-button-icon></lightning-button-icon>")
                    .append("</template>");
            Entry entry = createEntry("test.html", Entry.EntityType.LWC, builder.toString());
            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            assertThat(diagnosticResults.size(), Matchers.equalTo(3));
            diagnosticResults.forEach(diagnosticResult -> {
                List<Item> items = diagnosticResult.getItems();
                assertThat(items, Matchers.hasSize(1));
                Set<Action> actions = items.get(0).getActions();
                assertThat(actions, Matchers.hasSize(1));
                Action action = actions.iterator().next();
                String description = action.getDescription();
                assertThat(description, Matchers.equalTo(MobileSLDS_MarkupLabelValidator.REQUIRE_LABELS));
            });
        }

        @Test
        void elementsWithLabels() {
            StringBuilder builder = new StringBuilder();
            builder.append("<template>")
                    .append("   <label><input></label>")
                    .append("   <label for=\"myImg\"></label><a><img id=\"myImg\"></a>")
                    .append("   <label><lightning-button-icon></lightning-button-icon></label>")
                    .append("</template>");
            Entry entry = createEntry("test.html", Entry.EntityType.LWC, builder.toString());
            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            assertThat(diagnosticResults.size(), Matchers.equalTo(0));
        }

        @Test
        void smallFontSizeToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: var(--lwc-fontSize3);}");
            Entry entry = createEntry("test.css", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            assertThat(diagnosticResults.size(), Matchers.equalTo(1));
            diagnosticResults.forEach(diagnosticResult -> {
                List<Item> items = diagnosticResult.getItems();
                assertThat(items, Matchers.hasSize(1));
                Set<Action> actions = items.get(0).getActions();
                assertThat(actions, Matchers.hasSize(1));
                Action action = actions.iterator().next();
                String description = action.getDescription();
                assertThat(description, Matchers.equalTo(MobileSLDS_CSSValidator.USE_FONT_SIZE_4_OR_LARGER));
            });
        }

        @Test
        void acceptableFontSizeToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: var(--lwc-fontSize4);}");
            Entry entry = createEntry("test.css", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            assertThat(diagnosticResults.size(), Matchers.equalTo(0));
        }

        @Test
        void smallFontSize() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {")
                    .append(System.lineSeparator())
                    .append(    "font-size: 13px;")
                    .append(System.lineSeparator())
                    .append(    "font: italic small-caps bold 13px Georgia, serif;")
                    .append(System.lineSeparator())
                    .append("}");
            Entry entry = createEntry("test.css", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            assertThat(diagnosticResults.size(), Matchers.equalTo(2));
            diagnosticResults.forEach(diagnosticResult -> {
                List<Item> items = diagnosticResult.getItems();
                assertThat(items, Matchers.hasSize(1));
                Set<Action> actions = items.get(0).getActions();
                assertThat(actions, Matchers.hasSize(1));
                Action action = actions.iterator().next();
                String description = action.getDescription();
                assertThat(description, Matchers.equalTo(MobileSLDS_CSSValidator.USE_FONT_SIZE_14PX_OR_LARGER));
            });
        }

        @Test
        void acceptableFontSize() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {")
                    .append(System.lineSeparator())
                    .append("font-size: 15px;")
                    .append(System.lineSeparator())
                    .append("font: italic small-caps bold 15px Georgia, serif;")
                    .append(System.lineSeparator())
                    .append("}");
            Entry entry = createEntry("test.css", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            assertThat(diagnosticResults.size(), Matchers.equalTo(0));
        }

        @Test
        void explicitTruncation() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {")
                    .append(System.lineSeparator())
                    .append(    "white-space: nowrap;")
                    .append(System.lineSeparator())
                    .append(    "text-overflow: ellipsis;")
                    .append(System.lineSeparator())
                    .append("}");
            Entry entry = createEntry("test.css", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            assertThat(diagnosticResults.size(), Matchers.equalTo(2));
            diagnosticResults.forEach(diagnosticResult -> {
                List<Item> items = diagnosticResult.getItems();
                assertThat(items, Matchers.hasSize(1));
                Set<Action> actions = items.get(0).getActions();
                assertThat(actions, Matchers.hasSize(1));
                Action action = actions.iterator().next();
                String description = action.getDescription();
                assertThat(description, Matchers.equalTo(MobileSLDS_CSSValidator.AVOID_TRUNCATION));
            });
        }

        @Test
        void noTruncation() {
            String warning = "Avoid truncating labels on mobile.";
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {")
                    .append(System.lineSeparator())
                    .append(    "white-space: normal;")
                    .append(System.lineSeparator())
                    .append(    "text-overflow: clip;")
                    .append(System.lineSeparator())
                    .append("}");
            Entry entry = createEntry("test.css", Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);

            assertThat(diagnosticResults.size(), Matchers.equalTo(0));
        }
    }

    private void assertAction(CodeAction action, String expectedTitle, String expectedNewText) {
        assertThat(action.getTitle(), Matchers.is(expectedTitle));
        assertThat(action.getKind(), Matchers.is(CodeActionKind.QuickFix));
        List<TextEdit> edits = action.getEdit().getChanges().get(DEFAULT_URI);

        assertThat(edits, Matchers.hasSize(1));
        assertThat(edits.get(0).getNewText(), Matchers.is(expectedNewText));
    }

    private List<CodeAction> getCodeAction(Range range, List<DiagnosticResult> diagnosticResults) {
        CodeActionParams params = createCodeActionParams(range, diagnosticResults);

        List<Either<Command, CodeAction>> results = codeActionConverter.convert(params);
        assertThat(results.size(), Matchers.greaterThan(0));
        return results.stream().map(item -> item.getRight()).collect(Collectors.toList());
    }

    private CodeActionParams createCodeActionParams(Range range,
                                                    List<DiagnosticResult> diagnosticResults) {
        TextDocumentIdentifier item = Mockito.mock(TextDocumentIdentifier.class);
        Mockito.when(item.getUri()).thenReturn(DEFAULT_URI);

        CodeActionParams params = new CodeActionParams();
        params.setContext(new CodeActionContext(
                diagnosticResults.stream()
                        .map(DiagnosticResult::getDiagnostic)
                        .collect(Collectors.toList())));
        params.setTextDocument(item);
        params.setRange(range);

        return params;
    }

    private List<DiagnosticResult> getDiagnosticResult(Entry entry) {
        List<DiagnosticResult> diagnostics = converter.convert(entry);
        diagnosticResultRegistry.put(DEFAULT_URI, diagnostics);

        return diagnostics;
    }

    private Entry createJavascriptEntry(Entry.EntityType type, String content) {
        return createEntry("test.js", type, content);
    }

    private Entry createMarkupEntry(Entry.EntityType type, String content) {
        return createEntry("test.cmp", type, content);
    }

    private Entry createCssEntry(Entry.EntityType type, String content) {
        return createEntry("test.css", type, content);
    }

    private Entry createEntry(String name, Entry.EntityType type, String content ) {
        Entry entry = Entry.builder().path(name).entityType(type).rawContent(
                Arrays.asList(StringUtils.delimitedListToStringArray(content, System.lineSeparator()))).build();

        runner.setBundle(new Bundle(entry));
        runner.run();

        return entry;
    }
}
