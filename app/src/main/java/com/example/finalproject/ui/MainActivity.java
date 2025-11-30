package com.example.finalproject.ui;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;

import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.modules.DailyWorker;
import com.example.finalproject.R;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseHelper db = new DatabaseHelper(this);

        Button myButton = findViewById(R.id.Start_button);
        Button devButton = findViewById(R.id.Dev_button);

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        devButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.insertFake3MonthsTransactions(MainActivity.this); // <-- use MainActivity.this
            }
        });

        scheduleDailyWork();
    }

    private void scheduleDailyWork() {
        PeriodicWorkRequest dailyWork =
                new PeriodicWorkRequest.Builder(DailyWorker.class, 1, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork(
                        "daily_work",
                        ExistingPeriodicWorkPolicy.KEEP,
                        dailyWork
                );
    }

}