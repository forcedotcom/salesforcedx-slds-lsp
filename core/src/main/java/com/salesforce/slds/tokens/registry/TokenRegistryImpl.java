/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.tokens.registry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.salesforce.slds.shared.utils.ResourceUtilities;
import com.salesforce.slds.tokens.models.ComponentBlueprint;
import com.salesforce.slds.tokens.models.DesignToken;
import com.salesforce.slds.tokens.models.TokenStatus;
import com.salesforce.slds.tokens.models.UtilityClass;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
@Lazy
public class TokenRegistryImpl implements TokenRegistry, InitializingBean {

    @Override
    public Set<ComponentBlueprint> getComponentBlueprints() {
        return getComponentsInternal();
    }

    @Override
    public Set<String> getValidUtilityClasses() {
        if (this.validUtilityClasses == null) {
            this.validUtilityClasses = new HashSet<>();

            getComponentBlueprints().stream()
                    .forEach(componentBlueprint -> {
                        componentBlueprint.getSelectors().forEach(selector ->
                                validUtilityClasses.addAll(processTokens(selector)));

                        componentBlueprint.getTokens().forEach((name, componentDesignToken) -> {
                           componentDesignToken.getCssSelectors().forEach(selector ->
                                   validUtilityClasses.addAll(processTokens(selector)));
                        });
                    });

            getUtilityClasses().stream()
                    .map(utilityClass -> Arrays.asList(utilityClass.getName().split(" ")))
                    .flatMap(List::stream)
                    .forEach(s ->
                        validUtilityClasses.addAll(processTokens(s.trim()))
                    );
        }

        return this.validUtilityClasses;
    }

    @Override
    public Optional<ComponentBlueprint> getComponentBlueprint(String component) {
        return getComponentsInternal().stream()
                .filter(blueprint -> blueprint.getId().contentEquals(component))
                .findAny();
    }

    @Override
    public Optional<DesignToken> getDesignToken(String key) {
        DesignToken designToken = getDesignTokensInternal().get(key);
        return Optional.ofNullable(designToken);
    }

    @Override
    public List<DesignToken> getDesignTokensFromCategory(String category) {
        return getDesignTokensInternal().values().stream()
                .filter(token -> token.getCategory().contentEquals(category))
                .filter(token -> token.getScope() == null || token.getScope().contentEquals("global"))
                .filter(token -> getComponentsInternal().contains(token.getName()) == false)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getDesignTokenCategories() {
        return getDesignTokensInternal().values().stream()
                .map(token -> token.getCategory())
                .collect(Collector.of(TreeSet::new, Set::add,
                        (left, right) -> { left.addAll(right); return left; }));
    }

    @Override
    public List<UtilityClass> getUtilityClasses() {
        return getUtilityClassesInternal();
    }

    @Override
    public List<DesignToken> getDesignTokens() {
        return new ArrayList<>(getDesignTokensInternal().values());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SimpleModule module =
                new SimpleModule("TokenStatusDeserializer",
                        new Version(1, 0, 0, null, null, null));
        module.addDeserializer(TokenStatus.class, new TokenStatusDeserializer());
        mapper.registerModule(module);
    }

    private static final String BASE_LOCATION = "/tokens/slds";

    private Set<ComponentBlueprint> components;
    private Map<String, DesignToken> tokens;
    private List<UtilityClass> utilityClasses;
    private Set<String> validUtilityClasses;
    private ObjectMapper mapper = new ObjectMapper();
    private static final String SLDS = "slds-[^\\s,\\[:\\]\\.\";]*";
    private static final Pattern SLDSPattern = Pattern.compile(SLDS);

    private Set<ComponentBlueprint> getComponentsInternal() {
        if (this.components == null) {
            try {
                List<String> paths = ResourceUtilities.getResources(TokenRegistryImpl.class,BASE_LOCATION + "/components.json");

                this.components = mapper.readValue(
                        TokenRegistryImpl.class.getResourceAsStream(paths.get(0)),
                        new TypeReference<Set<ComponentBlueprint>>(){});
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return this.components;
    }

    public List<UtilityClass> getUtilityClassesInternal() {
        if (this.utilityClasses == null) {
            try {
                List<String> paths = ResourceUtilities.getResources(TokenRegistryImpl.class,BASE_LOCATION + "/utilities.json");

                this.utilityClasses = mapper.readValue(
                        TokenRegistryImpl.class.getResourceAsStream(paths.get(0)),
                        new TypeReference<List<UtilityClass>>(){});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.utilityClasses;
    }


    public Map<String, DesignToken> getDesignTokensInternal() {
        if (this.tokens == null) {
            try {
                List<String> paths = ResourceUtilities.getResources(TokenRegistryImpl.class, BASE_LOCATION + "/tokens.json");

                this.tokens = mapper.readValue(
                        TokenRegistryImpl.class.getResourceAsStream(paths.get(0)),
                        new TypeReference<Map<String, DesignToken>>(){});
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return this.tokens;
    }

    private Set<String> processTokens(String tokens) {
        Set<String> possibleTokens = new HashSet<>();

        Matcher matcher = SLDSPattern.matcher(tokens);

        while(matcher.find()) {
            possibleTokens.add(matcher.group());
        }

        return possibleTokens;
     }

    private static class TokenStatusDeserializer extends StdDeserializer<TokenStatus> {

        public TokenStatusDeserializer() {super(TokenStatus.class);}

        @Override
        public TokenStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            ObjectCodec codec = jsonParser.getCodec();
            JsonNode node = codec.readTree(jsonParser);

            return TokenStatus.fromValue(node.asText());
        }
    }
}
