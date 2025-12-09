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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvIncome, tvExpense, tvBalance;
    private TableLayout tableTransactions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);

        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tableTransactions = findViewById(R.id.transactionsLayout).findViewById(R.id.tableLayout);
        Button btnMore = findViewById(R.id.btnMore);

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

        tvIncome.setText("฿" + income);
        tvExpense.setText("฿" + expense);
        tvBalance.setText("฿" + balance);
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
            tvAmount.setText("฿" + amount);
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
        Cursor cursor = dbHelper.getExpenseAndSavingsThisMonth();

        List<PieEntry> entries = new ArrayList<>();

        while (cursor.moveToNext()) {
            String category = cursor.getString(cursor.getColumnIndexOrThrow("C_name"));
            float amount = (float) cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            entries.add(new PieEntry(amount, category));
        }
        cursor.close();

        PieDataSet dataSet = new PieDataSet(entries, "");

        // ---- Unique colors ----
        List<Integer> uniqueColors = new ArrayList<>();
        uniqueColors.add(Color.parseColor("#FF6B6B")); // red
        uniqueColors.add(Color.parseColor("#4ECDC4")); // teal
        uniqueColors.add(Color.parseColor("#556270")); // blue-gray
        uniqueColors.add(Color.parseColor("#C7F464")); // lime
        uniqueColors.add(Color.parseColor("#FFCC5C")); // yellow
        uniqueColors.add(Color.parseColor("#5DA5DA")); // blue
        uniqueColors.add(Color.parseColor("#B276B2")); // purple
        uniqueColors.add(Color.parseColor("#60BD68")); // green
        uniqueColors.add(Color.parseColor("#F17CB0")); // pink
        uniqueColors.add(Color.parseColor("#DECF3F")); // gold

        dataSet.setColors(uniqueColors);

        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);

        // ---- Add ฿ prefix ----
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "฿" + ((int) value);
            }
        });

        pieChart.setData(data);

        // ---- Center Text ----
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("ค่าใช้จ่าย");
        pieChart.setCenterTextSize(18f);
        pieChart.setCenterTextColor(Color.BLACK);

        // ---- Remove description label ----
        pieChart.getDescription().setEnabled(false);

        // ---- Legend settings ----
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setWordWrapEnabled(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChart.invalidate();
    }
}