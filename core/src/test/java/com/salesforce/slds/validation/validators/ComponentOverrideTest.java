/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators;

import com.salesforce.slds.configuration.SldsConfiguration;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.override.ComponentOverride;
import com.salesforce.slds.validation.runners.ValidateRunner;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SldsConfiguration.class)
public class ComponentOverrideTest {

    @Autowired
    ValidateRunner runner;

    @BeforeEach
    public void prepare() {
        StringBuilder builder = new StringBuilder();
        builder.append(".THIS .slds-not-a-real-class {\n" +
                "    width: 100%;\n" +
                "}\n" +
                "\n" +
                ".THIS .slds-accordion__section,\n" +
                ".THIS .slds-dropdown__item {\n" +
                "    width: 100%;\n" +
                "}\n" +
                ".THIS .slds-icon {\n" +
                "    width: 100%;\n" +
                "}\n" +
                ".THIS div.slds-icon {\n" +
                "    width: 90%;\n" +
                "}\n" +
                "\n" +
                ".THIS div.icons#row.slds-icon {\n" +
                "    width: 90%;\n" +
                "}");

        Entry entry = Entry.builder().path("test.css")
                .rawContent(Arrays.asList(StringUtils.delimitedListToStringArray(builder.toString(), "\n")))
                .build();
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);
        runner.run();

    }

    public List<ComponentOverride> getComponentOverrides() {
        return runner.getBundle().getEntries().stream().map(Entry::getOverrides)
                .flatMap(List::stream).collect(Collectors.toList());
    }

    @Test
    public void testOverridesCount() {
        assertThat(getComponentOverrides(), Matchers.iterableWithSize(5));
    }

    @Test
    public void testDataStructure() {
        List<ComponentOverride> componentOverrides = getComponentOverrides();
        ComponentOverride co1 = componentOverrides.get(0);
        ComponentOverride co2 = componentOverrides.get(1);
        ComponentOverride co3 = componentOverrides.get(2);
        ComponentOverride co4 = componentOverrides.get(3);
        ComponentOverride co5 = componentOverrides.get(4);


        assertThat(co1.getSelector().toString(false), Matchers.is(".THIS .slds-accordion__section"));
        assertThat(co1.getSldsComponentClass(), Matchers.is("slds-accordion__section"));

        assertThat(co2.getSelector().toString(false), Matchers.is(".THIS .slds-dropdown__item"));
        assertThat(co2.getSldsComponentClass(), Matchers.is("slds-dropdown__item"));

        assertThat(co3.getSelector().toString(false), Matchers.is(".THIS .slds-icon"));
        assertThat(co3.getSldsComponentClass(), Matchers.is("slds-icon"));

        assertThat(co4.getSelector().toString(false), Matchers.is(".THIS div.slds-icon"));
        assertThat(co4.getSldsComponentClass(), Matchers.is("slds-icon"));

        assertThat(co5.getSelector().toString(false), Matchers.is(".THIS div.icons#row.slds-icon"));
        assertThat(co5.getSldsComponentClass(), Matchers.is("slds-icon"));
    }

    @Test
    public void testRangeForMultipleOverrides() {
        List<ComponentOverride> componentOverrides = getComponentOverrides().subList(2, 5);

        assertThat(componentOverrides.get(0).getRange(),
                Matchers.is(new Range(new Location(8, 6), new Location(8, 16))));

        assertThat(componentOverrides.get(1).getRange(),
                Matchers.is(new Range(new Location(11, 6), new Location(11, 19))));

        assertThat(componentOverrides.get(2).getRange(),
                Matchers.is(new Range(new Location(15, 6), new Location(15, 29))));
    }

}
