package com.example.finalproject.modules;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.finalproject.database.DatabaseHelper;
import com.example.finalproject.models.TargetModel;

import java.util.ArrayList;
import java.util.List;

public class TargetModule {

    private final DatabaseHelper dbHelper;

    public TargetModule(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long addTarget(TargetModel target) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("target_type", target.getType());
        values.put("category_id", target.getCategoryId());
        values.put("T_amount", target.getAmount());
        values.put("T_percentage", target.getPercentage());
        values.put("start_date", target.getStartDate());
        values.put("end_date", target.getEndDate());
        return db.insert("Targets", null, values);
    }

    public List<TargetModel> getAllTargets() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<TargetModel> targets = new ArrayList<>();

        Cursor cursor = db.query("Targets", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                TargetModel target = new TargetModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow("target_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("target_type")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("tag_id")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("T_amount")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("T_percentage")),
                        cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("end_date"))
                );
                targets.add(target);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return targets;
    }

}