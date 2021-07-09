/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.annotations;

public enum AnnotationType {

    ALLOW("sldsValidatorAllow"), // enables recommendation validation
    IGNORE("sldsValidatorIgnore"), // disables recommendation validation
    IGNORE_NEXT_LINE("sldsValidatorIgnoreNextLine"), // disables recommendation validation only for the next immediate line (even if an element is defined across multiple lines)
    WARN("sldsValidatorWarn"),
    NONE(null);

    private final String value;

    AnnotationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public boolean validate() {
        return this != IGNORE && this != IGNORE_NEXT_LINE;
    }

}