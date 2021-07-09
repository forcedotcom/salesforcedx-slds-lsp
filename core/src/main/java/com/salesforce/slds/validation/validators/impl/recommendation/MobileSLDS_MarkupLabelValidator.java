/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.recommendation;

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
import com.salesforce.slds.validation.validators.interfaces.RecommendationValidator;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Lazy
public class MobileSLDS_MarkupLabelValidator implements RecommendationValidator, InitializingBean {
    public static final String REQUIRE_LABELS = "Clickable images, Lightning button icons, and form elements require labels for mobile users.";

    private HashSet<String> elementsRequiringLabels = Sets.newHashSet("lightning-button-icon",
            "input", "select", "textarea", "button", "fieldset",
            "legend", "datalist", "output", "optgroup");

    private boolean isAnchored(Element content) {
        if (content.tagName().matches("a")){
            return true;
        } else if (content.parent() != null) {
            return isAnchored(content.parent());
        }
        return false;
    }

    private boolean isContainedByLabel(Element content) {
        if (content.tagName().matches("label")){
            return true;
        } else if (content.parent() != null) {
            return isContainedByLabel(content.parent());
        }
        return false;
    }

    private boolean isElementWithoutLabel(HTMLElement input, HashSet<String> labelForHashSet) {
        Element content = input.getContent();
        String tag = input.getContent().tagName();
        Attributes attributes = content.attributes();
        String tagId = attributes.get("id");

        if (elementsRequiringLabels.contains(tag) ||
                (tag.matches("img") && isAnchored(content))) {
            if (labelForHashSet.contains(tagId) || isContainedByLabel(content)) {
                return false;
            }
            return true;
        }
        return false;
    }


    @Override
    public List<Recommendation> matches(Entry entry, Bundle bundle, Context context) {
        // Only validate for LWC source code.
        if (entry.getEntityType() != Entry.EntityType.LWC) {
            return Lists.newArrayList();
        }

        // First pass: Create a lookup for <label> that has a value for attribute "for".
        HashSet<String> labelForHashSet = new HashSet<>();
        for (Input input: entry.getInputs() ) {
            HTMLElement htmlElement = input.asElement();
            if (htmlElement != null) {
                Element element = htmlElement.getContent();
                String tag = element.tagName();

                if (tag == "label") {
                    Attributes attributes = element.attributes();
                    String forAttr = attributes.get("for");
                    labelForHashSet.add(forAttr);
                }
            }
        }

        // Second pass: Check if an element is an image button(anchored image or lightning button-icon) or
        // a form element. If it is then check to see that it has a label.
        return entry.getInputs().stream().filter(input -> input.asElement() != null)
                .map(Input::asElement)
                .filter(htmlElement -> isElementWithoutLabel(htmlElement, labelForHashSet))
                .map(htmlElement -> {
                    Recommendation.RecommendationBuilder builder = Recommendation.builder()
                            .input(htmlElement);

                    String tag = htmlElement.getContent().tagName();

                    Action action = Action.builder()
                            .value(htmlElement.getContent().outerHtml()).range(htmlElement.getRange())
                            .description(REQUIRE_LABELS)
                            .name(tag)
                            .actionType(ActionType.NONE)
                            .build();

                    Item item = new Item(tag, action);
                    builder.items(Sets.newHashSet(item));

                    return builder.build();
                }).collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}