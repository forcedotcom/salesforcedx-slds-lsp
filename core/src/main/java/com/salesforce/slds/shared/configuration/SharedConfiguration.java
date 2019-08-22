/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.salesforce.slds.shared.configuration;

import com.salesforce.slds.shared.converters.TypeConverters;
import com.salesforce.slds.shared.utils.TokenUtilities;
import com.salesforce.slds.shared.utils.ValueUtilities;
import com.salesforce.slds.tokens.configuration.TokensConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ValueUtilities.class, TokenUtilities.class, TokensConfiguration.class, TypeConverters.class})
public class SharedConfiguration {
}
