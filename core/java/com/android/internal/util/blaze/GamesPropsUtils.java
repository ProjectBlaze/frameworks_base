/*
 * Copyright (C) 2022 ReloadedOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.blaze;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GamesPropsUtils extends PixelPropsUtils {

    private static final String TAG = "GamesPropsUtils";
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_PACKAGES = false;

    private static final String MODEL_ROG6 = "ASUS_AI2201";
    private static final String MODEL_XP5 = "SO-52A";
    private static final String MODEL_OP8P = "IN2020";
    private static final String MODEL_OP9P = "LE2123";
    private static final String MODEL_MI11T = "21081111RG";
    private static final String MODEL_MI13P = "2210132C";
    private static final String MODEL_K30U = "M2006J10C";

    private static final Set<String> sRog6Packages = Set.of(
        "com.activision.callofduty.shooter",
        "com.ea.gp.fifamobile",
        "com.gameloft.android.ANMP.GloftA9HM",
        "com.madfingergames.legends",
        "com.mobile.legends",
        "com.pearlabyss.blackdesertm",
        "com.pearlabyss.blackdesertm.gl"
    );

    private static final Set<String> sXp5Packages = Set.of(
        "com.garena.game.codm",
        "com.tencent.tmgp.kr.codm",
        "com.vng.codmvn"
    );

    private static final Set<String> sOp8pPackages = Set.of(
        "com.tencent.ig",
        "com.pubg.krmobile",
        "com.vng.pubgmobile",
        "com.rekoo.pubgm",
        "com.tencent.tmgp.pubgmhd",
        "com.riotgames.league.wildrift",
        "com.riotgames.league.wildrifttw",
        "com.riotgames.league.wildriftvn",
        "com.netease.lztgglobal"
    );

    private static final Set<String> sOp9pPackages = Set.of(
        "com.epicgames.fortnite",
        "com.epicgames.portal",
        "com.tencent.lolm"
    );

    private static final Set<String> sMI11TPackages = Set.of(
        "com.ea.gp.apexlegendsmobilefps",
        "com.levelinfinite.hotta.gp",
        "com.supercell.clashofclans",
        "com.vng.mlbbvn"
    );

    private static final Set<String> sMI13PPackages = Set.of(
        "com.levelinfinite.sgameGlobal",
        "com.tencent.tmgp.sgame"
    );

    private static final Set<String> sK30UPackages = Set.of(
        "com.pubg.imobile"
    );

    private static final Map<String, String> sPackagesModelMap = new HashMap<String, String>();

    static {
        Map.of(
            sRog6Packages, MODEL_ROG6,
            sXp5Packages,  MODEL_XP5,
            sOp8pPackages, MODEL_OP8P,
            sOp9pPackages, MODEL_OP9P,
            sMI11TPackages, MODEL_MI11T,
	    sMI13PPackages, MODEL_MI13P,
	    sK30UPackages, MODEL_K30U
        ).forEach((k, v) -> k.forEach(p -> sPackagesModelMap.put(p, v)));
    }

    public static void setProps(Context context) {
        final String packageName = context.getPackageName();

        if (TextUtils.isEmpty(packageName)) {
            return;
        }

        if (sPackagesModelMap.containsKey(packageName) && Secure.getInt(
                context.getContentResolver(), Secure.GAMES_DEVICE_SPOOF, 0) == 1) {
            String model = sPackagesModelMap.get(packageName);
            dlog("Spoofing model to " + model + " for package " + packageName);
            setPropValue("MODEL", model);
        }
    }
}
