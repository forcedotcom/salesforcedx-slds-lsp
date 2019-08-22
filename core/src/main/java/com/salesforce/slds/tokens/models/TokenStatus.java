
/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.tokens.models;

public enum TokenStatus {

    ACTIVE_NEW("active-new"),
    ACTIVE("active"),
    ACTIVE_CHANGED("active-changed"),
    DEPRECATED("deprecated");
    private final String value;

    TokenStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TokenStatus fromValue(String v) {
        for (TokenStatus c: TokenStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
