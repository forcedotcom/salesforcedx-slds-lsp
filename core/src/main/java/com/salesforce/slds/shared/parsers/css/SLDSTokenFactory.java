/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.parsers.css;

import com.salesforce.omakase.parser.factory.BaseTokenFactory;
import com.salesforce.omakase.parser.factory.TokenFactory;
import com.salesforce.omakase.parser.token.Token;
import com.salesforce.omakase.parser.token.Tokens;
import com.salesforce.omakase.plugin.GrammarPlugin;

import java.util.Optional;

public class SLDSTokenFactory extends BaseTokenFactory {

    private static final Token SPECIAL_DECLARATION = Tokens.STAR.or(Tokens.HYPHEN);

    @Override
    public Optional<Token> specialDeclarationBegin() {
        // to allow for the the IE7 star hack - http://en.wikipedia.org/wiki/CSS_filter#Star_hack
        // it's not part of the CSS spec, but it still needs to be handled
        return Optional.of(SPECIAL_DECLARATION);
    }


    public static class SLDSGrammarPlugin implements GrammarPlugin {

        @Override
        public TokenFactory getTokenFactory() {
            return new SLDSTokenFactory();
        }
    }

}
