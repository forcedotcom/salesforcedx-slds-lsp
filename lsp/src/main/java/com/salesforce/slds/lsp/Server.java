package com.salesforce.slds.lsp;

import com.google.common.collect.Lists;
import com.salesforce.slds.lsp.diagnostics.Diagnoser;
import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.lsp.services.TextDocumentServiceImpl;
import com.salesforce.slds.lsp.services.interfaces.StateService;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.utils.EntryUtilities;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
public class Server implements LanguageServer, LanguageClientAware {

    @Autowired
    TextDocumentServiceImpl textDocumentService;

    @Autowired
    WorkspaceService workspaceService;

    @Autowired
    StateService stateService;

    @Autowired
    Diagnoser diagnoser;

    @Autowired
    TextDocumentRegistry documentRegistry;

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setCodeActionProvider(new CodeActionOptions(Lists.newArrayList(CodeActionKind.Refactor,
                CodeActionKind.QuickFix)));
        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void exit() {

    }

    @JsonDelegate
    public StateService getStateService() {
        return stateService;
    }

    @JsonRequest
    CompletableFuture<String> provideContext(TextDocumentIdentifier params) {
        TextDocumentItem item = documentRegistry.get(params.getUri());

        try {

            Entry entry = diagnoser.createEntry(item);
            entry.setBundle(diagnoser.getBundle(item));

            Entry.EntityType type = EntryUtilities.getType(entry);

            return CompletableFuture.completedFuture(type == Entry.EntityType.AURA ? "aura" : "other");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture("other");
    }

    @Override
    public void connect(LanguageClient languageClient) {
        this.textDocumentService.setClient(languageClient);
    }
}