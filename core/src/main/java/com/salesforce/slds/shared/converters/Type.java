/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters;

import java.util.regex.Matcher;

public interface Type {

    Matcher match(Converter.State state);

    Converter.State process(Matcher matcher, Converter.State state);
}
