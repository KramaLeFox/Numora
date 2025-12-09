package com.example.finalproject.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.models.TargetModel;
import com.example.finalproject.models.UsableBudget;
import com.example.finalproject.modules.TargetModule;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class TargetsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private Button btnAddGoal, btnConfigGoal;
    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets);

        pieChart = findViewById(R.id.pieChartDefaultGoal);
        btnAddGoal = findViewById(R.id.btnAddGoal);
        btnConfigGoal = findViewById(R.id.btnConfigGoal);
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

        // Show or hide custom input
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean isCustom = spType.getSelectedItem().toString().equals("Use less money in category…");
                tvCustomCategoryLabel.setVisibility(isCustom ? View.VISIBLE : View.GONE);
                etCustomCategory.setVisibility(isCustom ? View.VISIBLE : View.GONE);
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setView(dialogView);

        // Disable auto-close
        builder.setPositiveButton("บันทึก", null);
        builder.setNegativeButton("ยกเลิก", null);

        AlertDialog dialog = builder.show();

        Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Real-time reset of highlighting
        TextWatcher resetWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                etAmount.setBackgroundColor(Color.TRANSPARENT);
                etPercentage.setBackgroundColor(Color.TRANSPARENT);
                etCustomCategory.setBackgroundColor(Color.TRANSPARENT);
            }
        };

        etAmount.addTextChangedListener(resetWatcher);
        etPercentage.addTextChangedListener(resetWatcher);
        etCustomCategory.addTextChangedListener(resetWatcher);

        btnSave.setOnClickListener(v -> {
            boolean valid = true;

            double amount = 0;
            double percentage = 0;
            String type = spType.getSelectedItem().toString();
            String category = null;

            // Validate amount
            try {
                amount = Double.parseDouble(etAmount.getText().toString().trim());
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                etAmount.setBackgroundColor(Color.argb(80, 255, 0, 0));
                Toast.makeText(this, "จำนวนเงินต้องเป็นตัวเลขและมากกว่า 0", Toast.LENGTH_SHORT).show();
                valid = false;
            }

            // Validate percentage (optional)
            try {
                if (!etPercentage.getText().toString().trim().isEmpty()) {
                    percentage = Double.parseDouble(etPercentage.getText().toString().trim());
                    if (percentage < 0) throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                etPercentage.setBackgroundColor(Color.argb(80, 255, 0, 0));
                Toast.makeText(this, "เปอร์เซ็นต์ต้องเป็นตัวเลขที่ถูกต้อง", Toast.LENGTH_SHORT).show();
                valid = false;
            }

            // Validate custom category
            if (type.equals("Use less money in category…")) {
                category = etCustomCategory.getText().toString().trim();
                if (category.isEmpty()) {
                    etCustomCategory.setBackgroundColor(Color.argb(80, 255, 0, 0));
                    Toast.makeText(this, "กรุณากรอกหมวดหมู่", Toast.LENGTH_SHORT).show();
                    valid = false;
                } else {
                    type += ": " + category;
                }
            }

            if (!valid) return; // Don’t close the dialog if invalid

            String startDate = etStartDate.getText().toString();
            String endDate = etEndDate.getText().toString();

            // Save the target
            TargetModel target = new TargetModel(type, 1, null, amount, percentage, startDate, endDate);
            TargetModule module = new TargetModule(this);
            long id = module.addTarget(target);

            Toast.makeText(this,
                    id > 0 ? "เพิ่มเป้าหมายสำเร็จ" : "ไม่สามารถเพิ่มเป้าหมายได้",
                    Toast.LENGTH_SHORT).show();

            loadDefaultGoalChart();
            dialog.dismiss(); // Only close when everything is valid
        });
    }

    private void loadDefaultGoalChart() {
        // 1. Retrieve the income mode setting
        String mode = getSharedPreferences("GoalPrefs", MODE_PRIVATE)
                .getString("IncomeMode", "predicted");

        // 2. Get the necessary income and expense figures
        double actualIncomeThisMonth = dbHelper.getTotalIncomeThisMonth();
        double predictedIncomeNextMonth = dbHelper.predictNextMonthIncome();

        double necessityExp = dbHelper.getTotalNecessityThisMonth();
        double luxuryExp = dbHelper.getTotalLuxuryThisMonth();
        double savingExp = dbHelper.getTotalSavingThisMonth();

        // 3. Determine the base income for goal calculations
        double baseIncome = mode.equals("predicted")
                ? predictedIncomeNextMonth
                : actualIncomeThisMonth;

        if (baseIncome <= 0) {
            Toast.makeText(this, "ไม่สามารถคำนวณได้ (รายรับเป็นศูนย์)", Toast.LENGTH_SHORT).show();
            pieChart.clear();
            return;
        }

        // 4. Retrieve the target percentages (50/30/20 rule)
        float necPercentConfig = getSharedPreferences("GoalPrefs", MODE_PRIVATE).getFloat("Necessity", 50f);
        float luxPercentConfig = getSharedPreferences("GoalPrefs", MODE_PRIVATE).getFloat("Luxury", 30f);
        float savPercentConfig = getSharedPreferences("GoalPrefs", MODE_PRIVATE).getFloat("Saving", 20f);

        // 5. Calculate Target Amounts based on baseIncome
        double targetNec = baseIncome * necPercentConfig / 100;
        double targetLux = baseIncome * luxPercentConfig / 100;
        double targetSav = baseIncome * savPercentConfig / 100;

        // 6. Calculate Unspent/Remaining Amounts (used only for Pie Chart data)
        double unspentNec = Math.max(0, targetNec - necessityExp);
        double unspentLux = Math.max(0, targetLux - luxuryExp);
        double unspentSav = Math.max(0, targetSav - savingExp);

        // 7. Calculate Actual Percentages based on baseIncome
        double necessityPercent = (necessityExp / baseIncome) * 100;
        double luxuryPercent = (luxuryExp / baseIncome) * 100;
        double savingPercent = (savingExp / baseIncome) * 100;

        double unspentNecPercent = (unspentNec / baseIncome) * 100;
        double unspentLuxPercent = (unspentLux / baseIncome) * 100;
        double unspentSavPercent = (unspentSav / baseIncome) * 100;

        // 8. Calculate Raw and Cascading Usable Budget for Table Logic
        UsableBudget rawUsable = calculateTotalUsable(targetNec, necessityExp, targetLux, luxuryExp, targetSav, savingExp);
        UsableBudget adjustedUsable = calculateCascadingUsable(rawUsable);

        // --- PIE CHART SETUP ---
        ArrayList<PieEntry> entries = new ArrayList<>();

        // Add spent percentages
        if (necessityPercent > 0) entries.add(new PieEntry((float) necessityPercent, "จำเป็น"));
        if (luxuryPercent > 0) entries.add(new PieEntry((float) luxuryPercent, "ฟุ่มเฟือย"));
        if (savingPercent > 0) entries.add(new PieEntry((float) savingPercent, "ออม/ลงทุน"));

        // Add unspent (remaining) percentages
        if (unspentNecPercent > 0) entries.add(new PieEntry((float) unspentNecPercent, "จำเป็น (เหลือ)"));
        if (unspentLuxPercent > 0) entries.add(new PieEntry((float) unspentLuxPercent, "ฟุ่มเฟือย (เหลือ)"));
        if (unspentSavPercent > 0) entries.add(new PieEntry((float) unspentSavPercent, "ออม/ลงทุน (เหลือ)"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.rgb(102, 187, 106),  // จำเป็น (เข้ม)
                Color.rgb(255, 202, 40),   // ฟุ่มเฟือย (เข้ม)
                Color.rgb(66, 165, 245),   // ออม/ลงทุน (เข้ม)
                Color.rgb(178, 255, 89),   // จำเป็น (เหลือ)
                Color.rgb(255, 241, 118),  // ฟุ่มเฟือย (เหลือ)
                Color.rgb(144, 202, 249)   // ออม/ลงทุน (เหลือ)
        );
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                // Only show labels for entries >= 1% for clarity
                return value >= 1.0f ? String.format("%.1f%%", value) : "";
            }
        });

        pieChart.setData(data);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("เป้าหมาย\n(ฐาน: " + (mode.equals("predicted") ? "คาดการณ์" : "จริง") + ")");
        pieChart.setCenterTextSize(14f);
        pieChart.setDrawEntryLabels(false);
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setWordWrapEnabled(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        pieChart.animateY(1200);
        pieChart.invalidate();

        // --- SUMMARY TABLE SETUP ---
        TableLayout table = findViewById(R.id.summaryTable);
        table.removeAllViews();

        // Header row
        addTableRow(table, "หมวดหมู่", "เป้าหมาย (฿)", "ใช้จริง (฿)", "ส่วนต่าง", false);

        // Get Target Percentages (as fraction)
        double targetNecPercent = necPercentConfig / 100.0;
        double targetLuxPercent = luxPercentConfig / 100.0;
        double targetSavPercent = savPercentConfig / 100.0;

        // Get Actual Percentages (as fraction)
        double actualNecPercent = necessityExp / baseIncome;
        double actualLuxPercent = luxuryExp / baseIncome;
        double actualSavPercent = savingExp / baseIncome;

        // --- จำเป็น ---
        addTableRow(table, "จำเป็น",
                formatMoney(targetNec),
                formatMoney(necessityExp),
                formatAdjustedDiff(targetNec, necessityExp, adjustedUsable.getUsableNecessity(), false),
                false);

        addTableRow(table, "จำเป็น (%)",
                String.format("%.1f%%", targetNecPercent * 100),
                String.format("%.1f%%", actualNecPercent * 100),
                formatDiffPercent(targetNecPercent, actualNecPercent, false),
                false);

        // --- ฟุ่มเฟือย ---
        addTableRow(table, "ฟุ่มเฟือย",
                formatMoney(targetLux),
                formatMoney(luxuryExp),
                formatAdjustedDiff(targetLux, luxuryExp, adjustedUsable.getUsableLuxury(), false),
                false);

        addTableRow(table, "ฟุ่มเฟือย (%)",
                String.format("%.1f%%", targetLuxPercent * 100),
                String.format("%.1f%%", actualLuxPercent * 100),
                formatDiffPercent(targetLuxPercent, actualLuxPercent, false),
                false);

        // --- ออม/ลงทุน ---
        addTableRow(table, "ออม/ลงทุน",
                formatMoney(targetSav),
                formatMoney(savingExp),
                formatAdjustedDiff(targetSav, savingExp, adjustedUsable.getUsableSaving(), true),
                true); // flip is true because actual > target is GOOD here

        addTableRow(table, "ออม/ลงทุน (%)",
                String.format("%.1f%%", targetSavPercent * 100),
                String.format("%.1f%%", actualSavPercent * 100),
                formatDiffPercent(targetSavPercent, actualSavPercent, true),
                true); // flip is true because actual > target is GOOD here
    }

    private void showConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ตั้งค่าเป้าหมายหลัก(เปอร์เซ็นต์)");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_config_goal, null);
        EditText etNec = dialogView.findViewById(R.id.etNecessityPercent);
        EditText etLux = dialogView.findViewById(R.id.etLuxuryPercent);
        EditText etSav = dialogView.findViewById(R.id.etSavingPercent);
        Button btnRecommend = dialogView.findViewById(R.id.btnRecommend);
        Spinner spIncomeSource = dialogView.findViewById(R.id.spIncomeMode);

        ArrayAdapter<String> incomeModeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"ใช้รายรับที่คาดการณ์", "ใช้รายรับจริง"}
        );
        incomeModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIncomeSource.setAdapter(incomeModeAdapter);

        // Load saved mode
        String mode = getSharedPreferences("GoalPrefs", MODE_PRIVATE)
                .getString("IncomeMode", "predicted");

        spIncomeSource.setSelection(mode.equals("predicted") ? 0 : 1);

        etNec.setText(String.valueOf(getSharedPreferences("GoalPrefs", MODE_PRIVATE).getFloat("Necessity", 50f)));
        etLux.setText(String.valueOf(getSharedPreferences("GoalPrefs", MODE_PRIVATE).getFloat("Luxury", 30f)));
        etSav.setText(String.valueOf(getSharedPreferences("GoalPrefs", MODE_PRIVATE).getFloat("Saving", 20f)));

        // --- Recommend Logic (unchanged)
        btnRecommend.setOnClickListener(v -> {
            int monthCount = dbHelper.getDistinctMonthCount();
            if (monthCount < 3) {
                Toast.makeText(this, "ต้องมีข้อมูลธุรกรรมอย่างน้อย 3 เดือนเพื่อคำนวณคำแนะนำ", Toast.LENGTH_LONG).show();
                return;
            }

            double predictedIncome = dbHelper.predictNextMonthIncome();
            double currentIncome = dbHelper.getTotalIncomeThisMonth();
            double chosenIncome = spIncomeSource.getSelectedItemPosition() == 0
                    ? predictedIncome : currentIncome;

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

            int necInt = (int) Math.ceil(necPercent);
            int luxInt = (int) Math.ceil(luxPercent);
            int savInt = 100 - necInt - luxInt;

            etNec.setText(String.valueOf(necInt));
            etLux.setText(String.valueOf(luxInt));
            etSav.setText(String.valueOf(savInt));

            String msg = String.format(
                    "รายรับที่ใช้คำนวณ: ฿%,.2f\nค่าใช้จ่ายจำเป็น: ฿%,.2f\nค่าใช้จ่ายฟุ่มเฟือย: ฿%,.2f\nออม/ลงทุน: ฿%,.2f",
                    chosenIncome, necCost, luxCost, savCost
            );

            new AlertDialog.Builder(this)
                    .setTitle("คำแนะนำจากข้อมูล")
                    .setMessage(msg)
                    .setPositiveButton("ปิด", null)
                    .show();
        });

        builder.setView(dialogView);

        // IMPORTANT: No auto-dismiss
        builder.setPositiveButton("บันทึก", null);
        builder.setNegativeButton("ยกเลิก", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // --- Real buttons after .show()
        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Add live error-clearing
        TextWatcher clearError = new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                etNec.setBackgroundColor(Color.TRANSPARENT);
                etLux.setBackgroundColor(Color.TRANSPARENT);
                etSav.setBackgroundColor(Color.TRANSPARENT);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        etNec.addTextChangedListener(clearError);
        etLux.addTextChangedListener(clearError);
        etSav.addTextChangedListener(clearError);

        // --- Override save button
        positive.setOnClickListener(v -> {
            try {
                double nec = Double.parseDouble(etNec.getText().toString());
                double lux = Double.parseDouble(etLux.getText().toString());
                double sav = Double.parseDouble(etSav.getText().toString());
                double total = nec + lux + sav;

                if (nec < 0 || lux < 0 || sav < 0) {
                    markError(etNec, etLux, etSav);
                    Toast.makeText(this, "เปอร์เซ็นต์ต้องไม่ติดลบ", Toast.LENGTH_LONG).show();
                    return;
                }

                if (total != 100.0) {
                    markError(etNec, etLux, etSav);
                    Toast.makeText(this, "ผลรวมต้องเท่ากับ 100%", Toast.LENGTH_LONG).show();
                    return;
                }

                // Save only when valid
                getSharedPreferences("GoalPrefs", MODE_PRIVATE)
                        .edit()
                        .putFloat("Necessity", (float) nec)
                        .putFloat("Luxury", (float) lux)
                        .putFloat("Saving", (float) sav)
                        .putString("IncomeMode",
                                spIncomeSource.getSelectedItemPosition() == 0
                                        ? "predicted" : "current")
                        .apply();

                Toast.makeText(this, "บันทึกเรียบร้อย", Toast.LENGTH_SHORT).show();
                loadDefaultGoalChart();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                markError(etNec, etLux, etSav);
                Toast.makeText(this, "กรุณากรอกตัวเลขที่ถูกต้อง", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTableRow(TableLayout table, String col1, String col2, String col3, String col4, boolean flip) {
        TableRow row = new TableRow(this);
        row.setPadding(0, 8, 0, 8);

        row.addView(makeCell(col1));
        row.addView(makeCell(col2));
        row.addView(makeCell(col3));
        row.addView(makeColoredCell(col4, flip));

        table.addView(row);
    }

    private TextView makeCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(8, 4, 8, 4);
        tv.setTextColor(Color.BLACK);
        return tv;
    }

    private TextView makeColoredCell(String text, boolean flipColor) {
        TextView tv = makeCell(text);

        boolean isOver = text.contains("เกิน"); // true = over, false = leftover

        if (flipColor) isOver = !isOver;

        if (isOver) {
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(Color.parseColor("#388E3C"));
        }

        return tv;
    }

    private String formatMoney(double value) {
        return String.format("฿%,.2f", value);
    }

    private String formatDiff(double target, double actual, boolean flip) {
        double diff = Math.abs(target - actual);
        boolean over = actual > target;

        return over ?
                String.format("เกิน ฿%,.2f", diff) :
                String.format("เหลือ ฿%,.2f", diff);
    }

    private String formatDiffPercent(double targetPercent, double actualPercent, boolean flip) {
        double diff = Math.abs(targetPercent - actualPercent) * 100; // convert from fraction to %
        boolean over = actualPercent > targetPercent;

        return over ?
                String.format("เกิน %.1f%%", diff) :
                String.format("เหลือ %.1f%%", diff);
    }

    private double calculateActualUsable(UsableBudget Usable, double value, String type){

        return 0;
    }

    private UsableBudget calculateTotalUsable(double targetNec, double necessityExp,
                                              double targetLux, double luxuryExp,
                                              double targetSav, double savingExp) {

        // Calculate the raw remaining budget for each category
        // This value can be negative if the user overspent the target
        double usableNec = (targetNec - necessityExp);
        double usableLux = (targetLux - luxuryExp);
        double usableSav = (targetSav - savingExp);

        // Create and return the UsableBudget object
        return new UsableBudget(usableNec, usableLux, usableSav);
    }

    private UsableBudget calculateCascadingUsable(UsableBudget rawUsable) {

        // Start with the raw differences (Target - Actual). These can be negative.
        double nec = rawUsable.getUsableNecessity();
        double lux = rawUsable.getUsableLuxury();
        double sav = rawUsable.getUsableSaving();

        // The categories that were overspent (negative raw difference)
        double deficit = 0;
        if (lux < 0) deficit += lux;
        if (sav < 0) deficit += sav;
        if (nec < 0) deficit += nec;

        // Convert deficit to a positive amount representing the total shortage
        deficit = Math.abs(deficit);

        // If there is no deficit, return the original usable amounts (but ensure negatives are zeroed out for reporting)
        if (deficit == 0) {
            return new UsableBudget(
                    Math.max(0, nec),
                    Math.max(0, lux),
                    Math.max(0, sav)
            );
        }

        // --- 1. Absorb Deficit from LUXURY (Highest Absorption Priority) ---

        // Get the amount of leftover budget available in Luxury
        double luxUsable = Math.max(0, lux);

        // Amount to cut from Luxury is limited by its usable amount or the total deficit
        double cutFromLux = Math.min(luxUsable, deficit);

        // Reduce Luxury's leftover amount
        lux -= cutFromLux;
        // Reduce the deficit
        deficit -= cutFromLux;

        // --- 2. Absorb Deficit from SAVING ---
        if (deficit > 0) {
            // Get the amount of leftover budget available in Saving
            double savUsable = Math.max(0, sav);

            // Amount to cut from Saving is limited by its usable amount or the remaining deficit
            double cutFromSav = Math.min(savUsable, deficit);

            // Reduce Saving's leftover amount
            sav -= cutFromSav;
            // Reduce the deficit
            deficit -= cutFromSav;
        }

        // --- 3. Absorb Deficit from NECESSITY (Lowest Absorption Priority) ---
        if (deficit > 0) {
            // Get the amount of leftover budget available in Necessity
            double necUsable = Math.max(0, nec);

            // Amount to cut from Necessity is limited by its usable amount or the remaining deficit
            double cutFromNec = Math.min(necUsable, deficit);

            // Reduce Necessity's leftover amount
            nec -= cutFromNec;
            // Reduce the deficit (at this point, deficit should be 0 unless the total overall budget was busted)
            deficit -= cutFromNec;
        }

        // --- 4. Final Adjustment and Return ---

        // Any category that was overspent initially must be reported as 0 leftover usable,
        // even if the deficit was absorbed by another category.
        // The 'formatAdjustedDiff' method (which checks if actual > target) will handle reporting "เกิน".
        return new UsableBudget(
                Math.max(0, nec),
                Math.max(0, lux),
                Math.max(0, sav)
        );
    }

    private String formatAdjustedDiff(double target, double actual, double adjustedUsable, boolean flip) {
        if (actual > target) {
            // If the actual expense exceeded the target, it is ALWAYS an 'เกิน' (over) based on raw comparison.
            double diff = Math.abs(target - actual);
            return String.format("เกิน ฿%,.2f", diff);
        } else {
            // If actual is <= target, we report the ADJUSTED usable amount.
            // This adjusted amount will be 0 if the budget was cut due to overspending elsewhere.
            return String.format("เหลือ ฿%,.2f", adjustedUsable);
        }
    }

    private void markError(EditText... fields) {
        for (EditText f : fields) {
            f.setBackgroundColor(Color.argb(80, 255, 0, 0)); // light red highlight
        }
    }

}