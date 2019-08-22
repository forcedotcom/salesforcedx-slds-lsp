/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters.colors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.salesforce.slds.shared.converters.Converter;
import com.salesforce.slds.shared.utils.ResourceUtilities;

import java.awt.*;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.salesforce.slds.shared.RegexPattern.WORD_FRAGMENT;

public class NamedColorType extends ColorType {

    private static final Pattern pattern = Pattern.compile(WORD_FRAGMENT);

    private static final Map<String, String> COLOR_NAMES = getColorNames();

    @Override
    public Matcher match(Converter.State state) {
        return pattern.matcher(state.getInput());
    }

    @Override
    public Converter.State process(Matcher matcher, Converter.State state) {
        String name = matcher.group();

        String hex = COLOR_NAMES.get(name.toLowerCase());

        if (hex != null) {
            Color color = Color.decode(hex);
            state = addColor(matcher, color, state);
        }

        return state;
    }

    private static Map<String, String> getColorNames() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        ResourceUtilities.getResources(NamedColorType.class,"/shared/converters/colors.json")
                .forEach(path -> {
                    try (InputStream inputStream = NamedColorType.class.getResourceAsStream(path)) {
                        ObjectMapper mapper = new ObjectMapper();
                        builder.putAll((Map) mapper.readValue(inputStream,
                                new TypeReference<Map<String, String>>() {}));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        return builder.build();
    }
}
