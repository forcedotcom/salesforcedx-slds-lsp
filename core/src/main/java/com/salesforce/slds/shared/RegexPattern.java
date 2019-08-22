/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared;

public class RegexPattern {

    public static final String WORD_FRAGMENT = "\\w+";

    public static final String NUMBER_FRAGMENT = "(?:\\d+(?:\\.\\d*)?|\\.\\d+)";

    public static final String SIGNED_PATTERN = "(?<sign>[-|+])?";

    public static final String PERCENT_FRAGMENT_PATTERN = NUMBER_FRAGMENT + "%";

    public static final String NUMBER_PATTERN = SIGNED_PATTERN + "(?<number>" + NUMBER_FRAGMENT + ")";

    public static final String PERCENT_PATTERN = NUMBER_PATTERN + "%";

    public static final String NUMBER_WITH_UNIT_PATTERN = NUMBER_PATTERN + "\\s*(?<unit>[a-zA-Z]+)";

    public static final String HEX_COLOR_PATTERN = "#\\w{6,8}";

    public static final String RGB_COLOR_PATTERN = "rgb\\(\\s*(?<red>\\d+),\\s*(?<green>\\d+),\\s*(?<blue>\\d+)\\s*\\)";

    public static final String RGBA_COLOR_PATTERN = "rgba\\(\\s*(?<red>\\d+),\\s*(?<green>\\d+)," +
            "\\s*(?<blue>\\d+)\\,\\s*(?<alpha>" + NUMBER_FRAGMENT + ")\\s*\\)";

    public static final String HSL_COLOR_PATTERN = "hsl\\(\\s*(?<hue>\\d+),\\s*(?<saturation>" + PERCENT_FRAGMENT_PATTERN +
            "),\\s*(?<lightness>" + PERCENT_FRAGMENT_PATTERN + ")\\s*\\)";

    public static final String COLOR_PATTERN = HEX_COLOR_PATTERN + "|" +
            "rgb\\(\\s*\\d+,\\s*\\d+,\\s*\\d+\\s*\\)" + "|" +
            "rgba\\(\\s*\\d+,\\s*\\d+,\\s*\\d+\\,\\s*" + NUMBER_FRAGMENT + "\\s*\\)" + "|" +
            "hsl\\(\\s*\\d+,\\s*" + PERCENT_FRAGMENT_PATTERN + ",\\s*" + PERCENT_FRAGMENT_PATTERN + "\\s*\\)";

    public static final String NUMERIC_PATTERN = "[-|+]?" + NUMBER_FRAGMENT + "\\s*[a-zA-Z]+" + "|" +
            PERCENT_PATTERN + "|" + "[-|+]?" + NUMBER_FRAGMENT;

    public static final String START_TAG_PATTERN =
            "(?<start><(?<startComment>!--)|<(?<startTag>[\\w-]+(:?[\\w-]+)?)|(?<startSkip>\\{|[\\\\]?['\"]+))";
    public static final String END_TAG_PATTERN =
            "(?<end>/?>|(?<endComment>--\\s*>)|</(?<endTag>[\\w-]+(:?[\\w-]+)?)\\s*>|(?<endSkip>\\}))";

    public static final String AURA_TOKEN_FUNCTION = "t(?:oken)?\\((?<token>[\\w\\d]+)\\)";
    public static final String VAR_FUNCTION = "var\\(\\s*--lwc-(?<token>[\\w\\d-]+)\\s*(,\\s*(?<fallback>"+
            COLOR_PATTERN + "|" + NUMERIC_PATTERN + "|" + WORD_FRAGMENT + ")\\s*)?\\)";

    public static final String IMPORT_AND_EXPORT_TOKENS = "(:?\\s+|^)import\\s+|(:?\\s+|^)export\\s+";
}
