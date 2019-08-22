/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters;

import com.salesforce.slds.shared.converters.colors.ColorType;
import com.salesforce.slds.shared.converters.numbers.NumberType;
import com.salesforce.slds.shared.converters.tokens.TokenType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Component
public class TypeConverters extends Converter {

    private List<Type> availableTypes = new ArrayList<>();

    public TypeConverters() {
        this(ColorType.get(), NumberType.get(), TokenType.get());
    }

    public TypeConverters(List<Type> ... typeProviders) {
        for (List<Type> types : typeProviders) {
            availableTypes.addAll(types);
        }
    }

    @Override
    public State process(State state) {
        for (Type type : availableTypes) {
            Matcher matcher = type.match(state);

            while(matcher.find()) {
                state = type.process(matcher, state);
            }
        }

        return state;
    }
}
