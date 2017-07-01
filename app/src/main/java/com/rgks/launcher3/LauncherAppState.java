/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.rgks.launcher3;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.rgks.launcher3.accessibility.LauncherAccessibilityDelegate;
import com.rgks.launcher3.compat.LauncherAppsCompat;
import com.rgks.launcher3.compat.PackageInstallerCompat;
import com.rgks.launcher3.compat.UserManagerCompat;
import com.rgks.launcher3.config.FeatureFlags;
import com.rgks.launcher3.util.ConfigMonitor;
import com.rgks.launcher3.util.TestingUtils;
import com.rgks.launcher3.util.Thunk;

import java.lang.ref.WeakReference;

import static com.rgks.launcher3.LauncherFiles.SHARED_PREFERENCES_KEY;

public class LauncherAppState {

    private final AppFilter mAppFilter;
    @Thunk
    final LauncherModel mModel;
    private final IconCache mIconCache;
    private final WidgetPreviewLoader mWidgetCache;
    private boolean mIsScreenLarge;
    private boolean mWallpaperChangedSinceLastCheck;
    private RgkWidgetPreviewLoader.CacheDb mWidgetPreviewCacheDb;
    private static WeakReference<LauncherProvider> sLauncherProvider;
    private static Context sContext;

    private static LauncherAppState INSTANCE;

    private InvariantDeviceProfile mInvariantDeviceProfile;

    private LauncherAccessibilityDelegate mAccessibilityDelegate;

    public static LauncherAppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherAppState();
        }
        return INSTANCE;
    }

    public static String getSharedPreferencesKey() {
        return SHARED_PREFERENCES_KEY;
    }

    public static LauncherAppState getInstanceNoCreate() {
        return INSTANCE;
    }

    public Context getContext() {
        return sContext;
    }

    public static void setApplicationContext(Context context) {
        if (sContext != null) {
            Log.w(Launcher.TAG, "setApplicationContext called twice! old=" + sContext + " new=" + context);
        }
        sContext = context.getApplicationContext();
    }
    public boolean isScreenLarge() {
        return mIsScreenLarge;
    }
    private LauncherAppState() {
        if (sContext == null) {
            throw new IllegalStateException("LauncherAppState inited before app context set");
        }

        Log.v(Launcher.TAG, "LauncherAppState inited");

        if (TestingUtils.MEMORY_DUMP_ENABLED) {
            TestingUtils.startTrackingMemory(sContext);
        }
        mIsScreenLarge = isScreenLarge(sContext.getResources());
        mInvariantDeviceProfile = new InvariantDeviceProfile(sContext);
        mIconCache = new IconCache(sContext, mInvariantDeviceProfile);
        mWidgetCache = new WidgetPreviewLoader(sContext, mIconCache);
        recreateWidgetPreviewDb();
        mAppFilter = AppFilter.loadByName(sContext.getString(R.string.app_filter_class));
        mModel = new LauncherModel(this, mIconCache, mAppFilter);

        LauncherAppsCompat.getInstance(sContext).addOnAppsChangedCallback(mModel);

        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(SearchManager.INTENT_GLOBAL_SEARCH_ACTIVITY_CHANGED);
        // For handling managed profiles
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_ADDED);
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_REMOVED);
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_AVAILABLE);
        filter.addAction(LauncherAppsCompat.ACTION_MANAGED_PROFILE_UNAVAILABLE);

        sContext.registerReceiver(mModel, filter);
        UserManagerCompat.getInstance(sContext).enableAndResetCache();
        new ConfigMonitor(sContext).register();

        sContext.registerReceiver(
                new WallpaperChangedReceiver(), new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));
    }
    public static boolean isScreenLarge(Resources res) {
        return res.getBoolean(R.bool.is_large_tablet);
    }
    /**
     * Call from Application.onTerminate(), which is not guaranteed to ever be called.
     */
    public void onTerminate() {
        sContext.unregisterReceiver(mModel);
        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(sContext);
        launcherApps.removeOnAppsChangedCallback(mModel);
        PackageInstallerCompat.getInstance(sContext).onStop();
    }

    /**
     * Reloads the workspace items from the DB and re-binds the workspace. This should generally
     * not be called as DB updates are automatically followed by UI update
     */
    public void reloadWorkspace() {
        mModel.resetLoadedState(false, true);
        mModel.startLoaderFromBackground();
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
    }

    LauncherModel setLauncher(Launcher launcher) {
        getLauncherProvider().setLauncherProviderChangeListener(launcher);
        mModel.initialize(launcher);
        mAccessibilityDelegate = ((launcher != null) && Utilities.ATLEAST_LOLLIPOP) ?
                new LauncherAccessibilityDelegate(launcher) : null;
        return mModel;
    }

    public LauncherAccessibilityDelegate getAccessibilityDelegate() {
        return mAccessibilityDelegate;
    }

    public IconCache getIconCache() {
        return mIconCache;
    }

    public LauncherModel getModel() {
        return mModel;
    }

    static void setLauncherProvider(LauncherProvider provider) {
        sLauncherProvider = new WeakReference<LauncherProvider>(provider);
    }

    public static LauncherProvider getLauncherProvider() {
        return sLauncherProvider.get();
    }

    public WidgetPreviewLoader getWidgetCache() {
        return mWidgetCache;
    }

    public void onWallpaperChanged() {
        mWallpaperChangedSinceLastCheck = true;
    }

    public boolean hasWallpaperChangedSinceLastCheck() {
        boolean result = mWallpaperChangedSinceLastCheck;
        mWallpaperChangedSinceLastCheck = false;
        return result;
    }

    public InvariantDeviceProfile getInvariantDeviceProfile() {
        return mInvariantDeviceProfile;
    }

    public static boolean isDogfoodBuild() {
        return FeatureFlags.IS_ALPHA_BUILD || FeatureFlags.IS_DEV_BUILD;
    }

    //add lallapp code by luoran 20170313 start
    public static boolean isLRAllApp() {
        return true;
//        return false;
    }

    public void recreateWidgetPreviewDb() {
        if (mWidgetPreviewCacheDb != null) {
            mWidgetPreviewCacheDb.close();
        }
        mWidgetPreviewCacheDb = new RgkWidgetPreviewLoader.CacheDb(sContext);
    }

    RgkWidgetPreviewLoader.CacheDb getWidgetPreviewCacheDb() {
        return mWidgetPreviewCacheDb;
    }

    public static boolean isLAllappWhiteBG() {
        return false;
    }

    public static boolean isDisableAllApps() {
        return false;
    }

    boolean shouldShowAppOrWidgetProvider(ComponentName componentName) {
        return mAppFilter == null || mAppFilter.shouldShowApp(componentName);
    }
    //add lallapp code by luoran 20170313 end
}
