/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.recommendation;

import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.locations.RangeProvider;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class Action implements Comparable<Action>, RangeProvider {

    private final String name;
    private final String value;
    private final List<String> cssProperties;
    private final ActionType actionType;
    private final List<RelatedInformation> information;
    private final Range range;
    private final String description;
    private final Input.Type fileType;

    Action(String name, String value, Range range, ActionType actionType, String description,
           List<RelatedInformation> information, List<String> cssProperties, Input.Type fileType) {
        this.name = name;
        this.value = value;
        this.cssProperties = cssProperties;
        this.actionType = actionType;
        this.information = information;
        this.range = range;
        this.description = description;
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public List<String> getCssProperties() {
        return cssProperties;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public List<RelatedInformation> getInformation() {
        return this.information;
    }

    @Override
    public Range getRange() {
        return this.range;
    }

    public String getDescription() {
        return this.description;
    }

    public Optional<Input.Type> getFileType() {
        return Optional.ofNullable(fileType);
    }

    public static ActionBuilder  builder() {
        return new ActionBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;

        return new EqualsBuilder()
                .append(getName(), action.getName())
                .append(getValue(), action.getValue())
                .append(getCssProperties(), action.getCssProperties())
                .append(getActionType(), action.getActionType())
                .append(getInformation(), action.getInformation())
                .append(getRange(), action.getRange())
                .append(getDescription(), action.getDescription())
                .append(getFileType(), action.getFileType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getValue())
                .append(getCssProperties())
                .append(getActionType())
                .append(getInformation())
                .append(getRange())
                .append(getDescription())
                .append(getFileType())
                .toHashCode();
    }

    @Override
    public int compareTo(Action o) {
        if (this.equals(o)) {
            return 0;
        }

        if (actionType == ActionType.REPLACE) {
            if (o.actionType == ActionType.REMOVE) {
                return 1;
            }
        }

        if (actionType == ActionType.REMOVE) {
            if (o.actionType == ActionType.REPLACE) {
                return -1;
            }
        }

        int rangeResult = range.compareTo(o.range);

        if (rangeResult != 0) {
            return rangeResult;
        }

        return getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, JSON_STYLE)
                .append("name", name)
                .append("value", value)
                .append("cssProperties", cssProperties)
                .append("type", actionType)
                .append("information", information)
                .append("range", range)
                .append("description", description)
                .append("fileType", fileType)
                .toString();
    }

    public static class ActionBuilder {
        private String name;
        private String value;
        private List<String> cssProperties;
        private ActionType actionType;
        private List<RelatedInformation> information;
        private Range range;
        private String description;
        private Input.Type fileType;

        public ActionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ActionBuilder value(String value) {
            this.value = value;
            return this;
        }

        public ActionBuilder range(Range range) {
            this.range = range;
            return this;
        }

        public ActionBuilder cssProperties(List<String> cssProperties) {
            this.cssProperties = cssProperties;
            return this;
        }

        public ActionBuilder actionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public ActionBuilder relatedInformation(List<RelatedInformation> information) {
            this.information = information;
            return this;
        }

        public ActionBuilder description(String description){
            this.description = description;
            return this;
        }

        public ActionBuilder fileType(Input.Type fileType){
            this.fileType = fileType;
            return this;
        }

        public Action build() {
            return new Action(name, value, range, actionType, description, information, cssProperties, fileType);
        }
    }

}