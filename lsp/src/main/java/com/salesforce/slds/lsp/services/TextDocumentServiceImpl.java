package com.salesforce.slds.lsp.services;

import com.salesforce.slds.lsp.codeactions.CodeActionConverter;
import com.salesforce.slds.lsp.diagnostics.Diagnoser;
import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.lsp.services.interfaces.StateService;
import com.salesforce.slds.shared.models.context.ContextKey;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
public class TextDocumentServiceImpl implements TextDocumentService {

    @Autowired
    Diagnoser diagnoser;

    @Autowired
    StateService stateService;

    @Autowired
    CodeActionConverter codeActionConverter;

    @Autowired
    TextDocumentRegistry registry;

    private LanguageClient client;

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
        return null;
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        return stateService.isEnabled(ContextKey.GLOBAL) ?
                CompletableFuture.completedFuture(codeActionConverter.convert(params)) :
                CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem item) {
       return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        registry.register(params.getTextDocument());
        if (stateService.isEnabled(ContextKey.GLOBAL)) {
            diagnose(params.getTextDocument());
        } else {
            client.publishDiagnostics(
                    new PublishDiagnosticsParams(params.getTextDocument().getUri(),
                            new ArrayList<>()));
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        TextDocumentItem item = registry.get(uri);

        if (item == null) {
            return;
        }

        if (stateService.isEnabled(ContextKey.GLOBAL)) {
            for (TextDocumentContentChangeEvent changeEvent : params.getContentChanges()) {
                // Will be full update because we specified that is all we support
                item.setText(changeEvent.getText());
            }

            diagnose(item);
        } else {
            client.publishDiagnostics(
                    new PublishDiagnosticsParams(item.getUri(), new ArrayList<>()));
        }
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        registry.remove(uri);
    }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {

    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return null;
    }

    public void setClient(LanguageClient languageClient) {
        this.client = languageClient;
    }

    private void diagnose(TextDocumentItem item) {
        diagnoser.diagnose(item);

        client.publishDiagnostics(
            new PublishDiagnosticsParams(item.getUri(), 
            registry.getDiagnostics(item.getUri())));
    }
}