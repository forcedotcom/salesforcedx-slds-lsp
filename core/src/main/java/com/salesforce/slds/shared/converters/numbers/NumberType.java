/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters.numbers;

import com.google.common.collect.ImmutableList;
import com.salesforce.slds.shared.converters.Type;

import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NumberType implements Type {

    private static final Pattern CLEANSE_NUMBER_PATTERN = Pattern.compile("(?<dot>\\.?)[1-9]*(?<zero>0+)$");

    private static final List<Type> TYPES =
            ImmutableList.of(new UnitType(), new PercentType(), new UnitLessType());

    public static List<Type> get() {
        return TYPES;
    }


    protected String cleanseNumber(String number) {
        StringBuilder value = new StringBuilder();

        Matcher matcher = CLEANSE_NUMBER_PATTERN.matcher(number);

        if (matcher.find()) {
            int dotStart = matcher.start("dot");
            int dotEnd = matcher.end("dot");

            int zeroStart = matcher.start("zero");

            if (dotStart == dotEnd) {
                value.append(number);
            } else {
                if (dotStart == 0) {
                    value.append(0);
                }

                value.append(number, 0, dotEnd == zeroStart ? dotStart : zeroStart);
            }
        } else {
            value.append(number);
        }

        return value.toString();
    }

    protected Set<String> generateNumbers(String sign, String number) {
        return generateNumbers(Optional.ofNullable(sign), number, Optional.empty());
    }

    protected Set<String> generateNumbers(String sign, String number, String unit) {
        return generateNumbers(Optional.ofNullable(sign), number, Optional.ofNullable(unit));
    }

    private Set<String> generateNumbers(Optional<String> sign, String number, Optional<String> unit) {
        Set<String> results = new LinkedHashSet<>();

        if (unit.isPresent() && unit.get().contentEquals("px")) {
            Double num = Double.parseDouble(number) / 16.0;
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(3);
            df.setMinimumFractionDigits(0);

            results.add(generateString(sign, df.format(num), Optional.of("rem")));
        }

        if (unit.isPresent() && unit.get().contentEquals("rem")) {
            Double num = Double.parseDouble(number) * 16;
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(3);
            df.setMinimumFractionDigits(0);

            results.add(generateString(sign, df.format(num), Optional.of("px")));
        }

        results.add(generateString(sign, number, unit));

        return results;
    }

    private String generateString(Optional<String> sign, String number, Optional<String> unit) {
        StringBuilder value = new StringBuilder();

        if (sign.isPresent()) {
            value.append(sign.get());
        }

        String cleansedNumber = cleanseNumber(number);
        value.append(cleansedNumber);

        if (cleansedNumber.contentEquals("0") == false && unit.isPresent()) {
            value.append(unit.get());
        }

        return value.toString();
    }
}
