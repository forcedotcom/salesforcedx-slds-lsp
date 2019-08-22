/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.tokens.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

public class UtilityClass {

    private String name;
    private List<Setting> settings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public void setSettings(List<Setting> settings) {
        this.settings = settings;
    }

    public String displayAsBlock() {
        try {
            Map<String, String> props = new LinkedHashMap<>();

            settings.forEach(setting ->
                props.put(setting.getProperty(), setting.getValue())
            );

            return new ObjectMapper().writeValueAsString(props);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Setting {
        private String property;
        private String value;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Setting setting = (Setting) o;

            return new EqualsBuilder()
                    .append(getProperty(), setting.getProperty())
                    .append(getValue(), setting.getValue())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(getProperty())
                    .append(getValue())
                    .toHashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        UtilityClass that = (UtilityClass) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
                .append(getSettings(), that.getSettings())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getSettings())
                .toHashCode();
    }
}
