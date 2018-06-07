package com.rssheap.application;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.rssheap.utilities.AnalyticsTrackers;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Davor on 5.5.2016.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        Fabric.with(this, new Crashlytics());
        AnalyticsTrackers.initialize(getApplicationContext());
    }
}
