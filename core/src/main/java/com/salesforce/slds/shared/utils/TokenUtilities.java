/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.utils;

import com.salesforce.slds.shared.RegexPattern;
import com.salesforce.slds.shared.converters.Converter;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.registry.TokenRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TokenUtilities {

    /**
     * TOKEN & VALUE REGEX PATTERN
     *
     * AURA: t(TOKEN_NAME)
     * LWC: var(--lwc-TOKEN_NAME), var(--lwc-TOKEN_NAME, TOKEN_VALUE)
     * Plain Value: TOKEN_VALUE
     */
    private static final String TOKEN_PATTERN = "(?:t\\(\\s*%s\\s*\\)|var\\(\\s*--lwc-%s\\s*(,\\s*%s\\s*)?\\)|%s)";

    @Autowired
    TokenRegistry tokenRegistry;

    @Autowired
    ValueUtilities valueUtilities;

    private static final Pattern pattern = Pattern.compile("\\$(?<name>"+ RegexPattern.WORD_FRAGMENT+")");
    private static final Pattern important = Pattern.compile("!important");

    public Set<String> generatePatterns(String value) {
        Converter.State state = Converter.State.builder().input(value).build();

        Matcher matcher = important.matcher(value);
        if (matcher.find()) {
            return generatePatterns(value, state);
        }

        matcher  = pattern.matcher(value);

        while(matcher.find()) {
            String name = matcher.group("name");
            Optional<DesignToken> token = tokenRegistry.getDesignToken(name);

            if (token.isPresent()) {
                state.addValues(matcher,
                        String.format(TOKEN_PATTERN, name, name, token.get().getValue(), token.get().getValue()));
            }
        }


        return generatePatterns(value, state);
    }

    Set<String> generatePatterns(String value, Converter.State state) {
        Set<String> patterns = valueUtilities.generatePossibleValues(value, state);
        patterns.add(value);

        return patterns
                .stream().map(pattern -> pattern.replaceAll("\\s", "\\\\s*"))
                .collect(Collectors.toSet());
    }
}
