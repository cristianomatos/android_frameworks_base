package com.android.internal.util.cr;

import android.content.Context;

public class CrUtils {

    public static boolean isNavBarDefault(Context context) {
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
    }
}
