package com.kongqw.androidbluetoothmanager;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by kongqingwei on 2017/3/15.
 * InitApplication
 */

public class InitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }
}