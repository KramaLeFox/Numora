package com.example.finalproject.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.finalproject.R;
import com.example.finalproject.ui.AddTransactionActivity;
import com.example.finalproject.ui.HomeActivity;
import com.example.finalproject.ui.TargetsActivity;
import com.example.finalproject.ui.TransactionListActivity;
import com.example.finalproject.ui.ReportActivity;

public class BottomNavHelper {

    public static void setup(Activity activity) {
        View btnNav1 = activity.findViewById(R.id.Transaction_btn);
        View btnNav2 = activity.findViewById(R.id.btnNav2);
        View btnNav3 = activity.findViewById(R.id.btnNav3);
        View btnNav4 = activity.findViewById(R.id.btnNav4);
        View btnHome = activity.findViewById(R.id.btnNavHome);

        if (btnNav1 != null) {
            btnNav1.setOnClickListener(v -> {
                Intent intent = new Intent(activity, AddTransactionActivity.class);
                activity.startActivity(intent);
            });
        }

        if (btnNav2 != null) {
            btnNav2.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ReportActivity.class);
                activity.startActivity(intent);
            });
        }

        if (btnNav3 != null) {
            btnNav3.setOnClickListener(v -> {
                Intent intent = new Intent(activity, TargetsActivity.class);
                activity.startActivity(intent);
            });
        }

        if (btnNav4 != null) {
            btnNav4.setOnClickListener(v -> {
                Intent intent = new Intent(activity, TransactionListActivity.class);
                activity.startActivity(intent);
            });
        }

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(activity, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            });
        }
    }
}