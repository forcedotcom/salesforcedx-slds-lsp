/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.models;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class DiagnosticBuilder {
    private String source;
    private DiagnosticSeverity severity;
    private String message;
    private Position start;
    private Position end;
    private String code;

    public DiagnosticBuilder source(String source) {
        this.source = source;
        return this;
    }

    public DiagnosticBuilder severity(DiagnosticSeverity severity) {
        this.severity = severity;
        return this;
    }

    public DiagnosticBuilder message(String message) {
        this.message = message;
        return this;
    }

    public DiagnosticBuilder start(int line, int character) {
        this.start = new Position(line, character);
        return this;
    }

    public DiagnosticBuilder end(int line, int character) {
        this.end = new Position(line, character);
        return this;
    }

    public DiagnosticBuilder code(String code) {
        this.code = code;
        return this;
    }

    public Diagnostic build() {
        Range range = new Range(start, end);
        return new Diagnostic(range, message, severity, source, code);
    }
}