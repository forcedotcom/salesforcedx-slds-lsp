/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators;

import com.salesforce.slds.configuration.SldsConfiguration;
import com.salesforce.slds.shared.models.annotations.Annotation;
import com.salesforce.slds.shared.models.core.Bundle;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.recommendation.Recommendation;
import com.salesforce.slds.validation.runners.ValidateRunner;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SldsConfiguration.class)
public class AnnotationTest {

    @Autowired
    ValidateRunner runner;

    @Test
    public void blockAnnotationWithUtilityClasses() {
        String declaration = " /*@sldsValidatorIgnore*/ .THIS {\n" +
                "        overflow: hidden;\n" +
                "    }";

        Entry result = process(declaration);

        List<Annotation> annotations = result.getAnnotations();
        assertThat(annotations, Matchers.iterableWithSize(1));

        List<Recommendation> recommendations = result.getRecommendation();
        assertThat(recommendations, Matchers.iterableWithSize(0));
    }


    @Test
    public void inlineAnnotation() {
        String declaration = "/*@sldsValidatorAllow*/\n" +
                ".THIS .shareColumn .disabledPicklist {\n" +
                "    min-width: 150px;\n" +
                "    /*@sldsValidatorIgnore*/ max-width: 240px;\n" +
                "    margin-left: t(spacingLarge);\n" +
                "    padding: 0 t(spacingMedium) 0 t(spacingMedium);\n" +
                "    border: t(borderWidthThin) solid t(colorBorderInput);\n" +
                "    border-radius: t(borderRadiusMedium);\n" +
                "    /* @reviewed-violation W-2798020 */ height: t(lineHeightButton);\n" +
                "    line-height: t(lineHeightButton);\n" +
                "}";

        List<Annotation> annotations = process(declaration).getAnnotations();
        assertThat(annotations, Matchers.iterableWithSize(2));
    }

    private Entry process(String content) {
        String path = "test.css";
        Entry entry = Entry.builder().path(path)
                .rawContent(Arrays.asList(StringUtils.delimitedListToStringArray(content, "\n")))
                .build();
        Bundle bundle = new Bundle(entry);
        runner.setBundle(bundle);

        runner.run();

        return runner.getBundle().getEntries().get(0);
    }

}
