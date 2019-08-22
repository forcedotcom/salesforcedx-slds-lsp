/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.services.interfaces;

import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

@JsonSegment("state")
public interface StateService {

    class StateParams {
        String key;
        boolean value;

        public String getKey() {
            return this.key;
        }

        public boolean getValue() {
            return this.value;
        }
    }

    @JsonNotification
    default void updateState(StateParams state) {}


    boolean isEnabled(ContextKey key);

    Context getContext();
}
