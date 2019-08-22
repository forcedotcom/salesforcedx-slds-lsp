/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters.tokens;

import com.salesforce.slds.shared.converters.Converter;
import com.salesforce.slds.shared.converters.Type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.salesforce.slds.shared.RegexPattern.AURA_TOKEN_FUNCTION;

public class AuraTokenType implements Type {

    private static final Pattern pattern = Pattern.compile(AURA_TOKEN_FUNCTION, Pattern.CASE_INSENSITIVE);


    @Override
    public Matcher match(Converter.State state) {
        return pattern.matcher(state.getInput());
    }

    @Override
    public Converter.State process(Matcher matcher, Converter.State state) {
        state.addValues(matcher, matcher.group("token"));

        return state;
    }
}
