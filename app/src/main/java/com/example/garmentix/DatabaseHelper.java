package com.example.garmentix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GARMENTIX.db";
    private static final int DATABASE_VERSION = 4;

    // Users Table
    private static final String TABLE_USERS = "users";

    private static final String COL_USER_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_MOBILE = "mobile";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    // Custom Designs Table
    private static final String TABLE_DESIGNS = "custom_designs";

    // Measurements Table
    private static final String TABLE_MEASUREMENTS = "measurements";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // =====================================================
    // CREATE DATABASE TABLES
    // =====================================================

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Users Table
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_NAME + " TEXT NOT NULL, " +
                        COL_MOBILE + " TEXT NOT NULL, " +
                        COL_EMAIL + " TEXT NOT NULL UNIQUE, " +
                        COL_PASSWORD + " TEXT NOT NULL)";

        db.execSQL(createUsersTable);

        // Custom Designs Table
        String createDesignsTable =
                "CREATE TABLE " + TABLE_DESIGNS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "user_email TEXT NOT NULL, " +
                        "design_name TEXT NOT NULL, " +
                        "category TEXT, " +
                        "template_name TEXT, " +
                        "fabric TEXT, " +
                        "color TEXT, " +
                        "sleeve TEXT, " +
                        "neckline TEXT, " +
                        "pocket TEXT, " +
                        "cuff TEXT, " +
                        "dress_length TEXT, " +
                        "fit_style TEXT, " +
                        "pattern TEXT, " +
                        "custom_text TEXT, " +
                        "logo_path TEXT, " +
                        "design_data TEXT, " +
                        "preview_path TEXT, " +
                        "is_favorite INTEGER NOT NULL DEFAULT 0, " +
                        "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY(user_id) REFERENCES users(id))";

        db.execSQL(createDesignsTable);

        // Measurements Table
        String createMeasurementsTable =
                "CREATE TABLE " + TABLE_MEASUREMENTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_email TEXT NOT NULL UNIQUE, " +
                        "shoulder TEXT, " +
                        "chest TEXT, " +
                        "waist TEXT, " +
                        "hips TEXT, " +
                        "sleeve_length TEXT, " +
                        "dress_length TEXT, " +
                        "FOREIGN KEY(user_email) REFERENCES users(email))";

        db.execSQL(createMeasurementsTable);
    }

    // =====================================================
    // DATABASE UPGRADE
    // =====================================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {

        // Version 1 to Version 2: Measurements Table
        if (oldVersion < 2) {

            String createMeasurementsTable =
                    "CREATE TABLE IF NOT EXISTS " +
                            TABLE_MEASUREMENTS + " (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "user_email TEXT NOT NULL UNIQUE, " +
                            "shoulder TEXT, " +
                            "chest TEXT, " +
                            "waist TEXT, " +
                            "hips TEXT, " +
                            "sleeve_length TEXT, " +
                            "dress_length TEXT, " +
                            "FOREIGN KEY(user_email) REFERENCES users(email))";

            db.execSQL(createMeasurementsTable);
        }

        if (oldVersion < 4) {
            addColumnIfMissing(db, TABLE_DESIGNS, "user_email", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "template_name", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "sleeve", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "neckline", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "pocket", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "cuff", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "dress_length", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "fit_style", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "pattern", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "custom_text", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "logo_path", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "design_data", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "preview_path", "TEXT");
            addColumnIfMissing(db, TABLE_DESIGNS, "is_favorite", "INTEGER NOT NULL DEFAULT 0");
            db.execSQL("UPDATE custom_designs SET user_email = " +
                    "(SELECT email FROM users WHERE users.id = custom_designs.user_id) " +
                    "WHERE user_email IS NULL");
            migratePlaintextPasswords(db);
            db.execSQL("DROP TABLE IF EXISTS orders");
        }
    }

    private void addColumnIfMissing(SQLiteDatabase db, String table, String column, String type) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        try {
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            while (cursor.moveToNext()) {
                if (column.equals(cursor.getString(nameIndex))) {
                    return;
                }
            }
        } finally {
            cursor.close();
        }
        db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }

    private void migratePlaintextPasswords(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID, COL_PASSWORD},
                null, null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                String stored = cursor.getString(1);
                if (stored != null && !PasswordSecurity.isHashed(stored)) {
                    ContentValues values = new ContentValues();
                    values.put(COL_PASSWORD, PasswordSecurity.hash(stored));
                    db.update(TABLE_USERS, values, COL_USER_ID + " = ?",
                            new String[]{cursor.getString(0)});
                }
            }
        } finally {
            cursor.close();
        }
    }

    // =====================================================
    // REGISTER USER
    // =====================================================

    public boolean registerUser(
            String name,
            String mobile,
            String email,
            String password
    ) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor existing = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_EMAIL + " = ? COLLATE NOCASE",
                new String[]{email.trim()}, null, null, null);
        try {
            if (existing.moveToFirst()) {
                return false;
            }
        } finally {
            existing.close();
        }

        ContentValues values = new ContentValues();

        values.put(COL_NAME, name);
        values.put(COL_MOBILE, mobile);
        values.put(COL_EMAIL, email.trim().toLowerCase(java.util.Locale.ROOT));
        values.put(COL_PASSWORD, PasswordSecurity.hash(password));

        long result = db.insert(TABLE_USERS, null, values);

        return result != -1;
    }

    // =====================================================
    // CHECK LOGIN
    // =====================================================

    public boolean checkLogin(String email, String password) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID, COL_PASSWORD},
                COL_EMAIL + " = ? COLLATE NOCASE",
                new String[]{email},
                null,
                null,
                null
        );

        try {
            if (!cursor.moveToFirst()) {
                return false;
            }

            String stored = cursor.getString(1);
            if (PasswordSecurity.isHashed(stored)) {
                return PasswordSecurity.verify(password, stored);
            }
            if (stored != null && stored.equals(password)) {
                ContentValues values = new ContentValues();
                values.put(COL_PASSWORD, PasswordSecurity.hash(password));
                db.update(TABLE_USERS, values, COL_USER_ID + " = ?",
                        new String[]{cursor.getString(0)});
                return true;
            }
            return false;
        } finally {
            cursor.close();
        }
    }

    public boolean userExists(String email) {
        Cursor cursor = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_EMAIL + " = ? COLLATE NOCASE",
                new String[]{email},
                null,
                null,
                null
        );
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public int getUserCount() {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_USERS,
                null
        );
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    // =====================================================
    // GET USER DETAILS
    // =====================================================

    public Cursor getUserDetails(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_USERS,
                new String[]{
                        COL_USER_ID,
                        COL_NAME,
                        COL_MOBILE,
                        COL_EMAIL
                },
                COL_EMAIL + " = ? COLLATE NOCASE",
                new String[]{email},
                null,
                null,
                null
        );
    }

    public boolean updateUserProfile(String email, String name, String mobile) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_MOBILE, mobile);
        return getWritableDatabase().update(TABLE_USERS, values,
                COL_EMAIL + " = ? COLLATE NOCASE", new String[]{email}) > 0;
    }

    public boolean changePassword(String email, String currentPassword, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID, COL_PASSWORD},
                COL_EMAIL + " = ? COLLATE NOCASE", new String[]{email}, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return false;
            }
            String stored = cursor.getString(1);
            boolean valid = PasswordSecurity.isHashed(stored)
                    ? PasswordSecurity.verify(currentPassword, stored)
                    : currentPassword.equals(stored);
            if (!valid) {
                return false;
            }
            ContentValues values = new ContentValues();
            values.put(COL_PASSWORD, PasswordSecurity.hash(newPassword));
            return db.update(TABLE_USERS, values, COL_USER_ID + " = ?",
                    new String[]{cursor.getString(0)}) > 0;
        } finally {
            cursor.close();
        }
    }

    // =====================================================
    // SAVE MEASUREMENTS
    // =====================================================

    public boolean saveMeasurements(
            String email,
            String shoulder,
            String chest,
            String waist,
            String hips,
            String sleeveLength,
            String dressLength
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("user_email", email);
        values.put("shoulder", shoulder);
        values.put("chest", chest);
        values.put("waist", waist);
        values.put("hips", hips);
        values.put("sleeve_length", sleeveLength);
        values.put("dress_length", dressLength);

        int updated = db.update(TABLE_MEASUREMENTS, values,
                "user_email = ? COLLATE NOCASE", new String[]{email});
        if (updated > 0) return true;
        return db.insert(TABLE_MEASUREMENTS, null, values) != -1;
    }

    // =====================================================
    // GET SAVED MEASUREMENTS
    // =====================================================

    public Cursor getMeasurements(String email) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                TABLE_MEASUREMENTS,
                new String[]{
                        "id",
                        "user_email",
                        "shoulder",
                        "chest",
                        "waist",
                        "hips",
                        "sleeve_length",
                        "dress_length"
                },
                "user_email = ? COLLATE NOCASE",
                new String[]{email},
                null,
                null,
                null
        );
    }

    public long saveDesign(String email, String name, JSONObject design,
                           String previewPath, boolean favorite) {
        Long userId = getUserId(email);
        if (userId == null) {
            return -1;
        }
        ContentValues values = designValues(name, design, previewPath, favorite);
        values.put("user_id", userId);
        values.put("user_email", email);
        return getWritableDatabase().insert(TABLE_DESIGNS, null, values);
    }

    public boolean updateDesign(long id, String email, String name,
                                JSONObject design, String previewPath) {
        ContentValues values = designValues(name, design, previewPath, false);
        return getWritableDatabase().update(TABLE_DESIGNS, values,
                "id = ? AND user_email = ? COLLATE NOCASE",
                new String[]{String.valueOf(id), email}) > 0;
    }

    public Cursor getDesigns(String email, boolean favoritesOnly) {
        return getReadableDatabase().query(TABLE_DESIGNS, null,
                favoritesOnly ? "user_email = ? COLLATE NOCASE AND is_favorite = 1"
                        : "user_email = ? COLLATE NOCASE",
                new String[]{email}, null, null, "id DESC");
    }

    public boolean setDesignFavorite(long id, String email, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put("is_favorite", favorite ? 1 : 0);
        return getWritableDatabase().update(TABLE_DESIGNS, values,
                "id = ? AND user_email = ? COLLATE NOCASE",
                new String[]{String.valueOf(id), email}) > 0;
    }

    public boolean renameDesign(long id, String email, String name) {
        ContentValues values = new ContentValues();
        values.put("design_name", name);
        return getWritableDatabase().update(TABLE_DESIGNS, values,
                "id = ? AND user_email = ? COLLATE NOCASE",
                new String[]{String.valueOf(id), email}) > 0;
    }

    public boolean deleteDesign(long id, String email) {
        return getWritableDatabase().delete(TABLE_DESIGNS,
                "id = ? AND user_email = ? COLLATE NOCASE",
                new String[]{String.valueOf(id), email}) > 0;
    }

    public long duplicateDesign(long id, String email, String newName) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_DESIGNS, null, "id = ? AND user_email = ? COLLATE NOCASE",
                new String[]{String.valueOf(id), email}, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return -1;
            }
            ContentValues values = new ContentValues();
            String[] columns = {"category", "template_name", "fabric", "color",
                    "sleeve", "neckline", "pocket", "cuff", "dress_length",
                    "fit_style", "pattern", "custom_text", "logo_path", "design_data",
                    "preview_path"};
            for (String column : columns) {
                int index = cursor.getColumnIndex(column);
                if (index >= 0 && !cursor.isNull(index)) {
                    values.put(column, cursor.getString(index));
                }
            }
            Long userId = getUserId(email);
            if (userId == null) {
                return -1;
            }
            values.put("user_id", userId);
            values.put("user_email", email);
            values.put("design_name", newName);
            values.put("is_favorite", 0);
            values.put("created_at", java.text.DateFormat.getDateTimeInstance()
                    .format(new java.util.Date()));
            return db.insert(TABLE_DESIGNS, null, values);
        } finally {
            cursor.close();
        }
    }

    private Long getUserId(String email) {
        Cursor cursor = getReadableDatabase().query(TABLE_USERS,
                new String[]{COL_USER_ID}, COL_EMAIL + " = ? COLLATE NOCASE",
                new String[]{email}, null, null, null);
        try {
            return cursor.moveToFirst() ? cursor.getLong(0) : null;
        } finally {
            cursor.close();
        }
    }

    private ContentValues designValues(String name, JSONObject design,
                                       String previewPath, boolean favorite) {
        JSONObject data = design == null ? new JSONObject() : design;
        ContentValues values = new ContentValues();
        values.put("design_name", name);
        values.put("category", data.optString("category", "Garment"));
        values.put("template_name", data.optString("template", ""));
        values.put("fabric", data.optString("fabric", ""));
        values.put("color", data.optString("colorName", ""));
        values.put("sleeve", data.optString("sleeve", ""));
        values.put("neckline", data.optString("neckline", ""));
        values.put("pocket", data.optString("pocket", ""));
        values.put("cuff", data.optString("cuff", ""));
        values.put("dress_length", data.optString("dressLength", ""));
        values.put("fit_style", data.optString("fitStyle", ""));
        values.put("pattern", data.optString("pattern", ""));
        values.put("custom_text", data.optString("text", ""));
        values.put("logo_path", data.optString("logoPath", ""));
        values.put("design_data", data.toString());
        values.put("preview_path", previewPath);
        values.put("is_favorite", favorite ? 1 : 0);
        return values;
    }
}