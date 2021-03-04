/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.diagnostics;

import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import org.eclipse.lsp4j.TextDocumentItem;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DiagnoserTests {

    @Mock
    TextDocumentRegistry textDocumentRegistry;

    Diagnoser diagnoser;

    private static String STYLE_FAVOR = "styleFavor.html";
    private static String STYLE = "style.html";
    private static String IN_MEMORY_CONTENT = "<div> in memory </div>";
    private static String FILE_PROTOCOL = "file:/";
    private static String PREFIX = "/diagnostics/";

    @BeforeEach
    public void setup() {
        URL favor = DiagnoserTests.class.getResource(PREFIX + STYLE_FAVOR);
        TextDocumentItem item = new TextDocumentItem();
        item.setUri(favor.toString());
        item.setText(IN_MEMORY_CONTENT);

        when(textDocumentRegistry.get(anyString())).thenAnswer(invocationOnMock -> {
          if (invocationOnMock.getArguments()[0].equals(favor.toURI().toString())) {
              return item;
          }

          return null;
        });

        diagnoser = new Diagnoser();
        diagnoser.documentRegistry = textDocumentRegistry;
    }

    @Test
    public void bundle() throws IOException {
        URL resource = DiagnoserTests.class.getResource(PREFIX + "style.css");
        File f = new File(resource.getFile());

        TextDocumentItem item = new TextDocumentItem();
        item.setUri(f.toURI().toString());


        Bundle bundle = diagnoser.getBundle(item);

        assertThat(bundle.getEntries(), Matchers.hasSize(3));

        Optional<Entry> actualStyleEntry = getEntry(bundle, STYLE);
        assertStyle(actualStyleEntry);

        Optional<Entry> actualStyleFavorEntry = getEntry(bundle, STYLE_FAVOR);
        assertStyleFavor(actualStyleFavorEntry);
    }

    private void assertStyle(Optional<Entry> entry) {
        assertThat(entry.isPresent(), Matchers.is(true));

        assertThat(entry.get().getRawContent(), Matchers.hasSize(3));
        assertThat(entry.get().getPath(), Matchers.startsWith(FILE_PROTOCOL));
    }

    private void assertStyleFavor(Optional<Entry> entry) {
        assertThat(entry.isPresent(), Matchers.is(true));

        List<String> rawContent = new ArrayList<>();
        rawContent.add(IN_MEMORY_CONTENT);

        assertThat(entry.get().getRawContent(), Matchers.is(rawContent));
        assertThat(entry.get().getPath(), Matchers.startsWith(FILE_PROTOCOL));
    }

    private Optional<Entry> getEntry(Bundle bundle, String name) {
        return bundle.getEntries().stream()
                .filter(entry -> entry.getPath().endsWith(name))
                .findAny();
    }
}
