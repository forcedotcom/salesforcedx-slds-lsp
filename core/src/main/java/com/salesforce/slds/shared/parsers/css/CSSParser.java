/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.parsers.css;

import com.salesforce.omakase.Omakase;
import com.salesforce.omakase.ast.Rule;
import com.salesforce.omakase.plugin.core.SyntaxTree;
import com.salesforce.slds.shared.models.core.RuleSet;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CSSParser {

    public static List<RuleSet> parse(List<String> lines) {
        SyntaxTree tree = new SyntaxTree();

        Omakase.source(
                StringUtils.collectionToDelimitedString(lines, System.lineSeparator()))
                .use(new SLDSTokenFactory.SLDSGrammarPlugin(), tree).process();

        List<RuleSet> ruleSets = new ArrayList<>();

        for (Rule rule : tree.stylesheet().rules()) {
            Location start = new Location(rule.line() - 1, rule.column() - 1);
            Location end = findEndLocation(lines, start);

            ruleSets.add(
                    RuleSet.builder().rule(rule)
                            .range(new Range(start, end))
                            .raw(lines.subList(start.getLine(), end.getLine() + 1))
                            .build());
        }

        return ruleSets;
    }

    private static Location findEndLocation(List<String> lines, Location location) {
        int column = location.getColumn();

        for (int index = location.getLine() ; index  < lines.size() ; index ++) {
            String line = lines.get(index);

            if (line.indexOf(END_BRACKET, column) != -1) {
                return new Location(index , line.indexOf(END_BRACKET, column) + END_BRACKET.length());
            }

            column = -1;
        }

        throw new RuntimeException("Can't find END_BRACKET: " + lines);
    }

    private static final String END_BRACKET = "}";
}
