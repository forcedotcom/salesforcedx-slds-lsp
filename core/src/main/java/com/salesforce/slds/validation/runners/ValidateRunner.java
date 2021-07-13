/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.runners;

import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.models.override.ComponentOverride;
import com.salesforce.slds.shared.parsers.css.CSSParser;
import com.salesforce.slds.shared.parsers.javascript.JavascriptParser;
import com.salesforce.slds.shared.parsers.markup.MarkupParser;
import com.salesforce.slds.shared.utils.EntryUtilities;
import com.salesforce.slds.validation.aggregators.Aggregator;
import com.salesforce.slds.shared.models.core.*;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.processors.Processor;
import com.salesforce.slds.validation.validators.interfaces.OverrideValidator;
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import com.salesforce.slds.validation.validators.interfaces.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class ValidateRunner implements Runnable {

    private final List<Validator> validators;
    private final Aggregator aggregator;
    private final Processor processor;

    private Context context = new Context();
    private Bundle bundle;
    protected boolean completed = false;

    @Autowired
    public ValidateRunner(List<Validator> validators,
                          Aggregator aggregator, Processor processor) {
        this.validators = validators;
        this.aggregator = aggregator;
        this.processor = processor;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    protected void setup() {
        setupBundle();
    }

    protected void setupBundle() {
        getBundle().getEntries().forEach(e -> e.setInputs(getInputs(e)));

        String componentName = getComponentName(getBundle());
        Entry.EntityType type = getType(getBundle());

        getBundle().getEntries().forEach(entry -> {
            if (entry.getComponentName() == null) {
                entry.setComponentName(componentName);
            }

            if (entry.getEntityType() == null) {
                entry.setEntityType(type);
            }
        });
    }

    protected String getComponentName(Bundle bundle) {
        return EntryUtilities.getComponentName(bundle);
    }

    protected Entry.EntityType getType(Bundle bundle) {
        return EntryUtilities.getType(bundle);
    }

    @Override
    public void run() {
        if (context.isEnabled(ContextKey.GLOBAL)) {
            setup();

            for (Entry entry : bundle.getEntries()) {
                List<Recommendation> recommendations = validators.parallelStream()
                        .filter(validator -> validator instanceof RecommendationValidator)
                        .map(validator -> (RecommendationValidator) validator)
                        .map(validator -> validator.matches(entry, bundle, context))
                        .flatMap(List::stream)
                        .collect(aggregator.toList());

                entry.setRecommendation(processor.process(entry, recommendations));

                List<ComponentOverride> overrides = validators.parallelStream()
                        .filter(validator -> validator instanceof OverrideValidator)
                        .map(validator -> (OverrideValidator) validator)
                        .map(validator -> validator.getOverrides(entry, bundle, context))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

                entry.setOverrides(overrides);
            }
        }

        completed = true;
    }

    protected List<Input> getInputs(Entry entry) {
        List<Input> inputs = new ArrayList<>();

        inputs.addAll(getBlocks(entry));
        inputs.addAll(getRuleSets(entry));
        inputs.addAll(getElement(entry));

        return inputs;
    }

    protected List<RuleSet> getRuleSets(Entry entry) {
        if (entry.getPath().endsWith(".css") != true) {
            return new ArrayList<>();
        }

        return CSSParser.parse(entry.getRawContent());
    }

    protected List<HTMLElement> getElement(Entry entry) {
        if (entry.getPath().endsWith(".lib") != true
                && entry.getPath().endsWith(".evt") != true
                && entry.getPath().endsWith(".cmp") != true
                && entry.getPath().endsWith(".app") != true
                && entry.getPath().endsWith(".html") != true) {
            return new ArrayList<>();
        }

        return MarkupParser.parse(entry.getPath(), entry.getRawContent());
    }

    protected List<Block> getBlocks(Entry entry) {
        if (entry.getPath().endsWith(".js") != true) {
            return new ArrayList<>();
        }

        return JavascriptParser.convert(entry.getPath(), entry.getRawContent());
    }

}
