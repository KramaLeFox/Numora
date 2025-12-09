package com.example.finalproject.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.models.TransactionModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionListActivity extends AppCompatActivity {

    private RecyclerView recyclerTransactions;
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;

    private Spinner spinnerType, spinnerCategory, spinnerDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        recyclerTransactions = findViewById(R.id.recyclerTransactions);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        List<TransactionModel> transactions = dbHelper.getAllTransactions();

        adapter = new TransactionAdapter(transactions);
        recyclerTransactions.setAdapter(adapter);

        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDate = findViewById(R.id.spinnerDate);

        setupSpinners();
    }

    private void setupSpinners() {
        // ===== Type Spinner =====
        String[] types = {"ทั้งหมด", "ค่าใช้จ่าย", "รายรับ", "ย้ายเงิน", "อื่น ๆ"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // ===== Category Spinner =====
        loadCategorySpinner("ทั้งหมด"); // initially load all categories

        // ===== Date Spinner =====
        String[] dates = {"ทั้งหมด", "วันนี้", "สัปดาห์นี้", "เดือนนี้", "3 เดือนล่าสุด"};
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dates);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDate.setAdapter(dateAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = spinnerType.getSelectedItem().toString();
                loadCategorySpinner(selectedType); // dynamically update categories
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadCategorySpinner(String type) {
        List<String> categories;
        if (type.equals("ทั้งหมด")) {
            categories = dbHelper.getAllCategoryNames();
        } else if (type.equals("ย้ายเงิน/อื่น ๆ")) {
            categories = new ArrayList<>();
            categories.addAll(dbHelper.getCategoriesByType("ย้ายเงิน"));
            categories.addAll(dbHelper.getCategoriesByType("อื่น ๆ"));
        } else {
            categories = dbHelper.getCategoriesByType(type);
        }
        categories.add(0, "ทั้งหมด"); // add “All” option at top

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void applyFilters() {
        String selectedType = spinnerType.getSelectedItem().toString();
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        String selectedDate = spinnerDate.getSelectedItem().toString();

        List<TransactionModel> filtered = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (TransactionModel t : dbHelper.getAllTransactions()) {
            // ===== Type filter =====
            boolean typeMatch = selectedType.equals("ทั้งหมด") ||
                    (selectedType.equals("ย้ายเงิน/อื่น ๆ") && (t.getType().equals("ย้ายเงิน") || t.getType().equals("อื่น ๆ"))) ||
                    t.getType().equals(selectedType);

            // ===== Category filter =====
            boolean categoryMatch = selectedCategory.equals("ทั้งหมด") || t.getCategory().equals(selectedCategory);

            // ===== Date filter =====
            boolean dateMatch = true;
            try {
                Calendar transactionDate = Calendar.getInstance();
                transactionDate.setTime(sdf.parse(t.getDate()));

                switch (selectedDate) {
                    case "วันนี้":
                        dateMatch = transactionDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                                transactionDate.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR);
                        break;
                    case "สัปดาห์นี้":
                        dateMatch = transactionDate.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR) &&
                                transactionDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR);
                        break;
                    case "เดือนนี้":
                        dateMatch = transactionDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                                transactionDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR);
                        break;
                    case "3 เดือนล่าสุด":
                        Calendar threeMonthsAgo = (Calendar) cal.clone();
                        threeMonthsAgo.add(Calendar.MONTH, -3);
                        dateMatch = transactionDate.after(threeMonthsAgo) && transactionDate.before(cal);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (typeMatch && categoryMatch && dateMatch) {
                filtered.add(t);
            }
        }

        adapter.updateList(filtered);
    }

    // ==================== Adapter ====================
    public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

        private final List<TransactionModel> transactionList;

        public TransactionAdapter(List<TransactionModel> transactionList) {
            this.transactionList = transactionList;
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            TransactionModel t = transactionList.get(position);
            holder.textCategory.setText(t.getCategory());
            holder.textDate.setText(t.getDate());
            holder.textAmount.setText("฿" + t.getAmount());

            holder.btnShowNote.setOnClickListener(v -> showNoteDialog(t.getNote()));
        }

        @Override
        public int getItemCount() {
            return transactionList.size();
        }

        class TransactionViewHolder extends RecyclerView.ViewHolder {
            TextView textCategory, textDate, textAmount;
            Button btnShowNote;

            public TransactionViewHolder(@NonNull View itemView) {
                super(itemView);
                textCategory = itemView.findViewById(R.id.textCategory);
                textDate = itemView.findViewById(R.id.textDate);
                textAmount = itemView.findViewById(R.id.textAmount);
                btnShowNote = itemView.findViewById(R.id.btnShowNote);
            }
        }

        public void updateList(List<TransactionModel> newList) {
            transactionList.clear();
            transactionList.addAll(newList);
            notifyDataSetChanged();
        }

    }

    // ==================== Show Note Dialog ====================
    private void showNoteDialog(String note) {
        new AlertDialog.Builder(this)
                .setTitle("บันทึก")
                .setMessage(note == null || note.isEmpty() ? "ไม่มีบันทึก" : note)
                .setPositiveButton("ปิด", null)
                .show();
    }

}