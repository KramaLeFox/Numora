package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "numora.db";
    private static final int DATABASE_VERSION = 1;

    // Transactions Table
    private static final String TABLE_TRANSACTIONS = "Transactions";
    private static final String COL_TRANSACTION_ID = "transaction_id";
    private static final String COL_TRANS_CATEGORY_ID = "category_id";
    private static final String COL_TYPE = "T_type";
    private static final String COL_DATE = "T_date";
    private static final String COL_AMOUNT = "T_amount";
    private static final String COL_NOTE = "T_note";

    // Category Table
    private static final String TABLE_CATEGORY = "Category";
    private static final String COL_CATEGORY_ID = "category_id";
    private static final String COL_C_NAME = "C_name";
    private static final String COL_C_TYPE = "C_type";
    private static final String COL_C_EXT_TYPE = "C_ext_type";
    private static final String COL_C_DESCRIPTION = "C_description";

    // Recurring Items Table
    private static final String TABLE_RECURRING = "Recurring_Item";
    private static final String COL_RECURRING_ID = "recurring_id";
    private static final String COL_R_TYPE = "R_type";
    private static final String COL_R_AMOUNT = "R_amount";
    private static final String COL_FREQUENCY = "frequency"; // daily, weekly, monthly, custom
    private static final String COL_R_DUE_DATE = "R_due_date"; // YYYY-MM-DD (for monthly etc.)
    private static final String COL_R_DUE_AMOUNT = "R_due_amount"; // delay in days for custom
    private static final String COL_NEXT_DUE_DATE = "next_due_date"; // YYYY-MM-DD
    private static final String COL_NOTE_R = "R_note"; // optional
    private static final String COL_R_CATEGORY_ID = "category_id"; // link to category
    private static final String COL_R_TAGS = "tags"; // comma-separated tags

    // Tags Table
    private static final String TABLE_TAGS = "Tags";
    private static final String COL_TAG_ID = "tag_id";
    private static final String COL_TAG_NAME = "tag_name";

    // TransactionTags (Link Table)
    private static final String TABLE_TRANSACTION_TAGS = "TransactionTags";
    private static final String COL_TT_ID = "tt_id";
    private static final String COL_TT_TRANSACTION_ID = "transaction_id";
    private static final String COL_TT_TAG_ID = "tag_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Category Table
        String createCategoryTable = "CREATE TABLE " + TABLE_CATEGORY + " (" +
                COL_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_C_NAME + " TEXT NOT NULL, " +
                COL_C_TYPE + " TEXT, " +
                COL_C_EXT_TYPE + " TEXT, " +
                COL_C_DESCRIPTION + " TEXT" +
                ");";

        // Create Transactions Table
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COL_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRANS_CATEGORY_ID + " INTEGER, " +
                COL_TYPE + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_AMOUNT + " REAL, " +
                COL_NOTE + " TEXT, " +
                "FOREIGN KEY(" + COL_TRANS_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + COL_CATEGORY_ID + "));";

        String createRecurringTable = "CREATE TABLE " + TABLE_RECURRING + " (" +
                COL_RECURRING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_R_TYPE + " TEXT NOT NULL, " +
                COL_R_AMOUNT + " REAL NOT NULL, " +
                COL_FREQUENCY + " TEXT NOT NULL, " +
                COL_R_DUE_DATE + " TEXT, " +
                COL_R_DUE_AMOUNT + " INTEGER, " +
                COL_NEXT_DUE_DATE + " TEXT, " +
                COL_R_CATEGORY_ID + " INTEGER, " +
                COL_R_TAGS + " TEXT, " +
                COL_NOTE_R + " TEXT, " +
                "FOREIGN KEY(" + COL_R_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + COL_CATEGORY_ID + ")" +
                ");";

        String createTagsTable = "CREATE TABLE " + TABLE_TAGS + " (" +
                COL_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TAG_NAME + " TEXT UNIQUE NOT NULL);";

        String createTransactionTagsTable = "CREATE TABLE " + TABLE_TRANSACTION_TAGS + " (" +
                COL_TT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TT_TRANSACTION_ID + " INTEGER NOT NULL, " +
                COL_TT_TAG_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_TT_TRANSACTION_ID + ") REFERENCES " + TABLE_TRANSACTIONS + "(" + COL_TRANSACTION_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_TT_TAG_ID + ") REFERENCES " + TABLE_TAGS + "(" + COL_TAG_ID + ") ON DELETE CASCADE);";

        db.execSQL(createCategoryTable);
        db.execSQL(createTransactionsTable);
        db.execSQL(createRecurringTable);
        db.execSQL(createTagsTable);
        db.execSQL(createTransactionTagsTable);

        insertDefaultCategories(db);
    }

    private void insertDefaultCategories(SQLiteDatabase db) {
        // ค่าใช้จ่าย (Expenses)
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('อาหาร', 'ค่าใช้จ่าย', 'Necessity', 'ค่าอาหารและเครื่องดื่ม')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('เดินทาง', 'ค่าใช้จ่าย', 'Necessity', 'ค่าโดยสารและการเดินทาง')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ท่องเที่ยว', 'ค่าใช้จ่าย', 'Luxury', 'ค่าใช้จ่ายในการท่องเที่ยว')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ที่พัก', 'ค่าใช้จ่าย', 'Necessity', 'ค่าเช่าบ้าน/หอพัก/ค่าน้ำไฟ')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('สุขภาพ', 'ค่าใช้จ่าย', 'Necessity', 'ค่ารักษาพยาบาล/ยา/ประกัน')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('การศึกษา', 'ค่าใช้จ่าย', 'Necessity', 'ค่าเล่าเรียน/หนังสือ/คอร์สเรียน')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('บันเทิง', 'ค่าใช้จ่าย', 'Luxury', 'หนัง/เพลง/เกม/กิจกรรมยามว่าง')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ของใช้ส่วนตัว', 'ค่าใช้จ่าย', 'Luxury', 'เสื้อผ้า/ของใช้ประจำวัน')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('หนี้สิน', 'ค่าใช้จ่าย', 'Necessity', 'การผ่อนชำระ/หนี้บัตรเครดิต')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ช็อปปิ้ง', 'ค่าใช้จ่าย', 'Luxury', 'ค่าใช้จ่ายสำหรับการช็อปปิ้ง')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('อื่น ๆ(จำเป็น)', 'ค่าใช้จ่าย', 'Necessity', 'ค่าใช้จ่ายเบ็ดเตล็ดทั่วไปที่มีความจำเป็น')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('อื่น ๆ(ฟุ่มเฟือย)', 'ค่าใช้จ่าย', 'Luxury', 'ค่าใช้จ่ายเบ็ดเตล็ดทั่วไป')");

        // รายรับ (Income)
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('เงินเดือน', 'รายรับ', 'Income', 'รายได้จากการทำงาน')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('โบนัส', 'รายรับ', 'Income', 'โบนัสและค่าตอบแทนพิเศษ')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('รายได้จากการลงทุน', 'รายรับ', 'Income', 'รายได้จากหุ้น/กองทุน/ดอกเบี้ย')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ธุรกิจส่วนตัว', 'รายรับ', 'Income', 'รายได้จากกิจการ/การขายสินค้า')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ขายของ', 'รายรับ', 'Income', 'รายได้จากการขายสินค้า/บริการ')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ของขวัญ/เงินช่วยเหลือ', 'รายรับ', 'Income', 'เงินจากครอบครัว/เพื่อน/โอกาสพิเศษ')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('รายรับอื่น ๆ', 'รายรับ', 'Income', 'รายได้เบ็ดเตล็ดทั่วไป')");

        // เงินออม/ลงทุน (Saving & Investment)
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ฝากเงิน', 'ออมเงิน/ลงทุน', 'Savings', 'เงินฝากเข้าบัญชีเพื่อการออม')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ลงทุน', 'ออมเงิน/ลงทุน', 'Investment', 'เงินที่ใช้ลงทุนในหุ้น/กองทุน')");

        // ย้ายเงิน (Moving / Transfer)
        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('ย้ายเงิน', 'ย้ายเงิน', 'Transfer', 'เงินที่ย้ายสถานที่เก็บ')");

        db.execSQL("INSERT INTO " + TABLE_CATEGORY + " (" + COL_C_NAME + ", " + COL_C_TYPE + ", C_ext_type, " + COL_C_DESCRIPTION + ") VALUES ('อื่น ๆ', 'อื่น ๆ', 'Etc', 'รายการไม่ระบุประเภท')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_TAGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
        onCreate(db);
    }

    // Insert transaction
    public long insertTransaction(int categoryId, String type, String date, double amount, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String finalType = type;

        if (type.equals("ออมเงิน")) {
            finalType = "ค่าใช้จ่าย";
        }

        values.put(COL_TRANS_CATEGORY_ID, categoryId);
        values.put(COL_TYPE, finalType);
        values.put(COL_DATE, date);
        values.put(COL_AMOUNT, amount);
        values.put(COL_NOTE, note);

        return db.insert(TABLE_TRANSACTIONS, null, values);
    }

    // Get last N transactions
    public Cursor getLastTransactions(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS +
                " ORDER BY " + COL_TRANSACTION_ID + " DESC LIMIT ?", new String[]{String.valueOf(limit)});
    }

    // Get all transactions
    public Cursor getAllTransactionsRaw() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TRANSACTIONS +
                " ORDER BY " + COL_TRANSACTION_ID + " DESC";
        return db.rawQuery(query, null);
    }

    // Get all transactions as a List<TransactionModel>
    public List<TransactionModel> getAllTransactions() {
        List<TransactionModel> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Join Transactions with Category to get the category name + type
        String query = "SELECT t." + COL_DATE + ", t." + COL_AMOUNT + ", t." + COL_NOTE +
                ", c." + COL_C_NAME + " AS category, c." + COL_C_TYPE + " AS type " +
                "FROM " + TABLE_TRANSACTIONS + " t " +
                "LEFT JOIN " + TABLE_CATEGORY + " c " +
                "ON t." + COL_TRANS_CATEGORY_ID + " = c." + COL_CATEGORY_ID + " " +
                "ORDER BY t." + COL_TRANSACTION_ID + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type")); // new
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE));

                transactions.add(new TransactionModel(category, type, date, amount, note));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return transactions;
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
        return categoryId;
    }

    // Ensure defaults exist
    public void insertDefaultCategoriesIfEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CATEGORY, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            insertDefaultCategories(db);
        }
    }

    // Return category names
    public List<String> getAllCategoryNames() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_CATEGORY,
                new String[]{COL_C_NAME},
                null, null, null, null,
                COL_C_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_C_NAME)));
        }
        cursor.close();
        return list;
    }

    // --- Summary calculations ---
    public double getTotalIncome() {
        return getTotalByType("รายรับ");
    }

    public double getTotalExpense() {
        return getTotalByType("ค่าใช้จ่าย");
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    private double getTotalByType(String type) {
        double total = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COL_TYPE + " = ?", new String[]{type}
        );
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public List<String> getCategoriesByType(String type) {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CATEGORY,
                new String[]{COL_C_NAME},
                COL_C_TYPE + " = ?",
                new String[]{type},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_C_NAME)));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return categories;
    }

    // Insert Recurring
    public long insertRecurringItem(String type, double amount, String frequency,
                                    String dueDate, Integer dueAmount, String nextDueDate, String note,
                                    int categoryId, List<String> tags) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_R_TYPE, type);
        values.put(COL_R_AMOUNT, amount);
        values.put(COL_FREQUENCY, frequency);
        values.put(COL_R_DUE_DATE, dueDate);
        values.put(COL_R_DUE_AMOUNT, dueAmount);
        values.put(COL_NEXT_DUE_DATE, nextDueDate);
        values.put(COL_R_CATEGORY_ID, categoryId);

        // Store tags as comma-separated string
        if (tags != null && !tags.isEmpty()) {
            values.put(COL_R_TAGS, String.join(",", tags));
        } else {
            values.put(COL_R_TAGS, "");
        }

        values.put(COL_NOTE_R, note);
        return db.insert(TABLE_RECURRING, null, values);
    }

    public void makeRecurringTransaction() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());

        Cursor cursor = db.query(
                TABLE_RECURRING,
                null,
                COL_NEXT_DUE_DATE + " = ?",
                new String[]{today},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Recurring item info
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_R_TYPE));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_R_AMOUNT));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTE_R));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow(COL_FREQUENCY));
                int recurringId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RECURRING_ID));

                // Category
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_R_CATEGORY_ID));
                if (categoryId == -1) categoryId = getCategoryIdByName("อื่น ๆ");

                // Tags stored as CSV in recurring (optional)
                String tagsCsv = cursor.getString(cursor.getColumnIndexOrThrow("tags")); // you need to add this column
                List<String> tags = new ArrayList<>();
                if (tagsCsv != null && !tagsCsv.isEmpty()) {
                    tags = Arrays.asList(tagsCsv.split(",")); // split into individual tags
                }

                // Insert new transaction
                long newTransactionId = insertTransaction(categoryId, type, today, amount, note);

                // Link tags
                for (String tag : tags) {
                    tag = tag.trim();
                    if (!tag.isEmpty()) {
                        long tagId = insertOrGetTag(tag);
                        linkTransactionWithTag(newTransactionId, tagId);
                    }
                }

                // Update next due date
                String nextDue = calculateNextDate(today, frequency);
                ContentValues updateValues = new ContentValues();
                updateValues.put(COL_NEXT_DUE_DATE, nextDue);
                db.update(TABLE_RECURRING, updateValues, COL_RECURRING_ID + " = ?", new String[]{String.valueOf(recurringId)});

            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private String calculateNextDate(String currentDate, String frequency) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        java.util.Calendar cal = java.util.Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(currentDate));
        } catch (Exception e) {
            e.printStackTrace();
            return currentDate;
        }

        switch (frequency.toLowerCase()) {
            case "daily":
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
                break;
            case "weekly":
                cal.add(java.util.Calendar.WEEK_OF_YEAR, 1);
                break;
            case "monthly":
                cal.add(java.util.Calendar.MONTH, 1);
                break;
            default:
                // custom frequency or invalid — do nothing
                break;
        }

        return sdf.format(cal.getTime());
    }

    // Insert or get existing tag id
    public long insertOrGetTag(String tagName) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Try to insert
        ContentValues values = new ContentValues();
        values.put(COL_TAG_NAME, tagName.trim());

        long id = db.insertWithOnConflict(TABLE_TAGS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            // Already exists, fetch it
            Cursor cursor = db.query(TABLE_TAGS, new String[]{COL_TAG_ID},
                    COL_TAG_NAME + " = ?", new String[]{tagName}, null, null, null);
            if (cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TAG_ID));
            }
            cursor.close();
        }
        return id;
    }

    // Link transaction with a tag
    public void linkTransactionWithTag(long transactionId, long tagId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TT_TRANSACTION_ID, transactionId);
        values.put(COL_TT_TAG_ID, tagId);
        db.insert(TABLE_TRANSACTION_TAGS, null, values);
    }

    // Get all tags for a transaction
    public List<String> getTagsForTransaction(long transactionId) {
        List<String> tags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t." + COL_TAG_NAME +
                " FROM " + TABLE_TAGS + " t " +
                " JOIN " + TABLE_TRANSACTION_TAGS + " tt ON t." + COL_TAG_ID + " = tt." + COL_TT_TAG_ID +
                " WHERE tt." + COL_TT_TRANSACTION_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(transactionId)});
        while (cursor.moveToNext()) {
            tags.add(cursor.getString(0));
        }
        cursor.close();
        return tags;
    }

    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TAGS,
                new String[]{COL_TAG_NAME},
                null, null, null, null,
                COL_TAG_NAME + " ASC");
        while (cursor.moveToNext()) {
            tags.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_TAG_NAME)));
        }
        cursor.close();
        return tags;
    }

    // Get total amounts by category for expense + savings/investment types, excluding Transfer
    public Cursor getExpenseAndSavingsAmountsByCategoryExcludingTransfers() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c." + COL_C_NAME + " AS C_name, SUM(t." + COL_AMOUNT + ") AS total " +
                "FROM " + TABLE_TRANSACTIONS + " t " +
                "JOIN " + TABLE_CATEGORY + " c ON t." + COL_TRANS_CATEGORY_ID + " = c." + COL_CATEGORY_ID + " " +
                "WHERE (c." + COL_C_TYPE + " = 'ค่าใช้จ่าย' OR c." + COL_C_TYPE + " = 'ออมเงิน/ลงทุน') " +
                "AND c." + COL_C_EXT_TYPE + " != 'Transfer' " +
                "GROUP BY c." + COL_C_NAME;
        return db.rawQuery(query, null);
    }

}