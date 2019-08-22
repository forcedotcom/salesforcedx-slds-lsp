/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.slds.configuration;

import com.salesforce.slds.shared.configuration.SharedConfiguration;
import com.salesforce.slds.tokens.configuration.TokensConfiguration;
import com.salesforce.slds.validation.configuration.ValidationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SharedConfiguration.class, TokensConfiguration.class, ValidationConfiguration.class})
public class SldsConfiguration {
}