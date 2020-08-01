package com.salesforce.slds.lsp;

import com.salesforce.slds.lsp.codeactions.CodeActionConverter;
import com.salesforce.slds.lsp.configuration.ServerConfiguration;
import com.salesforce.slds.lsp.diagnostics.DiagnosticConverter;
import com.salesforce.slds.lsp.models.DiagnosticResult;
import com.salesforce.slds.lsp.registries.DiagnosticResultRegistry;
import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.validation.runners.ValidateRunner;
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
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ServerConfiguration.class)
public class RecommendationTests {

    static final String DEFAULT_URI = "1234";
    static final Position START = new Position(0,0);

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
    class LWC {
        @Test
        void deprecatedTokens() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: var(--lwc-fontSizeSmall);}");
            Entry entry = createEntry( Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = createRange(builder.toString());

            CodeAction action = getCodeAction(range, diagnosticResults);
            assertAction(action, "Update token to 'fontSize2'", "var(--lwc-fontSize2)");
        }

        @Test
        void staticValueToDesignToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: 0.75rem;}");
            Entry entry = createEntry( Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = createRange(builder.toString());

            CodeAction action = getCodeAction(range, diagnosticResults);
            assertAction(action, "Update token to 'fontSize2'",
                    "var(--lwc-fontSize2, 0.75rem)");
        }

        @Test
        void invalidDesignToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: var(--lwc-testing, 0.6rem)}");
            Entry entry = createEntry( Entry.EntityType.LWC, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = createRange(builder.toString());

            CodeAction action = getCodeAction(range, diagnosticResults);
            assertAction(action, "Remove design token 'testing'", "0.6rem");
        }
    }

    @Nested
    class AURA {
        @Test
        void deprecatedTokens() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: t(fontSizeSmall);}");
            Entry entry = createEntry( Entry.EntityType.AURA, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = createRange(builder.toString());

            CodeAction action = getCodeAction(range, diagnosticResults);
            assertAction(action, "Update token to 'fontSize2'", "t(fontSize2)");
        }

        @Test
        void staticValueToDesignToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: 0.75rem;}");
            Entry entry = createEntry( Entry.EntityType.AURA, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = createRange(builder.toString());

            CodeAction action = getCodeAction(range, diagnosticResults);
            assertAction(action, "Update token to 'fontSize2'", "t(fontSize2)");
        }

        @Test
        void invalidDesignToken() {
            StringBuilder builder = new StringBuilder();
            builder.append(".THIS {font-size: t(testing);}");
            Entry entry = createEntry( Entry.EntityType.AURA, builder.toString());

            List<DiagnosticResult> diagnosticResults = getDiagnosticResult(entry);
            Range range = createRange(builder.toString());

            CodeAction action = getCodeAction(range, diagnosticResults);
            assertAction(action, "Remove design token 'testing'", "");
        }
    }

    private void assertAction(CodeAction action, String expectedTitle, String expectedNewText) {
        assertThat(action.getTitle(), Matchers.is(expectedTitle));
        assertThat(action.getKind(), Matchers.is(CodeActionKind.QuickFix));
        List<TextEdit> edits = action.getEdit().getChanges().get(DEFAULT_URI);

        assertThat(edits, Matchers.hasSize(1));
        assertThat(edits.get(0).getNewText(), Matchers.is(expectedNewText));
    }

    private Range createRange(String content) {
        return new Range(START, new Position(0, content.length()));
    }

    private CodeAction getCodeAction(Range range, List<DiagnosticResult> diagnosticResults) {
        CodeActionParams params = createCodeActionParams(range, diagnosticResults);

        List<Either<Command, CodeAction>> results = codeActionConverter.convert(params);
        assertThat(results.size(), Matchers.greaterThan(0));
        return results.get(0).getRight();
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

    private Entry createEntry(Entry.EntityType type, String content) {
        Entry entry = Entry.builder().path("test.css").entityType(type).rawContent(
                Arrays.asList(StringUtils.delimitedListToStringArray(content, System.lineSeparator()))).build();

        runner.setEntry(entry);
        runner.run();

        return entry;
    }
}
