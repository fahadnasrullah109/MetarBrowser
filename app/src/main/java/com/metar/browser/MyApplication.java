package com.metar.browser;

import android.app.Application;
import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.metar.browser.syncing.SyncWorker;
import com.metar.browser.utils.NetworkHelper;

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new NetworkHelper().setContext(this);
        scheduleWork(getApplicationContext(), SyncWorker.class.getSimpleName());
    }

    public static void scheduleWork(Context context, String tag) {
        Constraints constraint = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest.Builder photoCheckBuilder =
                new PeriodicWorkRequest.Builder(SyncWorker.class, 1, TimeUnit.HOURS);
        PeriodicWorkRequest request = photoCheckBuilder.setConstraints(constraint).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}