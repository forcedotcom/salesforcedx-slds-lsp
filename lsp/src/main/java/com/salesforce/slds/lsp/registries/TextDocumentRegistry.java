/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.registries;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.salesforce.slds.lsp.models.DiagnosticResult;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextDocumentItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TextDocumentRegistry {
 
    private final HashMap<String, TextDocumentItem> documents = new HashMap<>();

    @Autowired
    DiagnosticResultRegistry diagnosticRegistry;

    public void register(TextDocumentItem item) {
        this.documents.put(translate(item.getUri()), item);
    }

    public TextDocumentItem get(String uri) {
        return this.documents.get(translate(uri));
    }

    public void remove(String uri) {
        documents.remove(uri);
        diagnosticRegistry.remove(uri);
    }

    public List<Diagnostic> getDiagnostics(String uri) {
        return diagnosticRegistry.getDiagnostics(uri);
    }

    public List<DiagnosticResult> getDiagnosticResults(String uri) {
        return diagnosticRegistry.getDiagnosticResults(uri);
    }

    private String translate(String uri) {
        return new File(uri).toURI().toString();
    }

    public void clear() {
        diagnosticRegistry.clear();
    }
}