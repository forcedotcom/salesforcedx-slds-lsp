/*
 * Copyright (c) 2021, salesforce.com, inc.
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
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.runners.ValidateRunner;
import com.salesforce.slds.validation.validators.impl.recommendation.MobileSLDS_MarkupLabelValidator;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static com.salesforce.slds.validation.validators.impl.recommendation.MobileSLDS_MarkupFriendlyValidator.NON_MOBILE_FRIENDLY_MESSAGE_TEMPLATE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SldsConfiguration.class)
public class MobileValidationTest {

    @Autowired
    private
    ValidateRunner runner;

    @Test
    void mobileComponentExperience1Parsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/components/mobileComponentExperience1.html");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        runner.run();

        List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                .flatMap(List::stream).collect(Collectors.toList());
        assertThat(recommendations, Matchers.hasSize(2));

        Map<String, List<Range>> results = new LinkedHashMap<>();
        Map<String, String> messages = new HashMap<>();
        recommendations.forEach(recommendation -> {
            recommendation.getItems().stream()
                    .forEach(item -> {
                        for (Action action : item.getActions()) {
                            List<Range> ranges = results.getOrDefault(action.getName(), new ArrayList<>());
                            ranges.add(action.getRange());
                            results.put(action.getName(), ranges);
                            messages.put(action.getName(), action.getDescription());
                        }

                    });
        });

        assertThat(results.get("lightning-datatable"),
                Matchers.hasItem(new Range(new Location(1, 4), new Location(1, 47))));
        assertThat(results.get("lightning-flow-support"),
                Matchers.hasItem(new Range(new Location(3, 4), new Location(3, 53))));
        assertThat(messages.get("lightning-datatable"),
                Matchers.equalTo("lightning-datatable" + NON_MOBILE_FRIENDLY_MESSAGE_TEMPLATE));
        assertThat(messages.get("lightning-flow-support"),
                Matchers.equalTo("lightning-flow-support" + NON_MOBILE_FRIENDLY_MESSAGE_TEMPLATE));
    }

    @Test
    void mobileComponentExperience2Parsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/components/mobileComponentExperience2.html");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        runner.run();

        List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                .flatMap(List::stream).collect(Collectors.toList());
        assertThat(recommendations, Matchers.hasSize(0));
    }

    @Test
    void mobileLabel1Parsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/components/mobileLabel1.html");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        runner.run();

        List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                .flatMap(List::stream).collect(Collectors.toList());
        assertThat(recommendations, Matchers.hasSize(11));
        for(int i = 0; i < recommendations.size(); i++) {
            Recommendation recommendation = recommendations.get(i);
            Set<Item> items = recommendation.getItems();
            assertThat(items, Matchers.hasSize(1));
            Item item = items.iterator().next();
            Set<Action> actions = item.getActions();
            assertThat(actions, Matchers.hasSize(1));
            Action action = actions.iterator().next();
            assertThat(action.getDescription(), Matchers.equalTo(MobileSLDS_MarkupLabelValidator.REQUIRE_LABELS));
        }
    }

    @Test
    void mobileLabel2Parsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/components/mobileLabel2.html");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        runner.run();

        List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                .flatMap(List::stream).collect(Collectors.toList());
        assertThat(recommendations, Matchers.hasSize(0));
    }

    @Test
    void mobileLabel3Parsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/components/mobileLabel3.html");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        runner.run();

        List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                .flatMap(List::stream).collect(Collectors.toList());
        assertThat(recommendations, Matchers.hasSize(1));
    }
}