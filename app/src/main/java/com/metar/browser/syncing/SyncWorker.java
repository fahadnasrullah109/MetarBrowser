package com.metar.browser.syncing;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.metar.browser.repository.SyncRepository;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        SyncRepository.getInstance(getApplicationContext()).syncDatabaseFromNetwork();
        return Result.success();
    }
}