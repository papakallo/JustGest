package com.example.android_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class GestureDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gestures.db";
    private static final int DATABASE_VERSION = 1;

    // users
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_NAME = "name";
    private static final String COL_CREATED_AT = "created_at";

    // gestures
    private static final String TABLE_GESTURES = "gesture_samples";
    private static final String COL_GESTURE_ID = "id";
    private static final String COL_GESTURE_USER_ID = "user_id";
    private static final String COL_SAMPLE_NUMBER = "sample_number";
    private static final String COL_GESTURE_DATA = "gesture_data";  // json with all of the data
    private static final String COL_GESTURE_TIMESTAMP = "timestamp";

    public GestureDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT UNIQUE NOT NULL, " +
                COL_CREATED_AT + " INTEGER NOT NULL)";
        db.execSQL(createUsersTable);

        String createGesturesTable = "CREATE TABLE " + TABLE_GESTURES + " (" +
                COL_GESTURE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_GESTURE_USER_ID + " INTEGER NOT NULL, " +
                COL_SAMPLE_NUMBER + " INTEGER NOT NULL, " +
                COL_GESTURE_DATA + " TEXT NOT NULL, " +
                COL_GESTURE_TIMESTAMP + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_GESTURE_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE)";
        db.execSQL(createGesturesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GESTURES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean userExists(String userName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_NAME + "=?",
                new String[]{userName},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // user and it's 5 gestures
    public boolean saveUser(String userName, List<String> gestures) {
        if (gestures.size() < 5) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues userValues = new ContentValues();
            userValues.put(COL_USER_NAME, userName);
            userValues.put(COL_CREATED_AT, System.currentTimeMillis());

            long userId = db.insert(TABLE_USERS, null, userValues);

            if (userId == -1) {
                return false;
            }

            for (int i = 0; i < gestures.size(); i++) {
                ContentValues gestureValues = new ContentValues();
                gestureValues.put(COL_GESTURE_USER_ID, userId);
                gestureValues.put(COL_SAMPLE_NUMBER, i + 1);
                gestureValues.put(COL_GESTURE_DATA, gestures.get(i));
                gestureValues.put(COL_GESTURE_TIMESTAMP, System.currentTimeMillis());

                long result = db.insert(TABLE_GESTURES, null, gestureValues);
                if (result == -1) {
                    db.endTransaction();
                    return false;
                }
            }

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public List<String> getAllUserNames() {
        List<String> userNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_NAME},
                null, null, null, null,
                COL_USER_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                userNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return userNames;
    }

    public List<String> getUserGestures(String userName) {
        List<String> gestures = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // user ID first
        Cursor userCursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_NAME + "=?",
                new String[]{userName},
                null, null, null);

        if (!userCursor.moveToFirst()) {
            userCursor.close();
            return gestures;
        }

        long userId = userCursor.getLong(0);
        userCursor.close();

        // all user gestures
        Cursor gestureCursor = db.query(TABLE_GESTURES,
                new String[]{COL_GESTURE_DATA},
                COL_GESTURE_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                COL_SAMPLE_NUMBER + " ASC");

        if (gestureCursor.moveToFirst()) {
            do {
                gestures.add(gestureCursor.getString(0));
            } while (gestureCursor.moveToNext());
        }

        gestureCursor.close();
        return gestures;
    }

    public boolean deleteUser(String userName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_USERS,
                COL_USER_NAME + "=?",
                new String[]{userName});
        return rowsDeleted > 0;
    }

    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
