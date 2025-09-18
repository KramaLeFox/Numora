package com.example.finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "numora.db";
    private static final int DATABASE_VERSION = 1;

    // Transactions Table
    public static final String TABLE_TRANSACTIONS = "Transactions";
    public static final String COL_TRANSACTION_ID = "transaction_id";
    public static final String COL_CATEGORY_ID = "category_id";
    public static final String COL_TAG_ID = "tag_id";
    public static final String COL_TYPE = "T_type";
    public static final String COL_DATE = "T_date";
    public static final String COL_AMOUNT = "T_amount";
    public static final String COL_NOTE = "T_note";

    // Summaries Table
    public static final String TABLE_SUMMARIES = "Summaries";
    public static final String COL_SUMMARY_ID = "summary_id";
    public static final String COL_TOTAL_INCOME = "total_income";
    public static final String COL_TOTAL_EXPENSE = "total_expense";
    public static final String COL_BALANCE = "balance";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Transactions Table
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CATEGORY_ID + " INTEGER, " +
                COL_TAG_ID + " INTEGER, " +
                COL_TYPE + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_AMOUNT + " REAL, " +
                COL_NOTE + " TEXT);";

        // Create Summaries Table
        String createSummariesTable = "CREATE TABLE " + TABLE_SUMMARIES + " (" +
                COL_SUMMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TOTAL_INCOME + " REAL, " +
                COL_TOTAL_EXPENSE + " REAL, " +
                COL_BALANCE + " REAL);";

        db.execSQL(createTransactionsTable);
        db.execSQL(createSummariesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUMMARIES);
        onCreate(db);
    }
}
