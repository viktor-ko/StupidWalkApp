package com.viktor.walkapp;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "walking_routes.db";
    public static final String TABLE_NAME = "walking_routes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TYPE = "type";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_LATITUDE + " REAL, "
                    + COLUMN_LONGITUDE + " REAL, "
                    + COLUMN_TYPE + " TEXT)";
        db.execSQL(createTableQuery);
        }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // delete later
    public void printDatabaseContents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                    String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));

                    Log.d("DatabaseDebug", "ID: " + id + ", Latitude: " + latitude + ", Longitude: " + longitude + ", Type: " + type);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
    }
}

