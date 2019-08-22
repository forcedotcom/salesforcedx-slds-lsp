/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.slds.shared.converters.tokens;

import com.google.common.collect.ImmutableList;
import com.salesforce.slds.shared.converters.Type;

import java.util.List;

public class TokenType {

    private static final List<Type> TYPES =
            ImmutableList.of(new AuraTokenType(), new VarTokenType());

    public static List<Type> get() {
        return TYPES;
    }

}
