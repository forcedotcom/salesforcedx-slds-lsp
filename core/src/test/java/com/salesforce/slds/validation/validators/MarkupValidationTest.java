/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators;

import com.salesforce.slds.configuration.SldsConfiguration;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.runners.ValidateRunner;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SldsConfiguration.class)
public class MarkupValidationTest {

    @Autowired
    ValidateRunner runner;

    @Test
    public void expression1Parsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/components/example1.cmp");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        runner.setEntry(entry);

        runner.run();

        List<Recommendation> recommendations = runner.getEntry().getRecommendation();
        assertThat(recommendations, Matchers.hasSize(1));

        Recommendation recommendation = recommendations.get(0);
        Map<String, List<Range>> results = new LinkedHashMap<>();

        recommendation.getItems().stream()
                .forEach(item -> {
                    for (Action action : item.getActions()) {
                        List<Range> ranges = results.getOrDefault(action.getName(), new ArrayList<>());
                        ranges.add(action.getRange());

                        results.put(action.getName(), ranges);
                    }
                });

        assertThat(results.get("slds-m-right_x-small"),
                Matchers.hasItem(new Range(new Location(11, 31), new Location(11, 52))));
    }

    @Test
    public void expression2Parsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/components/example2.cmp");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        runner.setEntry(entry);

        runner.run();

        assertThat(runner.getInputs(), Matchers.hasSize(14));

        List<Recommendation> recommendations = runner.getEntry().getRecommendation();
        assertThat(recommendations, Matchers.hasSize(6));
    }
}
