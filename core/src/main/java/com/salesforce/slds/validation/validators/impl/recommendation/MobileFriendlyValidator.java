/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.HTMLElement;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.shared.utils.ResourceUtilities;
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Lazy
public class MobileFriendlyValidator implements RecommendationValidator, InitializingBean {
    Map<String, List<String>> componentsRegistry;

    @Override
    public List<Recommendation> matches(Entry entry, Bundle bundle, Context context) {
        if (entry.getEntityType() != Entry.EntityType.LWC) {
            return Lists.newArrayList();
        }

        return entry.getInputs().stream().filter(input -> input.asElement() != null)
                .map(Input::asElement)
                .filter(htmlElement -> !isMobileFriendly(htmlElement))
                .map(htmlElement -> {
                    Recommendation.RecommendationBuilder builder = Recommendation.builder()
                            .input(htmlElement);

                    String tag = htmlElement.getContent().tagName();

                    Action action = Action.builder()
                            .value(htmlElement.getContent().outerHtml()).range(htmlElement.getRange())
                            .description(tag
                                    + " is known to have issues on mobile devices. Consider using a replacement " +
                                    "or create a custom component to use instead.")
                            .name(tag)
                            .actionType(ActionType.NONE)
                            .build();

                    Item item = new Item(tag, action);
                    builder.items(Sets.newHashSet(item));

                    return builder.build();
                }).collect(Collectors.toList());
    }

    private boolean isMobileFriendly(HTMLElement input) {
        String tag = input.getContent().tagName();
        String convertedTag = convertToNamespaceCamelCaseSyntax(tag);

        List<String> compatibilitySettings = this.componentsRegistry.get(convertedTag);
        if (compatibilitySettings != null) {
            boolean compatible = compatibilitySettings.stream().anyMatch(s ->
                    s.equalsIgnoreCase("Standard") || s.equalsIgnoreCase("Mobile"));
            return compatible;
        }
        return true;
    }

    private String convertToNamespaceCamelCaseSyntax(String tag) {
        StringBuilder sb = new StringBuilder();
        String[] tokens = tag.split("-");
        if (tokens.length > 1) {
            // Convert kebab-case component tag to camel-case.
            String namespace = tokens[0];
            List<String> camelCaseList = IntStream
                    .range(1, tokens.length)
                    .mapToObj(index -> {
                        if (index == 1) {
                            return tokens[index];
                        } else {
                            String token = tokens[index];
                            return token.substring(0, 1).toUpperCase() + token.substring(1);
                        }
                    })
                    .collect(Collectors.toList());
            String camelCased = String.format("%s:%s", namespace, String.join("", camelCaseList));
            return camelCased;
        } else {
            // There was no split. Return as it is.
            return tokens[0];
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ResourceUtilities.getResources(PriorityValidator.class,"validation/validators/mobile/component-experiences.json")
                .forEach(path -> {
                    try (InputStream inputStream = PriorityValidator.class.getResourceAsStream(path)) {
                        ObjectMapper mapper = new ObjectMapper();
                        this.componentsRegistry = mapper.readValue(inputStream, Map.class);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
    }
}
