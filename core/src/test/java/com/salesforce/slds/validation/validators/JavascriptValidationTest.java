/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators;

import com.google.common.collect.Lists;
import com.salesforce.slds.configuration.SldsConfiguration;
import com.salesforce.slds.shared.models.core.Block;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.shared.parsers.javascript.JavascriptParser;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SldsConfiguration.class)
public class JavascriptValidationTest {

    @Autowired
    private ValidateRunner runner;

    @Test
    public void libraryParsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/javascript/test-library.js");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        List<Input> inputs = Lists.newArrayList(JavascriptParser.convert(f.getPath(), Files.readAllLines(f.toPath())));
        entry.setInputs(inputs);
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        List<Block> blocks = runner.getBundle().getInputs().stream()
                .map(Input::asBlock).filter(Objects::nonNull).collect(Collectors.toList());

        assertThat(blocks, Matchers.iterableWithSize(7));

        runner.run();

        List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                .flatMap(List::stream).collect(Collectors.toList());
        assertThat(recommendations, Matchers.iterableWithSize(1));

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

        assertThat(results.keySet(),
                Matchers.hasItems("slds-float_left"));

        assertThat(results.get("slds-float_left"),
                Matchers.hasItem(new Range(new Location(40, 52),
                        new Location(40, 68))));
    }

    @Test
    public void helperParsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/javascript/test-helper.js");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        List<Input> inputs = Lists.newArrayList(JavascriptParser.convert(f.getPath(), Files.readAllLines(f.toPath())));
        entry.setInputs(inputs);
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        List<Block> blocks = runner.getBundle().getInputs().stream()
                .map(Input::asBlock).filter(Objects::nonNull).collect(Collectors.toList());
        assertThat(blocks, Matchers.hasSize(2));

        runner.run();

        List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                .flatMap(List::stream).collect(Collectors.toList());
        assertThat(recommendations, Matchers.iterableWithSize(1));

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

        assertThat(results.keySet(),
                Matchers.hasItems("slds-var-p-around_medium"));

        assertThat(results.get("slds-var-p-around_medium"),
                Matchers.hasItem(new Range(new Location(7, 34),
                        new Location(7, 54))));
    }

    @Test
    public void es6Parsing() throws IOException {
        URL resource = MarkupValidationTest.class.getResource("/javascript/es6.js");
        File f = new File(resource.getFile());

        Entry entry = Entry.builder().path(f.getPath()).rawContent(Files.readAllLines(f.toPath())).build();
        List<Input> inputs = Lists.newArrayList(JavascriptParser.convert(f.getPath(), Files.readAllLines(f.toPath())));
        entry.setInputs(inputs);
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        List<Block> blocks = runner.getBundle().getInputs().stream()
                .map(Input::asBlock).filter(Objects::nonNull).collect(Collectors.toList());
        assertThat(blocks, Matchers.hasSize(1));

        runner.run();

        List<Recommendation> recommendations = runner.getBundle().getEntries().stream().map(Entry::getRecommendation)
                .flatMap(List::stream).collect(Collectors.toList());
        assertThat(recommendations, Matchers.iterableWithSize(0));
    }
}
