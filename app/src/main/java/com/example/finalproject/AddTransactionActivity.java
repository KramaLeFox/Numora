package com.example.finalproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private Spinner spinnerType, spinnerCategory;
    private EditText editAmount, editNote;
    private TextView textDate;
    private Button btnSubmit;

    private Calendar selectedDate;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction); // 🔥 replace with your layout file name

        dbHelper = new DatabaseHelper(this);

        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        editAmount = findViewById(R.id.editAmount);
        editNote = findViewById(R.id.editNote);
        textDate = findViewById(R.id.textDate);
        btnSubmit = findViewById(R.id.btnSubmit);

        selectedDate = Calendar.getInstance();
        updateDateLabel();

        // Date picker
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

        btnSubmit.setOnClickListener(v -> saveTransaction());
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        textDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void saveTransaction() {
        String type = spinnerType.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String amountText = editAmount.getText().toString();
        String note = editNote.getText().toString();
        String date = textDate.getText().toString();

        if (amountText.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกจำนวนเงิน", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountText);

        int categoryId = dbHelper.getCategoryIdByName(category);

        if (categoryId == -1) {
            Toast.makeText(this, "ไม่พบหมวดหมู่", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.insertTransaction(categoryId, type, date, amount, note);

        if (result != -1) {
            Toast.makeText(this, "บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
        }
    }
}