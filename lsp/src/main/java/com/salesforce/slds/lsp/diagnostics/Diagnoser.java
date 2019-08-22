/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.diagnostics;

import com.salesforce.slds.lsp.models.DiagnosticResult;
import com.salesforce.slds.lsp.registries.DiagnosticResultRegistry;
import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.lsp.services.interfaces.StateService;
import com.salesforce.slds.validation.aggregators.SimpleAggregator;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.validation.processors.SortAndFilterProcessor;
import com.salesforce.slds.validation.runners.ValidateRunner;
import com.salesforce.slds.validation.validators.interfaces.Validator;
import org.eclipse.lsp4j.TextDocumentItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@Component
public class Diagnoser {

    @Autowired
    @Lazy
    List<Validator> validators;

    @Autowired
    DiagnosticConverter converter;

    @Autowired
    TextDocumentRegistry documentRegistry;

    @Autowired
    DiagnosticResultRegistry diagnosticRegistry;

    @Autowired
    StateService stateService;

    public void diagnose(TextDocumentItem item) {
        diagnosticRegistry.remove(item.getUri());

        try {
            ValidateRunner runner = 
            new ValidateRunner(validators, new SimpleAggregator(), new SortAndFilterProcessor());

            runner.setContext(stateService.getContext());

            Entry entry = createEntry(item);
            entry.setBundle(getBundle(item));

            runner.setEntry(entry);
            runner.run();

            List<DiagnosticResult> diagnostics = converter.convert(entry);
            diagnosticRegistry.put(item.getUri(), diagnostics);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Entry createEntry(TextDocumentItem item) {
        return createEntry(item.getUri(),
                    Arrays.asList(StringUtils.delimitedListToStringArray(item.getText(), System.lineSeparator())));
    }

    private Entry createEntry(String path, List<String> rawContents) {
        return Entry.builder().path(path).rawContent(rawContents).build();
    }

    public Bundle getBundle(TextDocumentItem entry) throws IOException {
        Bundle bundle = new Bundle();

        File originalFile = new File(URI.create(entry.getUri()));

        for (File f : originalFile.getParentFile().listFiles()) {
            if (f.isFile() && f.equals(originalFile) == false) {
                TextDocumentItem item = documentRegistry.get(f.toURI().toString());
                if (item != null) {
                    bundle.getEntries().add(createEntry(item));
                } else {
                    bundle.getEntries().add(createEntry( f.toURI().toString(), Files.readAllLines(f.toPath())));
                }
            }
        }

        return bundle;
    }

}