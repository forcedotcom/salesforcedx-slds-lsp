/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.utils;

import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ActionUtilities {

    public Item converts(DesignToken token, String value, List<Range> ranges) {
        Set<Action> actions = ranges.stream()
                .map(range -> converts(token, range))
                .collect(Collectors.toSet());

        Item returnValue =  new Item(value.trim());
        returnValue.setActions(actions);
        return returnValue;
    }

    public Action converts(DesignToken token, Range range) {
        return Action.builder()
                .name(token.getName())
                .value(token.getValue())
                .cssProperties(token.getCssProperties())
                .range(range)
                .description(token.getComment())
                .actionType(ActionType.REPLACE).build();
    }

}
