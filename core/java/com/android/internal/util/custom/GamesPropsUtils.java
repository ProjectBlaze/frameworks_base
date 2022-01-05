/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.internal.util.custom;

import android.os.Build;
import android.util.Log;

import java.util.Arrays;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GamesPropsUtils {

    private static final String TAG = GamesPropsUtils.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final Map<String, Object> propsToChangeMI11TP = createMap("2107113SI", "Xiaomi");
    private static final String[] packagesToChangeMI11TP = { // spoof as Mi 11T PRO
            "com.ea.gp.apexlegendsmobilefps",
            "com.levelinfinite.hotta.gp",
            "com.vng.mlbbvn"
    };

    private static final Map<String, Object> propsToChangeMI13P = createMap("2210132C", "Xiaomi");
    private static final String[] packagesToChangeMI13P = { // spoof as Mi 13 PRO
            "com.levelinfinite.sgameGlobal",
            "com.tencent.tmgp.sgame"
    };

    private static final Map<String, Object> propsToChangeOP8P = createMap("IN2020", "OnePlus");
    private static final String[] packagesToChangeOP8P = { // spoof as OnePlus 8 PRO
            "com.netease.lztgglobal",
            "com.pubg.imobile",
            "com.pubg.krmobile",
            "com.rekoo.pubgm",
            "com.riotgames.league.wildrift",
            "com.riotgames.league.wildrifttw",
            "com.riotgames.league.wildriftvn",
            "com.tencent.ig",
            "com.tencent.tmgp.pubgmhd",
            "com.vng.pubgmobile"
    };

    private static final Map<String, Object> propsToChangeOP9P = createMap("LE2101", "OnePlus");
    private static final String[] packagesToChangeOP9P = { // spoof as OnePlus 9 PRO
            "com.epicgames.fortnite",
            "com.epicgames.portal",
            "com.tencent.lolm"
    };

    private static final Map<String, Object> propsToChangeF4 = createMap("22021211RG", "Xiaomi");
    private static final String[] packagesToChangeF4 = { // spoof as POCO F4
            "com.dts.freefiremax",
            "com.dts.freefireth",
            "com.mobile.legends"
    };

    private static final Map<String, Object> propsToChangeROG3 = createMap("ASUS_I003D", "asus");
    private static final String[] packagesToChangeROG3 = { // spoof as ROG Phone 3
    	    "com.ea.gp.fifamobile",
            "com.pearlabyss.blackdesertm.gl",
            "com.pearlabyss.blackdesertm"
    };

    private static final Map<String, Object> propsToChangeROG6 = createMap("ASUS_AI2201", "asus");
    private static final String[] packagesToChangeROG6 = { // spoof as ROG Phone 6
            "com.activision.callofduty.shooter",
            "com.madfingergames.legends"
    };

    private static final Map<String, Object> propsToChangeXP5 = createMap("SO-52A", "Sony");
    private static final String[] packagesToChangeXP5 = { // spoof as Xperia 5
            "com.garena.game.codm",
            "com.tencent.tmgp.kr.codm",
            "com.vng.codmvn"
    };

    private static Map<String, Object> createMap(String model, String manufacturer) {
        Map<String, Object> map = new HashMap<>();
        map.put("MODEL", model);
        map.put("MANUFACTURER", manufacturer);
        return map;
    }

    public static void setProps(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        Map<String, Object> propsToChange = null;
        if (Arrays.asList(packagesToChangeMI11TP).contains(packageName)) {
            propsToChange = propsToChangeMI11TP;
        } else if (Arrays.asList(packagesToChangeOP8P).contains(packageName)) {
            propsToChange = propsToChangeOP8P;
        } else if (Arrays.asList(packagesToChangeOP9P).contains(packageName)) {
            propsToChange = propsToChangeOP9P;
        } else if (Arrays.asList(packagesToChangeF4).contains(packageName)) {
            propsToChange = propsToChangeF4;
        } else if (Arrays.asList(packagesToChangeROG3).contains(packageName)) {
            propsToChange = propsToChangeROG3;
        } else if (Arrays.asList(packagesToChangeROG6).contains(packageName)) {
            propsToChange = propsToChangeROG6;
        } else if (Arrays.asList(packagesToChangeXP5).contains(packageName)) {
            propsToChange = propsToChangeXP5;
        }
        if (propsToChange != null) {
            if (DEBUG) {
                Log.d(TAG, "Defining props for: " + packageName);
            }
            for (Map.Entry<String, Object> prop : propsToChange.entrySet()) {
                String key = prop.getKey();
                Object value = prop.getValue();
                setPropValue(key, value);
            }
        }
    }

    private static void setPropValue(String key, Object value) {
        try {
            if (DEBUG) {
                Log.d(TAG, "Defining prop " + key + " to " + value.toString());
            }
            Field field = Build.class.getDeclaredField(key);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, "Failed to set prop " + key, e);
        }
    }
}
