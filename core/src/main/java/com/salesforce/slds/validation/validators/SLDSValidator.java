/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators;

import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.registry.TokenRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SLDSValidator implements InitializingBean {

    @Autowired
    TokenRegistry tokenRegistry;

    protected final Map<String, DesignToken> DESIGN_TOKENS = new LinkedHashMap<>();
    protected final Set<String> VALID_UTILITY_CLASSES = new LinkedHashSet<>();

    static Pattern SLDS = Pattern.compile("slds-[^\\s,\\[:\\]\\.\";]*");

    @Override
    public void afterPropertiesSet(){
        setValidUtilityClasses();
        setDesignTokenLists();
    }

    private void setValidUtilityClasses(){

        tokenRegistry.getComponentBlueprints().stream()
                .forEach(componentBlueprint -> {
                    componentBlueprint.getSelectors().forEach(selector ->
                            VALID_UTILITY_CLASSES.addAll(processTokens(selector))
                    );

                    componentBlueprint.getTokens().forEach((name, componentDesignToken) -> {
                        componentDesignToken.getCssSelectors().forEach(selector ->
                                VALID_UTILITY_CLASSES.addAll(processTokens(selector))
                        );
                    });

                });

        tokenRegistry.getUtilityClasses().stream()
                .map(utilityClass -> Arrays.asList(utilityClass.getName().split(" ")))
                .flatMap(List::stream)
                .forEach(s -> {
                    String value = s.trim();
                    VALID_UTILITY_CLASSES.addAll(processTokens(value));
                });

    }

    private void setDesignTokenLists() {

        tokenRegistry.getDesignTokens().forEach(designToken -> {
            DESIGN_TOKENS.put(designToken.getName(), designToken);
        });

    }

    public Set<String> processTokens(String tokens) {
        Set<String> possibleTokens = new HashSet<>();

        Matcher matcher = SLDS.matcher(tokens);

        while(matcher.find()) {
            possibleTokens.add(matcher.group());
        }

        return possibleTokens;
    }
}
