/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.diagnostics;

import com.google.common.collect.ImmutableList;
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

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

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

            Bundle bundle = getBundle(item);
            runner.setBundle(bundle);

            runner.run();

            /**
             * Handles URI variation between different FileSystem format.
             * `file:///c%3A/...` - Windows OS
             * `file:/c:/...` - Windows OS
             * `file:///...` - Mac OS
             * `file:/...` - Mac OS
             */
            File itemFile = new File(URI.create(item.getUri()).getPath());

            Optional<Entry> result = bundle.getEntries().stream()
                    .filter(entry -> new File(URI.create(entry.getPath()).getPath()).equals(itemFile)).findFirst();

            List<DiagnosticResult> diagnostics = converter.convert(result.get());
            diagnosticRegistry.put(item.getUri(), diagnostics);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Entry createEntry(TextDocumentItem item) {
        //item.getText() uses "\n" as LineSeparator regardless as OS
        return createEntry(item.getUri(),
                    Arrays.asList(StringUtils.delimitedListToStringArray(item.getText(), "\n")));
    }

    private Entry createEntry(String path, List<String> rawContents) {
        return Entry.builder().path(path).rawContent(rawContents).build();
    }

    public Bundle getBundle(TextDocumentItem entry) throws IOException {
        Bundle bundle = new Bundle();

        File originalFile = new File(URI.create(entry.getUri()));

        for (File f : originalFile.getParentFile().listFiles(Diagnoser::isLightningComponentFiles)) {
            if (f.isFile()) {
                TextDocumentItem item = documentRegistry.get(f.toURI().toString());
                if (item != null) {
                    bundle.getEntries().add(createEntry(item));
                } else {
                    bundle.getEntries().add(createEntry(f.toURI().toString(), Files.readAllLines(f.toPath())));
                }
            }
        }

        return bundle;
    }

    private static boolean isLightningComponentFiles(File file) {
        String fileName = file.getName();
        int position = fileName.lastIndexOf('.');
        if (position != -1) {
            String fileExtension = fileName.substring(position);
            return SUPPORTED_FILE_EXTENSIONS.contains(fileExtension.toLowerCase());
        }

        return false;
    }

    private static List<String> SUPPORTED_FILE_EXTENSIONS = ImmutableList.of(".html", ".js", ".css", ".cmp", ".app");
}