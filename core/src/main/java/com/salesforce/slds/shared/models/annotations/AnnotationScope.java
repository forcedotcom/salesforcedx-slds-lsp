/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.annotations;

public enum AnnotationScope {

    INLINE("inline"), BLOCK("block");

    private final String value;

    AnnotationScope(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
