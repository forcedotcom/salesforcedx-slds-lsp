/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.tokens.registry;

import com.salesforce.slds.tokens.models.ComponentBlueprint;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.models.UtilityClass;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TokenRegistry {

    Set<ComponentBlueprint> getComponentBlueprints();

    Optional<ComponentBlueprint> getComponentBlueprint(String component);

    Optional<DesignToken> getDesignToken(String key);

    List<DesignToken> getDesignTokensFromCategory(String category);

    Set<String> getDesignTokenCategories();

    List<UtilityClass> getUtilityClasses();

    List<DesignToken> getDesignTokens();

}
