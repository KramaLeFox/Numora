package com.example.finalproject;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Date;

public class DailyWorker extends Worker {

    public DailyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("DailyWorker", "Worker executed at: " + new Date());
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        db.makeRecurringTransaction();
        return Result.success();
    }
}