/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.utils.ResourceUtilities;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.utils.CSSValidationUtilities;
import com.salesforce.slds.validation.utils.JavascriptValidationUtilities;
import com.salesforce.slds.validation.utils.MarkupValidationUtilities;
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import com.salesforce.slds.validation.validators.models.Properties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PriorityValidator implements RecommendationValidator, InitializingBean {

    final Map<String, List<DesignToken>> TOKENS_BY_GROUP = new HashMap<>();

    @Autowired
    MarkupValidationUtilities markupValidationUtilities;

    @Autowired
    JavascriptValidationUtilities javascriptValidationUtilities;

    @Autowired
    CSSValidationUtilities cssValidationUtilities;


    @Override
    public void afterPropertiesSet() {
        Properties properties = buildEntries();
        TOKENS_BY_GROUP.put(ContextKey.BEM.name(),
                properties.asTokens(item -> item.getName().contentEquals("bem-naming")));

        TOKENS_BY_GROUP.put(ContextKey.DENSITY.name(),
                properties.asTokens(item -> item.getName().contentEquals("bem-naming") == false));
    }

    @Override
    public List<Recommendation> matches(Entry entry, Context context) {

        List<DesignToken> tokens = new ArrayList<>();
        if (context.isEnabled(ContextKey.BEM)) {
            tokens.addAll(TOKENS_BY_GROUP.get(ContextKey.BEM.name()));
        }

        if (context.isEnabled(ContextKey.DENSITY)) {
            tokens.addAll(TOKENS_BY_GROUP.get(ContextKey.DENSITY.name()));
        }

        return entry.getInputs().stream()
                .map(input -> process(input, entry.getRawContent(), tokens))
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected List<Recommendation> process(Input input, List<String> rawContents, List<DesignToken> tokens) {
        List<Recommendation> result = new ArrayList<>();

        Input.Type type = input.getType();

        if (type == Input.Type.STYLE) {
            result.addAll(input.asRuleSet().getStylesWithAnnotationType().stream()
                    .map(style -> cssValidationUtilities.match(style, tokens, rawContents, false))
                    .collect(Collectors.toList()));
        }

        if (type == Input.Type.JAVASCRIPT) {
            result.add(javascriptValidationUtilities.match(input.asBlock(), tokens, rawContents));
        }


        if (type == Input.Type.MARKUP &&
                input.asElement().getContent().hasAttr("class")) {
            result.add(markupValidationUtilities.match(input.asElement(), tokens));
        }

        return result;
    }

    Properties buildEntries() {
        Properties properties = new Properties();
        List<Properties.Item> items = new ArrayList<>();
        Yaml yaml = new Yaml();


        ResourceUtilities.getResources(PriorityValidator.class,"/validation/validators/priorities")
                .forEach(path -> {
                    try (InputStream inputStream = PriorityValidator.class.getResourceAsStream(path)) {
                        Properties prop = yaml.loadAs(inputStream, Properties.class);

                        items.addAll(prop.getItems());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

        properties.setItems(items);

        return properties;
    }
}
