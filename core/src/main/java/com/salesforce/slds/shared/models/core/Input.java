/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.models.core;

public abstract class Input {

    public enum Type {MARKUP, JAVASCRIPT, STYLE}

    public abstract Type getType();

    public HTMLElement asElement() {
        if (this instanceof HTMLElement) {
            return (HTMLElement)this;
        }

        return null;
    }

    public RuleSet asRuleSet() {
        if (this instanceof RuleSet) {
            return (RuleSet)this;
        }

        return null;
    }

    public Block asBlock() {
        if (this instanceof Block) {
            return (Block)this;
        }

        return null;
    }

    public Style asStyle() {
        if (this instanceof Style) {
            return (Style)this;
        }

        return null;
    }
}
