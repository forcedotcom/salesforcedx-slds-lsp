/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.models;

import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.models.TokenPriority;
import com.salesforce.slds.tokens.models.TokenType;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Properties {

    private List<Item> items;

    public static class Item {
        private String name;
        private String category;
        private TokenType tokenType;
        private List<String> properties;
        private Map<String, String> tokens;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public TokenType getTokenType() {
            return tokenType;
        }

        public void setTokenType(TokenType tokenType) {
            this.tokenType = tokenType;
        }

        public List<String> getProperties() {
            return properties;
        }

        public void setProperties(List<String> properties) {
            this.properties = properties;
        }

        public Map<String, String> getTokens() {
            return tokens;
        }

        public void setTokens(Map<String, String> tokens) {
            this.tokens = tokens;
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<DesignToken> asTokens() {
        return asTokens(item -> true);
    }

    public List<DesignToken> asTokens(Predicate<Item> filter) {
        return items.stream()
                .filter(filter)
                .flatMap(item -> toTokenStream(item))
                .collect(Collectors.toList());
    }

    private Stream<DesignToken> toTokenStream(Item item) {
        Stream.Builder<DesignToken> builder = Stream.builder();

        item.getTokens().forEach((newValue, current) -> {
            DesignToken token = new DesignToken();
            token.setName(newValue);
            token.setValue(current);
            token.setTokenPriority(TokenPriority.HIGH);
            token.setTokenType(item.getTokenType());
            token.setCssProperties(item.getProperties());
            token.setCategory(item.getCategory());
            builder.add(token);
        });

        return builder.build();
    }
}
