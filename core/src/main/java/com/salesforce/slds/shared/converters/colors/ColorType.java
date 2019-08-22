/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.converters.colors;

import com.google.common.collect.ImmutableList;
import com.salesforce.slds.shared.converters.Converter;
import com.salesforce.slds.shared.converters.Type;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;

public abstract class ColorType implements Type {

    private static final List<Type> TYPES =
            ImmutableList.of(new HexColorType(), new HSLColorType(),
                    new RGBAColorType(), new RGBColorType(), new NamedColorType());

    public static List<Type> get() {
        return TYPES;
    }

    protected Converter.State addColor(Matcher matcher, Color color, Converter.State state) {
        String hex = getHEX(color);

        state.addValues(matcher, hex.toLowerCase());
        state.addValues(matcher, hex.toUpperCase());
        state.addValues(matcher, getRGB(color));
        state.addValues(matcher, getHSL(color));

        return state;
    }

    protected String getHEX(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    protected String getRGB(Color color) {
        StringBuilder value = new StringBuilder("rgb(")
                .append(color.getRed()).append(", ")
                .append(color.getGreen()).append(", ")
                .append(color.getBlue()).append(")");

        return value.toString();
    }

    protected String getHSL(Color color) {
        float[] hsl = fromRGB(color);

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setRoundingMode(RoundingMode.HALF_DOWN);

        StringBuilder value = new StringBuilder("hsl(")
                .append(Math.round(hsl[0])).append(", ")
                .append(decimalFormat.format(hsl[1])).append("%, ")
                .append(decimalFormat.format(hsl[2])).append("%)");

        return value.toString();
    }

    private float[] fromRGB(Color color) {
        //  Get RGB values in the range 0 - 1

        float[] rgb = color.getRGBColorComponents( null );
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];

        //	Minimum and Maximum RGB values are used in the HSL calculations

        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));

        //  Calculate the Hue

        float h = 0;

        if (max == min)
            h = 0;
        else if (max == r)
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        else if (max == g)
            h = (60 * (b - r) / (max - min)) + 120;
        else if (max == b)
            h = (60 * (r - g) / (max - min)) + 240;

        //  Calculate the Luminance

        float l = (max + min) / 2;

        //  Calculate the Saturation

        float s = 0;

        if (max == min)
            s = 0;
        else if (l <= .5f)
            s = (max - min) / (max + min);
        else
            s = (max - min) / (2 - max - min);

        return new float[] {h, s * 100, l * 100};
    }

    protected Color toColor(float h, float s, float l) {
        return toColor(h, s, l, 1.0f);
    }

    protected Color toColor(float h, float s, float l, float alpha) {
        if (s <0.0f || s > 100.0f)
        {
            String message = "Color parameter outside of expected range - Saturation";
            throw new IllegalArgumentException( message );
        }

        if (l <0.0f || l > 100.0f)
        {
            String message = "Color parameter outside of expected range - Luminance";
            throw new IllegalArgumentException( message );
        }

        if (alpha <0.0f || alpha > 1.0f)
        {
            String message = "Color parameter outside of expected range - Alpha";
            throw new IllegalArgumentException( message );
        }

        //  Formula needs all values between 0 - 1.

        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;

        float q = 0;

        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);

        float p = 2 * l - q;

        float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, HueToRGB(p, q, h));
        float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);

        return new Color(r, g, b, alpha);
    }

    private static float HueToRGB(float p, float q, float h) {
        if (h < 0) h += 1;

        if (h > 1 ) h -= 1;

        if (6 * h < 1)
        {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1 )
        {
            return  q;
        }

        if (3 * h < 2)
        {
            return p + ( (q - p) * 6 * ((2.0f / 3.0f) - h) );
        }

        return p;
    }
}
