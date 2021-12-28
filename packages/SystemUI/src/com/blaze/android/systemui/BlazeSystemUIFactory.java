package com.blaze.android.systemui;

import android.content.Context;

import com.blaze.android.systemui.dagger.BlazeGlobalRootComponent;
import com.blaze.android.systemui.dagger.DaggerBlazeGlobalRootComponent;

import com.android.systemui.SystemUIFactory;
import com.android.systemui.dagger.GlobalRootComponent;

public class BlazeSystemUIFactory extends SystemUIFactory {
    @Override
    protected GlobalRootComponent buildGlobalRootComponent(Context context) {
        return DaggerBlazeGlobalRootComponent.builder()
                .context(context)
                .build();
    }
}
