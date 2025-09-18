package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "numora.db";
    private static final int DATABASE_VERSION = 1;

    // Transactions Table
    public static final String TABLE_TRANSACTIONS = "Transactions";
    public static final String COL_TRANSACTION_ID = "transaction_id";
    public static final String COL_TRANS_CATEGORY_ID = "category_id"; // FK
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

    // Category Table
    public static final String TABLE_CATEGORY = "Category";
    public static final String COL_CATEGORY_ID = "category_id";
    public static final String COL_C_NAME = "C_name";
    public static final String COL_C_TYPE = "C_type";
    public static final String COL_C_DESCRIPTION = "C_description";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Category Table
        String createCategoryTable = "CREATE TABLE " + TABLE_CATEGORY + " (" +
                COL_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_C_NAME + " TEXT NOT NULL, " +
                COL_C_TYPE + " TEXT, " +
                COL_C_DESCRIPTION + " TEXT);";

        // Create Transactions Table
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRANS_CATEGORY_ID + " INTEGER, " +
                COL_TYPE + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_AMOUNT + " REAL, " +
                COL_NOTE + " TEXT, " +
                "FOREIGN KEY(" + COL_TRANS_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + COL_CATEGORY_ID + "));";

        // Create Summaries Table
        String createSummariesTable = "CREATE TABLE " + TABLE_SUMMARIES + " (" +
                COL_SUMMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TOTAL_INCOME + " REAL, " +
                COL_TOTAL_EXPENSE + " REAL, " +
                COL_BALANCE + " REAL);";

        db.execSQL(createCategoryTable);
        db.execSQL(createTransactionsTable);
        db.execSQL(createSummariesTable);

        insertDefaultCategories(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        // ค่าใช้จ่าย (Expenses)
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('อาหาร', 'ค่าใช้จ่าย', 'ค่าอาหารและเครื่องดื่ม')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('เดินทาง', 'ค่าใช้จ่าย', 'ค่าโดยสารและการเดินทาง')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('ที่พัก', 'ค่าใช้จ่าย', 'ค่าเช่าบ้าน/หอพัก/ค่าน้ำไฟ')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('สุขภาพ', 'ค่าใช้จ่าย', 'ค่ารักษาพยาบาล/ยา/ประกัน')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('การศึกษา', 'ค่าใช้จ่าย', 'ค่าเล่าเรียน/หนังสือ/คอร์สเรียน')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('บันเทิง', 'ค่าใช้จ่าย', 'หนัง/เพลง/เกม/กิจกรรมยามว่าง')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('ของใช้ส่วนตัว', 'ค่าใช้จ่าย', 'เสื้อผ้า/ของใช้ประจำวัน')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('หนี้สิน', 'ค่าใช้จ่าย', 'การผ่อนชำระ/หนี้บัตรเครดิต')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('อื่น ๆ', 'ค่าใช้จ่าย', 'ค่าใช้จ่ายเบ็ดเตล็ดทั่วไป')");

        // รายรับ (Income)
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('เงินเดือน', 'รายรับ', 'รายได้จากการทำงาน')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('โบนัส', 'รายรับ', 'โบนัสและค่าตอบแทนพิเศษ')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('การลงทุน', 'รายรับ', 'รายได้จากหุ้น/กองทุน/ดอกเบี้ย')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('ธุรกิจส่วนตัว', 'รายรับ', 'รายได้จากกิจการ/การขายสินค้า')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('ของขวัญ/เงินช่วยเหลือ', 'รายรับ', 'เงินจากครอบครัว/เพื่อน/โอกาสพิเศษ')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", " + COL_C_DESCRIPTION + ") VALUES ('อื่น ๆ', 'รายรับ', 'รายได้เบ็ดเตล็ดทั่วไป')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUMMARIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        onCreate(db);
    }

    // Insert transaction
    public long insertTransaction(int categoryId, String type, String date, double amount, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_CATEGORY_ID, categoryId);
        values.put(COL_TYPE, type);
        values.put(COL_DATE, date);
        values.put(COL_AMOUNT, amount);
        values.put(COL_NOTE, note);

        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return result;
    }

    // Get categoryId from category name
    public int getCategoryIdByName(String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_CATEGORY,
                new String[]{COL_CATEGORY_ID},
                COL_C_NAME + " = ?",
                new String[]{categoryName},
                null, null, null
        );

        int categoryId = -1;
        if (cursor.moveToFirst()) {
            categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CATEGORY_ID));
        }
        cursor.close();
        db.close();
        return categoryId;
    }
}