package com.example.finalproject.ui;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.models.TargetModel;
import com.example.finalproject.modules.TargetModule;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class TargetsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private Button btnAddGoal, btnEditGoal;
    private TextView tvIncomeSummary;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets);

        pieChart = findViewById(R.id.pieChartDefaultGoal);
        btnAddGoal = findViewById(R.id.btnAddGoal);
        btnEditGoal = findViewById(R.id.btnEditGoal);
        tvIncomeSummary = findViewById(R.id.tvIncomeSummary);
        dbHelper = new DatabaseHelper(this);

        loadDefaultGoalChart();

        btnAddGoal.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("เพิ่มเป้าหมาย");

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_target, null);
            EditText etAmount = dialogView.findViewById(R.id.etTargetAmount);
            EditText etPercentage = dialogView.findViewById(R.id.etTargetPercentage);
            EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
            EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
            Spinner spType = dialogView.findViewById(R.id.spTargetType);

            builder.setView(dialogView);

            builder.setPositiveButton("บันทึก", (dialog, which) -> {
                String type = spType.getSelectedItem().toString();
                double amount = 0;
                double percentage = 0;

                try {
                    amount = Double.parseDouble(etAmount.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "กรุณากรอกจำนวนเงินที่ถูกต้อง", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    percentage = Double.parseDouble(etPercentage.getText().toString());
                } catch (NumberFormatException ignored) {
                }

                String startDate = etStartDate.getText().toString();
                String endDate = etEndDate.getText().toString();

                TargetModel target = new TargetModel(type, 1, null, amount, percentage, startDate, endDate);
                TargetModule module = new TargetModule(this);
                long id = module.addTarget(target);

                if (id > 0) Toast.makeText(this, "เพิ่มเป้าหมายสำเร็จ", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "ไม่สามารถเพิ่มเป้าหมายได้", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("ยกเลิก", null);
            builder.show();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultGoalChart();
    }

    private void loadDefaultGoalChart() {
        double predictedIncome = dbHelper.predictNextMonthIncome();
        double necessityExp = dbHelper.predictNextMonthNecessityExpense();
        double luxuryExp = dbHelper.predictNextMonthLuxuryExpense();
        double savingExp = dbHelper.predictNextMonthSavingExpense();

        if (predictedIncome <= 0) {
            Toast.makeText(this, "ไม่สามารถคำนวณได้ (รายรับเป็นศูนย์)", Toast.LENGTH_SHORT).show();
            pieChart.clear();
            tvIncomeSummary.setText("ไม่มีข้อมูลรายรับเพียงพอ");
            return;
        }

        // Target 50/30/20
        double targetNec = predictedIncome * 0.5;
        double targetLux = predictedIncome * 0.3;
        double targetSav = predictedIncome * 0.2;

        // Calculate unspent
        double unspentNec = Math.max(0, targetNec - necessityExp);
        double unspentLux = Math.max(0, targetLux - luxuryExp);
        double unspentSav = Math.max(0, targetSav - savingExp);

        // Percentages relative to predicted income
        double necessityPercent = (necessityExp / predictedIncome) * 100;
        double luxuryPercent = (luxuryExp / predictedIncome) * 100;
        double savingPercent = (savingExp / predictedIncome) * 100;

        double unspentNecPercent = (unspentNec / predictedIncome) * 100;
        double unspentLuxPercent = (unspentLux / predictedIncome) * 100;
        double unspentSavPercent = (unspentSav / predictedIncome) * 100;

        ArrayList<PieEntry> entries = new ArrayList<>();

        // Add slices with your calculated percentages
        entries.add(new PieEntry((float) necessityPercent, "จำเป็น"));
        if (unspentNecPercent > 0)
            entries.add(new PieEntry((float) unspentNecPercent, "จำเป็น (เหลือ)"));

        entries.add(new PieEntry((float) luxuryPercent, "ฟุ่มเฟือย"));
        if (unspentLuxPercent > 0)
            entries.add(new PieEntry((float) unspentLuxPercent, "ฟุ่มเฟือย (เหลือ)"));

        entries.add(new PieEntry((float) savingPercent, "ออม/ลงทุน"));
        if (unspentSavPercent > 0)
            entries.add(new PieEntry((float) unspentSavPercent, "ออม/ลงทุน (เหลือ)"));

        PieDataSet dataSet = new PieDataSet(entries, "การใช้จ่ายเทียบกับเป้าหมาย 50/30/20");
        dataSet.setColors(
                Color.rgb(102, 187, 106),   // Necessity
                Color.rgb(178, 255, 89),    // Unspent necessity
                Color.rgb(255, 202, 40),    // Luxury
                Color.rgb(255, 241, 118),   // Unspent luxury
                Color.rgb(66, 165, 245),    // Saving
                Color.rgb(144, 202, 249)    // Unspent saving
        );
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                String label = entry.getLabel();
                if (label.equals("จำเป็น") && value > 50f)
                    return String.format("%.1f%% (+%.1f%%)", value, value - 50);
                else if (label.equals("ฟุ่มเฟือย") && value > 30f)
                    return String.format("%.1f%% (+%.1f%%)", value, value - 30);
                else if (label.equals("ออม/ลงทุน") && value > 20f)
                    return String.format("%.1f%% (+%.1f%%)", value, value - 20);
                else
                    return String.format("%.1f%%", value);
            }
        });

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(false); // <-- your change here
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("เป้าหมาย 50 / 30 / 20");
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(1200);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);

        pieChart.invalidate();

        // Summary text
        String summary = String.format(
                "รายรับที่คาดการณ์: ฿%,.2f\n" +
                        "จำเป็น: ฿%,.2f (%.1f%%)%s\n" +
                        "ฟุ่มเฟือย: ฿%,.2f (%.1f%%)%s\n" +
                        "ออม/ลงทุน: ฿%,.2f (%.1f%%)%s\n\n" +
                        "เหลือจำเป็น: ฿%,.2f | เหลือฟุ่มเฟือย: ฿%,.2f | เหลือออม: ฿%,.2f",
                predictedIncome,
                necessityExp, necessityPercent, necessityPercent > 50 ? String.format(" (+%.1f%%)", necessityPercent - 50) : "",
                luxuryExp, luxuryPercent, luxuryPercent > 30 ? String.format(" (+%.1f%%)", luxuryPercent - 30) : "",
                savingExp, savingPercent, savingPercent > 20 ? String.format(" (+%.1f%%)", savingPercent - 20) : "",
                unspentNec, unspentLux, unspentSav
        );
        tvIncomeSummary.setText(summary);
    }

}