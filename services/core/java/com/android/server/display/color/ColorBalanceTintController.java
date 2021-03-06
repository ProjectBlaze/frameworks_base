/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.display.color;

import static com.android.server.display.color.DisplayTransformManager.LEVEL_COLOR_MATRIX_COLOR_BALANCE;

import android.content.Context;
import android.graphics.Color;
import android.hardware.display.ColorDisplayManager;
import android.opengl.Matrix;
import android.provider.Settings;

import java.util.Arrays;

/** Control the color transform for global color balance. */
final class ColorBalanceTintController extends TintController {

    private final float[] mMatrix = new float[16];
    private boolean mNeedsLinear;

    @Override
    public void setUp(Context context, boolean needsLinear) {
        mNeedsLinear = needsLinear;
    }

    @Override
    public float[] getMatrix() {
        return Arrays.copyOf(mMatrix, mMatrix.length);
    }

    @Override
    public void setMatrix(int rgb) {
        Matrix.setIdentityM(mMatrix, 0);

        float red = ((float) Color.red(rgb)) / 255.0f;
        float green = ((float) Color.green(rgb)) / 255.0f;
        float blue = ((float) Color.blue(rgb)) / 255.0f;

        if (!mNeedsLinear) {
            // Convert to non-linear sRGB as the assumed native color space
            red = linearToSrgb(red);
            green = linearToSrgb(green);
            blue = linearToSrgb(blue);
        }

        mMatrix[0] = red;
        mMatrix[5] = green;
        mMatrix[10] = blue;
    }

    @Override
    public int getLevel() {
        return LEVEL_COLOR_MATRIX_COLOR_BALANCE;
    }

    @Override
    public boolean isAvailable(Context context) {
        return ColorDisplayManager.isColorTransformAccelerated(context);
    }

    public void updateBalance(Context context, int userId) {
        int red = Settings.Secure.getIntForUser(context.getContentResolver(), channelToKey(0),
                255, userId);
        int green = Settings.Secure.getIntForUser(context.getContentResolver(), channelToKey(1),
                255, userId);
        int blue = Settings.Secure.getIntForUser(context.getContentResolver(), channelToKey(2),
                255, userId);

        int rgb = Color.rgb(red, green, blue);
        setMatrix(rgb);
    }

    public static String channelToKey(int channel) {
        switch (channel) {
            case 0:
                return Settings.Secure.DISPLAY_COLOR_BALANCE_RED;
            case 1:
                return Settings.Secure.DISPLAY_COLOR_BALANCE_GREEN;
            case 2:
                return Settings.Secure.DISPLAY_COLOR_BALANCE_BLUE;
            default:
                throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }

    private static float linearToSrgb(float x) {
        if (x >= 0.0031308) {
            return (1.055f) * ((float) Math.pow(x, 1.0f / 2.4f)) - 0.055f;
        } else {
            return 12.92f * x;
        }
    }
}
