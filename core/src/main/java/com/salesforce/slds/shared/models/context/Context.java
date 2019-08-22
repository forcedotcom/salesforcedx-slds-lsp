/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Context {

    private final Map<ContextKey, Boolean> state = new HashMap<>();

    public Context() {
        for(ContextKey key : ContextKey.values()) {
            state.put(key, Boolean.TRUE);
        }
    }

    public void setState(String name, boolean value) {
        Optional<ContextKey> key = ContextKey.get(name);
        if (key.isPresent()) {
            state.put(key.get(), Boolean.valueOf(value));
        }
    }

    public boolean isEnabled(String name) {
        Optional<ContextKey> key = ContextKey.get(name);
        return key.isPresent() ? state.get(key.get()) : false;
    }

    public boolean isEnabled(ContextKey key) {
        return state.getOrDefault(key, false);
    }
}