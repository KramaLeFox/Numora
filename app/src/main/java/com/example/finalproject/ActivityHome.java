package com.example.finalproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        /*
        db.execSQL("INSERT INTO Summaries (total_income, total_expense, balance) VALUES (45000, -38000, 7000);");
        */
        Date currentDate = Calendar.getInstance().getTime();

        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = df.format(currentDate);

        db.execSQL("INSERT INTO Transactions (category_id, tag_id, T_type, T_date, T_amount, T_note) " +
                "VALUES (1, 1, 'expense','" + formattedDate + "', -9000, 'ค่าเช่า');");
        db.execSQL("INSERT INTO Transactions (category_id, tag_id, T_type, T_date, T_amount, T_note) " +
                "VALUES (2, 1, 'income','" + formattedDate + "', 40000, 'เงินเดือน');");



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

            android.util.Log.d("DB_LOG", "Income: " + income + " | Expense: " + expense + " | Balance: " + balance);

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

        while (cursor.moveToNext()) {
            String note = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TYPE));

            android.util.Log.d("DB_LOG", "Transaction -> " +
                    "Type: " + type +
                    ", Note: " + note +
                    ", Date: " + date +
                    ", Amount: " + amount);

            TableRow row = new TableRow(this);

            row.setBackgroundResource(R.drawable.bg_table);

            TextView tvNote = new TextView(this);
            tvNote.setText(note);
            tvNote.setPadding(5, 5, 5, 5);
            tvNote.setTextColor(getResources().getColor(R.color.TextDarkGreen));

            TextView tvDate = new TextView(this);
            tvDate.setText(date);
            tvDate.setPadding(5, 5, 5, 5);
            tvDate.setTextColor(getResources().getColor(R.color.TextDarkGreen));

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
