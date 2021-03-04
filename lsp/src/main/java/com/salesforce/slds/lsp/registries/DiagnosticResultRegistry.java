/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.registries;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.salesforce.slds.lsp.models.DiagnosticResult;

import org.eclipse.lsp4j.Diagnostic;
import org.springframework.stereotype.Component;

@Component
public class DiagnosticResultRegistry {

    private final Map<String, List<DiagnosticResult>> results = new HashMap<>();

    public List<DiagnosticResult> remove(String uri) {
        return results.remove(translate(uri));
    }

    public void put(String uri, List<DiagnosticResult> diagnostics) {
        this.results.put(translate(uri), diagnostics);
    }

    List<Diagnostic> getDiagnostics(String uri) {
        String key = translate(uri);

        if (results.containsKey(key)) {
            return results.get(key).stream()
                    .map(DiagnosticResult::getDiagnostic).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    List<DiagnosticResult> getDiagnosticResults(String uri) {
        String key = translate(uri);

        if (results.containsKey(key)) {
            return results.get(key);
        }
        return new ArrayList<>();
    }

    private String translate(String uri) {
        try {
            URI result = new URI(uri);
            return result.toString();
        } catch (URISyntaxException e) {
            return new File(uri).toURI().toString();
        }
    }

    void clear() {
        this.results.clear();
    }
}