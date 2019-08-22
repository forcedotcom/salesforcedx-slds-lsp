/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

import com.salesforce.slds.validation.validators.BaseValidator;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE;

public class TokenValidator extends BaseValidator {

    final List<String> categories;
    final List<String> properties;

    public TokenValidator(List<String> categories, Set<String> properties) {
        this.categories = categories;
        this.properties = new ArrayList<>(properties);
    }

    @Override
    protected List<String> getCategories() {
        return this.categories;
    }

    @Override
    protected List<String> getProperties() {
        return this.properties;
    }

    @Override
    public String toString() {
        return new ToStringBuilder( this, JSON_STYLE)
                .append("categories", categories)
                .toString();
    }
}
