/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.context;

import java.util.Optional;

public enum ContextKey {
    GLOBAL,
    BEM,
    DENSITY,
    DEPRECATED,
    INVALID,
    OVERRIDE,
    UTILITY_CLASS,
    DESIGN_TOKEN,
    SLDS_MOBILE_VALIDATION;

    public static Optional<ContextKey> get(String name) {
        for (ContextKey key : values()) {
            if (key.name().contentEquals(name)) {
                return Optional.of(key);
            }
        }

        return Optional.empty();
    }
}
