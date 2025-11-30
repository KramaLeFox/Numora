package com.example.finalproject.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.utils.BottomNavHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvIncome, tvExpense, tvBalance;
    private TableLayout tableTransactions;
    private Button btnMore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);

        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tableTransactions = findViewById(R.id.transactionsLayout).findViewById(R.id.tableLayout);
        btnMore = findViewById(R.id.btnMore);

        PieChart pieChart = findViewById(R.id.pieChart);

        BottomNavHelper.setup(this);
        btnMore.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TransactionListActivity.class);
            startActivity(intent);
        });
        setupPieChart(pieChart);
        loadSummary();
        loadTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        dbHelper.insertDefaultCategoriesIfEmpty();

        loadSummary();

        PieChart pieChart = findViewById(R.id.pieChart);

        setupPieChart(pieChart);

        tableTransactions.removeAllViews();
        loadTransactions();
    }


    private void loadSummary() {
        double income = dbHelper.getTotalIncomeThisMonth();
        double expense = dbHelper.getTotalExpenseThisMonth();
        double balance = dbHelper.getBalanceThisMonth();

        android.util.Log.d("DB_LOG", "Income: " + income + " | Expense: " + expense + " | Balance: " + balance);

        tvIncome.setText(String.valueOf(income));
        tvExpense.setText(String.valueOf(expense));
        tvBalance.setText(String.valueOf(balance));
    }

    private void loadTransactions() {
        Cursor cursor = dbHelper.getTransactionsThisMonth(); // <- use this month only
        int count = 0;
        int maxTransactions = 5;

        while (cursor.moveToNext() && count < maxTransactions) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow("T_date"));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("T_amount"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("T_type"));

            TableRow row = new TableRow(this);
            row.setBackgroundResource(R.drawable.bg_table);

            // Date column
            TextView tvDate = new TextView(this);
            tvDate.setText(date);
            tvDate.setPadding(10, 10, 10, 10);
            tvDate.setTextColor(getResources().getColor(R.color.TextDarkGreen));
            tvDate.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            TableRow.LayoutParams lp1 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            tvDate.setLayoutParams(lp1);

            // Amount column
            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.valueOf(amount));
            tvAmount.setPadding(10, 10, 10, 10);
            tvAmount.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            if ("รายรับ".equalsIgnoreCase(type)) {
                tvAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            TableRow.LayoutParams lp2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            tvAmount.setLayoutParams(lp2);

            row.addView(tvDate);
            row.addView(tvAmount);

            tableTransactions.addView(row);
            count++;
        }

        cursor.close();
    }

    private void setupPieChart(PieChart pieChart) {
        Cursor cursor = dbHelper.getExpenseAndSavingsThisMonth(); // <- use this month only

        List<PieEntry> entries = new ArrayList<>();

        while (cursor.moveToNext()) {
            String category = cursor.getString(cursor.getColumnIndexOrThrow("C_name"));
            float amount = (float) cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            entries.add(new PieEntry(amount, category));
        }
        cursor.close();

        PieDataSet dataSet = new PieDataSet(entries, "ค่าใช้จ่ายตามประเภท");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("ค่าใช้จ่าย & ออมเงิน/ลงทุน");
        pieChart.setCenterTextSize(16f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
}