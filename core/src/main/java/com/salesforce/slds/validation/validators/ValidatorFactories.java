/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators;

import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.registry.TokenRegistry;
import com.salesforce.slds.validation.validators.impl.recommendation.TokenValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ValidatorFactories implements InitializingBean {

    @Autowired
    GenericApplicationContext context;

    @Autowired
    TokenRegistry utilities;

    @Override
    public void afterPropertiesSet() {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();

        utilities.getDesignTokens().stream()
                .collect(Collectors.groupingBy(o -> o.getCategory()))
                .forEach((s, tokens) -> {
                    BeanDefinitionBuilder builder  =
                            BeanDefinitionBuilder.genericBeanDefinition(TokenValidator.class)
                                    .addConstructorArgValue(Arrays.asList(s))
                                    .addConstructorArgValue(
                                            tokens.stream().map(DesignToken::getCssProperties)
                                                    .flatMap(List::stream).collect(Collectors.toSet()));

                    registry.registerBeanDefinition(s,
                            builder.getBeanDefinition());
                });
    }
}
