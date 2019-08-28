package com.example.platypus.kitkat_storage_poc;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import timber.log.Timber;

public class TestApp extends Application {

    @SuppressLint("StaticFieldLeak")
    // A context leak happening at app level isn't _really_ a leak, right ? ;-)
    private static Context instance;

    public static Context getAppContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Timber
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
}
