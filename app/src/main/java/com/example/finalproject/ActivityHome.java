package com.example.finalproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityHome extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvIncome, tvExpense, tvBalance;
    private TableLayout tableTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);

        // Link TextViews
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tableTransactions = findViewById(R.id.transactionsLayout).findViewById(R.id.tableLayout);

        loadSummary();
        loadTransactions();
    }

    private void loadSummary() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_SUMMARIES +
                " ORDER BY " + DatabaseHelper.COL_SUMMARY_ID + " DESC LIMIT 1", null);

        if (cursor.moveToFirst()) {
            double income = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL_INCOME));
            double expense = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL_EXPENSE));
            double balance = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BALANCE));

            tvIncome.setText(String.valueOf(income));
            tvExpense.setText(String.valueOf(expense));
            tvBalance.setText(String.valueOf(balance));
        }
        cursor.close();
    }

    private void loadTransactions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " ORDER BY " + DatabaseHelper.COL_TRANSACTION_ID + " DESC LIMIT 5", null);

        // Skip first row (the header you already defined in XML)
        while (cursor.moveToNext()) {
            String note = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE));

            TableRow row = new TableRow(this);

            TextView tvNote = new TextView(this);
            tvNote.setText(note);
            tvNote.setPadding(5, 5, 5, 5);

            TextView tvDate = new TextView(this);
            tvDate.setText(date);
            tvDate.setPadding(5, 5, 5, 5);

            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.valueOf(amount));
            tvAmount.setPadding(5, 5, 5, 5);
            if ("income".equalsIgnoreCase(type)) {
                tvAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }

            row.addView(tvNote);
            row.addView(tvDate);
            row.addView(tvAmount);

            tableTransactions.addView(row);
        }

        cursor.close();
    }
}
