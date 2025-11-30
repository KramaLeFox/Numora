package com.example.finalproject.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.models.TargetModel;
import com.example.finalproject.modules.TargetModule;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class TargetsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private Button btnAddGoal, btnConfigGoal;
    private TextView tvIncomeSummary;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets);

        pieChart = findViewById(R.id.pieChartDefaultGoal);
        btnAddGoal = findViewById(R.id.btnAddGoal);
        btnConfigGoal = findViewById(R.id.btnConfigGoal);
        tvIncomeSummary = findViewById(R.id.tvIncomeSummary);
        dbHelper = new DatabaseHelper(this);

        loadDefaultGoalChart();

        btnAddGoal.setOnClickListener(v -> showAddTargetDialog());
        btnConfigGoal.setOnClickListener(v -> showConfigDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultGoalChart();
    }

    private void showAddTargetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("เพิ่มเป้าหมาย");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_target, null);
        EditText etAmount = dialogView.findViewById(R.id.etTargetAmount);
        EditText etPercentage = dialogView.findViewById(R.id.etTargetPercentage);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        Spinner spType = dialogView.findViewById(R.id.spTargetType);
        TextView tvCustomCategoryLabel = dialogView.findViewById(R.id.tvCustomCategoryLabel);
        EditText etCustomCategory = dialogView.findViewById(R.id.etCustomCategory);

        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean isCustom = spType.getSelectedItem().toString().equals("Use less money in category…");
                tvCustomCategoryLabel.setVisibility(isCustom ? View.VISIBLE : View.GONE);
                etCustomCategory.setVisibility(isCustom ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setView(dialogView);

        builder.setPositiveButton("บันทึก", (dialog, which) -> {
            String type = spType.getSelectedItem().toString();
            double amount = 0;
            double percentage = 0;
            String category = null;

            try {
                amount = Double.parseDouble(etAmount.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "กรุณากรอกจำนวนเงินที่ถูกต้อง", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                percentage = Double.parseDouble(etPercentage.getText().toString());
            } catch (NumberFormatException ignored) {}

            String startDate = etStartDate.getText().toString();
            String endDate = etEndDate.getText().toString();

            if (type.equals("Use less money in category…")) {
                category = etCustomCategory.getText().toString().trim();
                if (category.isEmpty()) {
                    Toast.makeText(this, "กรุณากรอกหมวดหมู่", Toast.LENGTH_SHORT).show();
                    return;
                }
                type += ": " + category;
            }

            TargetModel target = new TargetModel(type, 1, null, amount, percentage, startDate, endDate);
            TargetModule module = new TargetModule(this);
            long id = module.addTarget(target);

            Toast.makeText(this,
                    id > 0 ? "เพิ่มเป้าหมายสำเร็จ" : "ไม่สามารถเพิ่มเป้าหมายได้",
                    Toast.LENGTH_SHORT).show();

            loadDefaultGoalChart(); // update chart after adding
        });

        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }

    private void loadDefaultGoalChart() {
        double predictedIncome = dbHelper.getTotalIncomeThisMonth();
        double necessityExp = dbHelper.getTotalNecessityThisMonth();
        double luxuryExp = dbHelper.getTotalLuxuryThisMonth();
        double savingExp = dbHelper.getTotalSavingThisMonth();

        Log.d("DB_DEBUG", "TotalNecessityThisMonth = " + dbHelper.getTotalNecessityThisMonth());
        Log.d("DB_DEBUG", "TotalLuxuryThisMonth = " + dbHelper.getTotalLuxuryThisMonth());
        Log.d("DB_DEBUG", "TotalSavingThisMonth = " + dbHelper.getTotalSavingThisMonth());
        Log.d("DB_DEBUG", "TotalIncomeThisMonth = " + dbHelper.getTotalIncomeThisMonth());


        if (predictedIncome <= 0) {
            Toast.makeText(this, "ไม่สามารถคำนวณได้ (รายรับเป็นศูนย์)", Toast.LENGTH_SHORT).show();
            pieChart.clear();
            tvIncomeSummary.setText("ไม่มีข้อมูลรายรับเพียงพอ");
            return;
        }

        float necPercent = getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE).getFloat("Necessity", 50f);
        float luxPercent = getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE).getFloat("Luxury", 30f);
        float savPercent = getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE).getFloat("Saving", 20f);

        double targetNec = predictedIncome * necPercent / 100;
        double targetLux = predictedIncome * luxPercent / 100;
        double targetSav = predictedIncome * savPercent / 100;

        double unspentNec = Math.max(0, targetNec - necessityExp);
        double unspentLux = Math.max(0, targetLux - luxuryExp);
        double unspentSav = Math.max(0, targetSav - savingExp);

        double necessityPercent = (necessityExp / predictedIncome) * 100;
        double luxuryPercent = (luxuryExp / predictedIncome) * 100;
        double savingPercent = (savingExp / predictedIncome) * 100;

        double unspentNecPercent = (unspentNec / predictedIncome) * 100;
        double unspentLuxPercent = (unspentLux / predictedIncome) * 100;
        double unspentSavPercent = (unspentSav / predictedIncome) * 100;

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) necessityPercent, "จำเป็น"));
        if (unspentNecPercent > 0) entries.add(new PieEntry((float) unspentNecPercent, "จำเป็น (เหลือ)"));
        entries.add(new PieEntry((float) luxuryPercent, "ฟุ่มเฟือย"));
        if (unspentLuxPercent > 0) entries.add(new PieEntry((float) unspentLuxPercent, "ฟุ่มเฟือย (เหลือ)"));
        entries.add(new PieEntry((float) savingPercent, "ออม/ลงทุน"));
        if (unspentSavPercent > 0) entries.add(new PieEntry((float) unspentSavPercent, "ออม/ลงทุน (เหลือ)"));

        PieDataSet dataSet = new PieDataSet(entries, "เป้าหมาย");
        dataSet.setColors(
                Color.rgb(102, 187, 106),
                Color.rgb(178, 255, 89),
                Color.rgb(255, 202, 40),
                Color.rgb(255, 241, 118),
                Color.rgb(66, 165, 245),
                Color.rgb(144, 202, 249)
        );
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                return String.format("%.1f%%", value);
            }
        });

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("เป้าหมาย");
        pieChart.setCenterTextSize(16f);
        pieChart.setDrawEntryLabels(false);
        pieChart.animateY(1200);
        pieChart.getLegend().setEnabled(false);

        LinearLayout legendLayout = findViewById(R.id.layoutCustomLegend);
        legendLayout.removeAllViews();
        addLegendItem(legendLayout, "จำเป็น", Color.rgb(102, 187, 106));
        addLegendItem(legendLayout, "จำเป็น (เหลือ)", Color.rgb(178, 255, 89));
        addLegendItem(legendLayout, "ฟุ่มเฟือย", Color.rgb(255, 202, 40));
        addLegendItem(legendLayout, "ฟุ่มเฟือย (เหลือ)", Color.rgb(255, 241, 118));
        addLegendItem(legendLayout, "ออม/ลงทุน", Color.rgb(66, 165, 245));
        addLegendItem(legendLayout, "ออม/ลงทุน (เหลือ)", Color.rgb(144, 202, 249));

        pieChart.invalidate();

        // summary
        SpannableStringBuilder summaryBuilder = new SpannableStringBuilder();
        summaryBuilder.append(String.format("รายรับเดือนนี้: ฿%,.2f\n\n", predictedIncome));
        appendSummaryLine(summaryBuilder, "รายจ่ายจำเป็น", targetNec, necessityExp, false);
        appendSummaryLine(summaryBuilder, "รายจ่ายฟุ่มเฟือย", targetLux, luxuryExp, false);
        appendSummaryLine(summaryBuilder, "ออม/ลงทุน", targetSav, savingExp, true);
        tvIncomeSummary.setText(summaryBuilder);
    }

    private void appendSummaryLine(SpannableStringBuilder builder, String title, double target, double actual, boolean flipLogic) {

        double diff = Math.abs(target - actual);
        boolean isOver = actual > target;
        String label = isOver ? "เกิน" : "เหลือ";

        // Pick correct color depending on category logic
        int color;
        if (flipLogic) {
            // Saving category: "overspend" is GOOD
            color = isOver ? Color.parseColor("#388E3C") : Color.RED;
        } else {
            // Normal categories: overspend BAD
            color = isOver ? Color.RED : Color.parseColor("#388E3C");
        }

        String text = String.format(
                "%s: คาดการณ์: ฿%,.2f | ใช้แล้ว: ฿%,.2f | %s: ฿%,.2f\n",
                title, target, actual, label, diff
        );

        int start = builder.length();
        builder.append(text);

        // Find the label inside the newly appended text
        int labelStart = builder.toString().indexOf(label, start);
        int labelEnd = labelStart + label.length();

        // Only apply color if found
        if (labelStart >= 0) {
            builder.setSpan(
                    new ForegroundColorSpan(color),
                    labelStart,
                    labelEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private void addLegendItem(LinearLayout parent, String label, int color) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(4, 4, 4, 4);

        View colorBox = new View(this);
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(40, 40);
        boxParams.setMargins(8, 0, 16, 0);
        colorBox.setLayoutParams(boxParams);
        colorBox.setBackgroundColor(color);

        TextView text = new TextView(this);
        text.setText(label);
        text.setTextColor(Color.BLACK);
        text.setTextSize(14f);

        item.addView(colorBox);
        item.addView(text);
        parent.addView(item);
    }

    private void showConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ตั้งค่าเป้าหมายหลัก");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_config_goal, null);
        EditText etNec = dialogView.findViewById(R.id.etNecessityPercent);
        EditText etLux = dialogView.findViewById(R.id.etLuxuryPercent);
        EditText etSav = dialogView.findViewById(R.id.etSavingPercent);
        Button btnRecommend = dialogView.findViewById(R.id.btnRecommend);

        etNec.setText(String.valueOf(getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE).getFloat("Necessity", 50f)));
        etLux.setText(String.valueOf(getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE).getFloat("Luxury", 30f)));
        etSav.setText(String.valueOf(getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE).getFloat("Saving", 20f)));

        btnRecommend.setOnClickListener(v -> {
            int monthCount = dbHelper.getDistinctMonthCount();
            if (monthCount < 3) {
                Toast.makeText(this, "ต้องมีข้อมูลธุรกรรมอย่างน้อย 3 เดือนเพื่อคำนวณคำแนะนำ", Toast.LENGTH_LONG).show();
                return;
            }

            double necCost = dbHelper.predictNextMonthNecessityExpense();
            double luxCost = dbHelper.predictNextMonthLuxuryExpense();
            double savCost = dbHelper.predictNextMonthSavingExpense();
            double total = necCost + luxCost + savCost;

            if (total <= 0) {
                Toast.makeText(this, "ไม่มีข้อมูลเพียงพอสำหรับคำแนะนำ", Toast.LENGTH_SHORT).show();
                return;
            }

            double necPercent = (necCost / total) * 100;
            double luxPercent = (luxCost / total) * 100;
            double savPercent = 100 - necPercent - luxPercent;

            etNec.setText(String.format("%.1f", necPercent));
            etLux.setText(String.format("%.1f", luxPercent));
            etSav.setText(String.format("%.1f", savPercent));
        });

        builder.setView(dialogView);
        builder.setPositiveButton("บันทึก", (dialog, which) -> {
            try {
                double nec = Double.parseDouble(etNec.getText().toString());
                double lux = Double.parseDouble(etLux.getText().toString());
                double sav = Double.parseDouble(etSav.getText().toString());
                double total = nec + lux + sav;
                if (total == 0) {
                    Toast.makeText(this, "ผลรวมต้องมากกว่า 0%", Toast.LENGTH_SHORT).show();
                    return;
                }

                nec = (nec / total) * 100.0;
                lux = (lux / total) * 100.0;
                sav = (sav / total) * 100.0;

                getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putFloat("Necessity", (float) nec)
                        .putFloat("Luxury", (float) lux)
                        .putFloat("Saving", (float) sav)
                        .apply();

                Toast.makeText(this, String.format("บันทึกเรียบร้อย (%.1f%% / %.1f%% / %.1f%%)", nec, lux, sav), Toast.LENGTH_SHORT).show();
                loadDefaultGoalChart();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "กรุณากรอกตัวเลขที่ถูกต้อง", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("ยกเลิก", null);
        builder.show();
    }
}