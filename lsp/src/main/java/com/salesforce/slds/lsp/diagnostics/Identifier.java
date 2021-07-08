/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.diagnostics;

public class Identifier {

    public static final String SOURCE = "SLDS";

    public enum DiagnosticCode {
        INVALID_TOKENS,
        ALTERNATIVE_TOKENS,
        UTILITY_TOKENS,
        COMPONENT_OVERRIDE,
        MOBILE_SLDS,
        DEFAULT;

        public String toString() {
            return String.valueOf(this.ordinal());
        }

        public static DiagnosticCode getCode(String value) {
            for (DiagnosticCode code : values()) {
                if (String.valueOf(code.ordinal()).contentEquals(value)) {
                    return code;
                }
            }

            return DEFAULT;
        }
    }
}