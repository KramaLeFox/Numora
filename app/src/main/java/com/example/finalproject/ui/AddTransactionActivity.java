package com.example.finalproject.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.example.finalproject.modules.DailyWorker;
import com.example.finalproject.R;
import com.example.finalproject.database.DatabaseHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class AddTransactionActivity extends AppCompatActivity {

    private Spinner spinnerType, spinnerCategory;
    private EditText editAmount, editNote;
    private TextView textDate;
    private Button btnSubmit;

    private Calendar selectedDate;
    private DatabaseHelper dbHelper;

    private CheckBox checkRecurring;
    private Button btnRecurringEdit;
    private String recurringFrequency;
    private String recurringNextDate;
    private Integer recurringCustomDays;
    private TextView textRecurringSummary;



    // --- Tags UI
    private ChipGroup chipGroupTags;
    private Button btnAddTag;
    private List<String> selectedTags = new ArrayList<>();
    private List<String> allTagsCache = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = new DatabaseHelper(this);

        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        editAmount = findViewById(R.id.editAmount);
        editNote = findViewById(R.id.editNote);
        textDate = findViewById(R.id.textDate);
        btnSubmit = findViewById(R.id.btnSubmit);
        textRecurringSummary = findViewById(R.id.textRecurringSummary);

        chipGroupTags = findViewById(R.id.chipGroupTags);
        btnAddTag = findViewById(R.id.btnAddTag);
        btnAddTag = findViewById(R.id.btnAddTag);

        // --- TAG BUTTON ---
        btnAddTag.setOnClickListener(v -> openTagDialog());

        // 1) Populate Type spinner (static)
        List<String> types = Arrays.asList("รายรับ", "ค่าใช้จ่าย","ออมเงิน/ลงทุน","ย้ายเงิน");
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // 2) Ensure default categories exist and populate Category spinner
        dbHelper.insertDefaultCategoriesIfEmpty();
        List<String> categories = dbHelper.getAllCategoryNames();
        if (categories == null) categories = new ArrayList<>();
        if (categories.isEmpty()) categories.add("อื่น ๆ");
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // --- Update category spinner based on selected type ---
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = spinnerType.getSelectedItem().toString();

                // Fetch categories that match this type
                List<String> categories = dbHelper.getCategoriesByType(selectedType);

                if (categories == null) categories = new ArrayList<>();
                if (categories.isEmpty()) categories.add("อื่น ๆ"); // fallback

                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(AddTransactionActivity.this,
                        android.R.layout.simple_spinner_item, categories);
                catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(catAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Date handling
        selectedDate = Calendar.getInstance();
        updateDateLabel();

        textDate.setOnClickListener(v -> {
            new DatePickerDialog(AddTransactionActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateLabel();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)).show();
        });

        checkRecurring = findViewById(R.id.checkRecurring);
        btnRecurringEdit = findViewById(R.id.btnRecurringEdit);

        checkRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnRecurringEdit.setEnabled(isChecked);
            if (!isChecked) {
                recurringFrequency = null;
                recurringNextDate = null;
                recurringCustomDays = null;
            }
        });

        btnRecurringEdit.setOnClickListener(v -> openRecurringDialog());

        btnSubmit.setOnClickListener(v -> {
            saveTransaction();

            scheduleDailyWork();

        });
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        textDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveTransaction() {
        if (spinnerType.getSelectedItem() == null) {
            Toast.makeText(this, "กรุณาเลือกประเภท", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "กรุณาเลือกหมวดหมู่", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = spinnerType.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String amountText = editAmount.getText().toString().trim();
        String note = editNote.getText().toString();
        String date = textDate.getText().toString();

        if (amountText.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกจำนวนเงิน", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "รูปแบบจำนวนเงินไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = dbHelper.getCategoryIdByName(category);
        if (categoryId == -1) {
            Toast.makeText(this, "ไม่พบหมวดหมู่", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = getCurrentTime();
        long result = dbHelper.insertTransaction(categoryId, type, date, time, amount, note);

        if (result != -1) {
            if (checkRecurring.isChecked()) {
                dbHelper.insertRecurringItem(
                        type,
                        amount,
                        recurringFrequency,
                        date,
                        recurringCustomDays,
                        recurringNextDate,
                        note,
                        categoryId,
                        selectedTags  // save selected tags
                );
            }

            // Save selected tags
            for (String tag : selectedTags) {
                long tagId = dbHelper.insertOrGetTag(tag);
                dbHelper.linkTransactionWithTag(result, tagId);
            }

            Toast.makeText(this, "บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
        }
    }

    // TAG DIALOG
    private void openTagDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tags, null);
        ChipGroup chipGroupAllTags = dialogView.findViewById(R.id.chipGroupAllTags);
        EditText editNewTag = dialogView.findViewById(R.id.editNewTag);
        Button btnAddNewTag = dialogView.findViewById(R.id.btnAddNewTag);
        Button btnSaveTags = dialogView.findViewById(R.id.btnSaveTags);

        // Merge DB + cache
        List<String> dbTags = dbHelper.getAllTags();
        if (dbTags == null) dbTags = new ArrayList<>();
        Set<String> combined = new HashSet<>(dbTags);
        if (allTagsCache == null) {
            allTagsCache = new ArrayList<>();
        }
        combined.addAll(allTagsCache);
        List<String> allTags = new ArrayList<>(combined);

        // Populate chips
        chipGroupAllTags.removeAllViews();
        for (String tag : allTags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setChecked(selectedTags.contains(tag));
            chipGroupAllTags.addView(chip);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnAddNewTag.setOnClickListener(v -> {
            String newTag = editNewTag.getText().toString().trim();
            if (!newTag.isEmpty() && !allTags.contains(newTag)) {
                allTagsCache.add(newTag); // keep it until saved
                allTags.add(newTag);

                Chip chip = new Chip(this);
                chip.setText(newTag);
                chip.setCheckable(true);
                chip.setChecked(true);
                chipGroupAllTags.addView(chip);
                editNewTag.setText("");
            }
        });

        btnSaveTags.setOnClickListener(v -> {
            selectedTags.clear();
            for (int i = 0; i < chipGroupAllTags.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupAllTags.getChildAt(i);
                if (chip.isChecked()) {
                    selectedTags.add(chip.getText().toString());
                }
            }

            // When saving, persist new tags into DB
            for (String tag : allTagsCache) {
                dbHelper.insertOrGetTag(tag);
            }
            allTagsCache.clear();

            refreshTagChips();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void refreshTagChips() {
        chipGroupTags.removeAllViews();
        for (String tag : selectedTags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setChipBackgroundColorResource(R.color.ButtonBG); // pick your color
            chip.setTextColor(getResources().getColor(android.R.color.white));
            chip.setCloseIconVisible(true); // allow user to remove tag
            chip.setOnCloseIconClickListener(v -> {
                selectedTags.remove(tag);
                refreshTagChips();
            });
            chipGroupTags.addView(chip);
        }
    }

    private void openRecurringDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_recurring, null);
        Spinner spinnerFreq = dialogView.findViewById(R.id.spinnerFrequency);
        EditText editCustomDays = dialogView.findViewById(R.id.editCustomDays);
        TextView textCustomDays = dialogView.findViewById(R.id.textCustomDays);
        TextView editDueDate = dialogView.findViewById(R.id.editDueDate);
        Button btnSave = dialogView.findViewById(R.id.btnSaveRecurring);

        // Setup spinner
        List<String> freqs = Arrays.asList("รายวัน", "รายสัปดาห์", "รายเดือน", "กำหนดเอง");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, freqs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFreq.setAdapter(adapter);

        Calendar dueDate = Calendar.getInstance();

        editDueDate.setOnClickListener(v -> {
            new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        dueDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        editDueDate.setText(sdf.format(dueDate.getTime()));
                    },
                    dueDate.get(Calendar.YEAR),
                    dueDate.get(Calendar.MONTH),
                    dueDate.get(Calendar.DAY_OF_MONTH)).show();
        });

        spinnerFreq.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                String selected = freqs.get(pos);
                if ("กำหนดเอง".equals(selected)) {
                    textCustomDays.setVisibility(View.VISIBLE);
                    editCustomDays.setVisibility(View.VISIBLE);
                } else {
                    textCustomDays.setVisibility(View.GONE);
                    editCustomDays.setVisibility(View.GONE);
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Recurring Settings")
                .setView(dialogView)
                .create();

        btnSave.setOnClickListener(v -> {
            recurringFrequency = spinnerFreq.getSelectedItem().toString();
            recurringNextDate = editDueDate.getText().toString();

            if ("กำหนดเอง".equals(recurringFrequency)) {
                String d = editCustomDays.getText().toString().trim();
                recurringCustomDays = d.isEmpty() ? null : Integer.parseInt(d);
            }

            // Build summary text
            StringBuilder summary = new StringBuilder("รูปแบบ: " + recurringFrequency);
            if (recurringCustomDays != null) {
                summary.append(" (ทุก ").append(recurringCustomDays).append(" วัน)");
            }
            if (recurringNextDate != null && !recurringNextDate.isEmpty()) {
                summary.append("\nวันครบกำหนดถัดไป: ").append(recurringNextDate);
            }

            textRecurringSummary.setText(summary.toString());

            dialog.dismiss();
        });

        dialog.show();
    }

    private void scheduleDailyWork() {
        PeriodicWorkRequest dailyWork =
                new PeriodicWorkRequest.Builder(DailyWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork(
                        "daily_work",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        dailyWork
                );
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

}