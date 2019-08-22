/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.lsp.models;

import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.override.ComponentOverride;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.lsp4j.Diagnostic;

import java.util.List;

public class DiagnosticResult {

    private final Diagnostic diagnostic;
    private final Recommendation recommendation;
    private final ComponentOverride componentOverride;
    private final List<Item> items;
    private final Entry.EntityType entityType;

    public DiagnosticResult(Diagnostic diagnostic, Recommendation recommendation, ComponentOverride componentOverride,
                            Entry.EntityType entityType, List<Item> items) {
        this.diagnostic = diagnostic;
        this.recommendation = recommendation;
        this.items = items;
        this.componentOverride = componentOverride;
        this.entityType = entityType;
    }

    public Diagnostic getDiagnostic() {
        return this.diagnostic;
    }

    public List<Item> getItems() {
        return this.items;
    }

    public Recommendation getRecommendation() {
        return this.recommendation;
    }

    public ComponentOverride getComponentOverride() {
        return componentOverride;
    }

    public Entry.EntityType getEntityType() {
        return this.entityType;
    }

    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.JSON_STYLE);
        b.append("diagnostic", this.diagnostic);
        b.append("items", this.items);
        b.append("recommendation", this.recommendation);
        b.append("componentOverride", this.componentOverride);
        b.append("entityType", this.entityType);
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DiagnosticResult that = (DiagnosticResult) o;

        return new EqualsBuilder()
                .append(getDiagnostic(), that.getDiagnostic())
                .append(getRecommendation(), that.getRecommendation())
                .append(getComponentOverride(), that.getComponentOverride())
                .append(getItems(), that.getItems())
                .append(getEntityType(), that.getEntityType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getDiagnostic())
                .append(getRecommendation())
                .append(getComponentOverride())
                .append(getEntityType())
                .append(getItems())
                .toHashCode();
    }
}