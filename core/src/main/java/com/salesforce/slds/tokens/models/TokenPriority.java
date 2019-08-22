
/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.tokens.models;

public enum TokenPriority {

    HIGH,
    NORMAL;

    public String value() {
        return name();
    }

    public static TokenPriority fromValue(String v) {
        return valueOf(v);
    }

}
