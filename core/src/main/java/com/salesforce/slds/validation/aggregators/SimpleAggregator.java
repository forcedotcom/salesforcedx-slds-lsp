/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.aggregators;

import com.google.common.base.Equivalence;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class SimpleAggregator extends Aggregator {

    public Equivalence<Recommendation> getEquivalence(){
        return EQUIVALENCE;
    }

    private static final Equivalence<Recommendation> EQUIVALENCE = new Equivalence<Recommendation>() {
        @Override
        protected boolean doEquivalent(Recommendation a, Recommendation b) {
            return a.equals(b);
        }

        @Override
        protected int doHash(Recommendation recommendation) {
            return recommendation.hashCode();
        }
    };
}
