/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.RelatedInformation;
import com.salesforce.slds.shared.parsers.markup.MarkupParser;
import com.salesforce.slds.tokens.models.UtilityClass;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;


public class UtilityClassValidatorTests {

    private UtilityClassValidator validator = new UtilityClassValidator();

    @Nested
    class CovertToRelatedInformation {

        @Test
        void emptyClass() {
            UtilityClass utilityClass = new UtilityClass();
            utilityClass.setName("testing");

            List<String> html = Arrays.asList("<div>DIV</div>");
            List<List<HTMLElement>> collectionsOfElements = new ArrayList<>();
            collectionsOfElements.add(MarkupParser.parse("test.cmp", html));

            List<RelatedInformation> results =
                    validator.convertToRelatedInformation(utilityClass, collectionsOfElements);

            assertThat(results, Matchers.iterableWithSize(1));

            RelatedInformation information = results.get(0);
            assertThat(information.getRange(),
                    Matchers.is(new Range(new Location(0, 4), new Location(0, 4))));
            assertThat(information.getOriginal(), Matchers.emptyString());
            assertThat(information.getValue(), Matchers.containsString("class=\"testing\""));
            assertThat(information.getPath(), Matchers.is("test.cmp"));
        }

        @Test
        void simpleClass() {
            UtilityClass utilityClass = new UtilityClass();
            utilityClass.setName("testing");

            List<String> html = Arrays.asList("<div class=\"existing\">DIV</div>");
            List<List<HTMLElement>> collectionsOfElements = new ArrayList<>();
            collectionsOfElements.add(MarkupParser.parse("test.cmp", html));

            List<RelatedInformation> results =
                    validator.convertToRelatedInformation(utilityClass, collectionsOfElements);

            assertThat(results, Matchers.iterableWithSize(1));

            RelatedInformation information = results.get(0);
            assertThat(information.getRange(),
                    Matchers.is(new Range(new Location(0, 12), new Location(0, 12))));
            assertThat(information.getOriginal(), Matchers.emptyString());
            assertThat(information.getValue(), Matchers.is("testing "));
        }


        @Test
        void expressionClass() {
            UtilityClass utilityClass = new UtilityClass();
            utilityClass.setName("testing");

            List<String> html = Arrays.asList("<div class=\"{! v.testing ? 'existing' : 'other'}\">DIV</div>");
            List<List<HTMLElement>> collectionsOfElements = new ArrayList<>();
            collectionsOfElements.add(MarkupParser.parse("test.cmp", html));

            List<RelatedInformation> results =
                    validator.convertToRelatedInformation(utilityClass, collectionsOfElements);

            assertThat(results, Matchers.iterableWithSize(1));

            RelatedInformation information = results.get(0);
            assertThat(information.getRange(),
                    Matchers.is(new Range(new Location(0, 14), new Location(0, 14))));
            assertThat(information.getOriginal(), Matchers.emptyString());
            assertThat(information.getValue(), Matchers.is("\'testing\' + "));
        }

        @Test
        @DisplayName("Chaining multiple utility class quick fix")
        void chain() {
            UtilityClass utilityClass = new UtilityClass();
            utilityClass.setName("testing");

            List<String> html = Arrays.asList("<div class=\"{! 'previous' + v.testing ? 'existing' : 'other'}\">DIV</div>");
            List<List<HTMLElement>> collectionsOfElements = new ArrayList<>();
            collectionsOfElements.add(MarkupParser.parse("test.cmp", html));

            List<RelatedInformation> results =
                    validator.convertToRelatedInformation(utilityClass, collectionsOfElements);

            assertThat(results, Matchers.iterableWithSize(1));

            RelatedInformation information = results.get(0);
            assertThat(information.getRange(),
                    Matchers.is(new Range(new Location(0, 25), new Location(0, 25))));
            assertThat(information.getOriginal(), Matchers.emptyString());
            assertThat(information.getValue(), Matchers.is("testing"));
        }
    }
}
