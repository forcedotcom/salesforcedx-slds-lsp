/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.services;

import com.salesforce.slds.lsp.registries.TextDocumentRegistry;
import com.salesforce.slds.lsp.services.interfaces.StateService;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StateServiceImpl implements StateService {

    final Context context = new Context();

    @Autowired
    TextDocumentRegistry textDocumentRegistry;

    @Override
    public void updateState(StateService.StateParams stateParams) {
        String key = stateParams.getKey();

        context.setState(key, stateParams.getValue());

        if (context.isEnabled(ContextKey.GLOBAL) == false) {
            textDocumentRegistry.clear();
        }
    }

    @Override
    public boolean isEnabled(ContextKey key) {
        return context.isEnabled(key);
    }

    @Override
    public Context getContext() {
        return this.context;
    }
}
