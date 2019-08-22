/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.aggregators;

import com.google.common.base.Equivalence;
import com.salesforce.slds.shared.models.recommendation.Recommendation;

public class AggregatorWithEquivalence extends Aggregator {

    private final Equivalence<Recommendation> equivalence;

    public AggregatorWithEquivalence(Equivalence<Recommendation> equivalence) {
        this.equivalence = equivalence;
    }

    public Equivalence<Recommendation> getEquivalence() {
        return this.equivalence;
    }
}
