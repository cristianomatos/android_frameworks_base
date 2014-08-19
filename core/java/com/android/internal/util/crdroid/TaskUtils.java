/*
 * Copyright (c) 2012-2013 The Linux Foundation. All rights reserved.
 * Not a Contribution.
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.internal.util.crdroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.util.Log;

import com.android.internal.R;

import java.util.List;

public class TaskUtils {

    public static void toggleLastAppImpl(final Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        final ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        String defaultHomePackage = "com.android.launcher";
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo != null && !res.activityInfo.packageName.equals("android")) {
            defaultHomePackage = res.activityInfo.packageName;
        }

        final List<ActivityManager.RecentTaskInfo> tasks =
                am.getRecentTasks(5, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        // lets get enough tasks to find something to switch to
        // Note, we'll only get as many as the system currently has - up to 5
        int lastAppId = 0;
        Intent lastAppIntent = null;
        for (int i = 1; i < tasks.size() && lastAppIntent == null; i++) {
            final String packageName = tasks.get(i).baseIntent.getComponent().getPackageName();
            if (!packageName.equals(defaultHomePackage) && !packageName.equals("com.android.systemui")) {
                final ActivityManager.RecentTaskInfo info = tasks.get(i);
                lastAppId = info.id;
                lastAppIntent = info.baseIntent;
            }
        }

        if (lastAppId > 0) {
            // Provide some animation on last app switch
            final ActivityOptions opts = ActivityOptions.makeCustomAnimation(context,
                        R.anim.last_app_in, R.anim.last_app_out);
            am.moveTaskToFront(lastAppId, am.MOVE_TASK_NO_USER_ACTION, opts.toBundle());
        } else if (lastAppIntent != null) {
            // last task is dead, restart it.
            lastAppIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            try {
                context.startActivityAsUser(lastAppIntent, UserHandle.CURRENT);
            } catch (ActivityNotFoundException e) {
                Log.w("Recent", "Unable to launch recent task", e);
            }
        }
    }
}
