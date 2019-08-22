/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.interfaces;

import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.override.ComponentOverride;

import java.util.List;

public interface OverrideValidator extends Validator {

    List<ComponentOverride> getOverrides(Entry entry, Context context);
}
