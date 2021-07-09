/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.configuration;

import com.salesforce.slds.validation.aggregators.SimpleAggregator;
import com.salesforce.slds.validation.processors.SortAndFilterProcessor;
import com.salesforce.slds.validation.runners.ValidateRunner;
import com.salesforce.slds.validation.utils.ActionUtilities;
import com.salesforce.slds.validation.utils.CSSValidationUtilities;
import com.salesforce.slds.validation.utils.JavascriptValidationUtilities;
import com.salesforce.slds.validation.utils.MarkupValidationUtilities;
import com.salesforce.slds.validation.validators.ValidatorFactories;
import com.salesforce.slds.validation.validators.impl.override.ComponentOverrideValidator;
import com.salesforce.slds.validation.validators.impl.recommendation.*;
import com.salesforce.slds.validation.validators.impl.recommendation.DesignTokenValidator;
import com.salesforce.slds.validation.validators.impl.recommendation.InvalidValidator;
import com.salesforce.slds.validation.validators.impl.recommendation.PriorityValidator;
import com.salesforce.slds.validation.validators.impl.recommendation.UtilityClassValidator;
import com.salesforce.slds.validation.validators.utils.HTMLElementUtilities;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SimpleAggregator.class, SortAndFilterProcessor.class,
        ActionUtilities.class, CSSValidationUtilities.class,
        JavascriptValidationUtilities.class, MarkupValidationUtilities.class,
        InvalidValidator.class, PriorityValidator.class,
        UtilityClassValidator.class, ValidatorFactories.class,
        ValidateRunner.class, ComponentOverrideValidator.class,
        HTMLElementUtilities.class, DesignTokenValidator.class,
        MobileSLDS_MarkupFriendlyValidator.class,
        MobileSLDS_MarkupLabelValidator.class,
        MobileSLDS_CSSValidator.class
})
public class ValidationConfiguration {
}
