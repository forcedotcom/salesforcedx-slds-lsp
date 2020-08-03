/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators;

import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.registry.TokenRegistry;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.core.RuleSet;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.utils.CSSValidationUtilities;
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseValidator implements RecommendationValidator, InitializingBean {

    @Autowired
    TokenRegistry registry;

    @Autowired
    CSSValidationUtilities utilities;

    protected abstract List<String> getCategories();

    protected abstract List<String> getProperties();

    private Set<DesignToken> DESIGN_TOKENS = new LinkedHashSet<>();

    @Override
    public void afterPropertiesSet() {
        if (getCategories().contains("*")) {
            registry.getDesignTokenCategories().forEach(category ->
                    DESIGN_TOKENS.addAll(registry.getDesignTokensFromCategory(category)));
        } else {
            registry.getDesignTokenCategories().stream().filter(key -> getCategories().contains(key))
                    .forEach(key -> DESIGN_TOKENS.addAll(registry.getDesignTokensFromCategory(key)));
        }
    }

    @Override
    public List<Recommendation> matches(Entry entry, Context context) {
        if (context.isEnabled(ContextKey.DESIGN_TOKEN)) {
            return entry.getInputs().stream()
                    .filter(input -> input.getType() == Input.Type.STYLE)
                    .map(Input::asRuleSet)
                    .map(RuleSet::getStylesWithAnnotationType)
                    .flatMap(List::stream)
                    .filter(style -> utilities.filter(style, getProperties()))
                    .map(style -> utilities.match(style, new ArrayList<>(DESIGN_TOKENS), entry.getEntityType(), entry.getRawContent()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}