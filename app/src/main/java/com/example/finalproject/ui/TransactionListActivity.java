package com.example.finalproject.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.models.TransactionModel;

import java.util.ArrayList;
import java.util.List;

public class TransactionListActivity extends AppCompatActivity {

    private RecyclerView recyclerTransactions;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        recyclerTransactions = findViewById(R.id.recyclerTransactions);
        recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        List<TransactionModel> transactions = dbHelper.getAllTransactions();

        TransactionAdapter adapter = new TransactionAdapter(transactions);
        recyclerTransactions.setAdapter(adapter);

        CheckBox cbExpense = findViewById(R.id.cbExpense);
        CheckBox cbIncome = findViewById(R.id.cbIncome);
        CheckBox cbTransferOther = findViewById(R.id.cbTransferOther);

        View.OnClickListener filterListener = v -> {
            List<TransactionModel> filtered = new ArrayList<>();
            for (TransactionModel t : dbHelper.getAllTransactions()) {
                String type = t.getType();
                if ((cbExpense.isChecked() && (type.equals("ค่าใช้จ่าย") || type.equals("ออมเงิน/ลงทุน"))) ||
                        (cbIncome.isChecked() && type.equals("รายรับ")) ||
                        (cbTransferOther.isChecked() && (type.equals("ย้ายเงิน") || type.equals("อื่น ๆ")))) {
                    filtered.add(t);
                }
            }
            adapter.updateList(filtered);
        };

        cbExpense.setOnClickListener(filterListener);
        cbIncome.setOnClickListener(filterListener);
        cbTransferOther.setOnClickListener(filterListener);

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