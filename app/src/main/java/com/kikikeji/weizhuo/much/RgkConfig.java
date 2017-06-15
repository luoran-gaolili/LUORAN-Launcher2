
package com.kikikeji.weizhuo.much;


import android.content.Context;

public final class RgkConfig {
    public static final boolean SUPPORT_MUCH_STYLE = true;
    public static final String SCREEN_EFFECT_PREFS = "screenEffect";
    public static final String LAUNCHER_PREFS = "com.android.launcher3.prefs";
    private static RgkConfig sInstatnce;
    private Context mContext;

    public static RgkConfig getInstance() {
        if (sInstatnce == null) {
            throw new IllegalAccessError("init first !!!");
        }
        return sInstatnce;
    }

    private RgkConfig(Context context) {
        mContext = context;
    }


}
