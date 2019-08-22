/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.configuration;

import com.salesforce.slds.configuration.SldsConfiguration;

import com.salesforce.slds.lsp.Server;
import com.salesforce.slds.lsp.codeactions.CodeActionConverter;
import com.salesforce.slds.lsp.diagnostics.Diagnoser;
import com.salesforce.slds.lsp.diagnostics.DiagnosticConverter;
import com.salesforce.slds.lsp.registries.DiagnosticResultRegistry;
import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.lsp.services.StateServiceImpl;
import com.salesforce.slds.lsp.services.TextDocumentServiceImpl;
import com.salesforce.slds.lsp.services.WorkspaceServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SldsConfiguration.class, CodeActionConverter.class,
        DiagnosticConverter.class, Diagnoser.class,
        DiagnosticResultRegistry.class, TextDocumentRegistry.class,
        TextDocumentServiceImpl.class, WorkspaceServiceImpl.class,
        Server.class, StateServiceImpl.class
})
public class ServerConfiguration { }