package com.viktor.walkapp;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

    public class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "walking_routes.db";
        private static final String TABLE_NAME = "walking_routes";
        private static final String COLUMN_ID = "id";
        private static final String COLUMN_LATITUDE = "latitude";
        private static final String COLUMN_LONGITUDE = "longitude";
        private static final String COLUMN_TYPE = "type";
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

        public void insertLocation(SQLiteDatabase db, double latitude, double longitude, String type) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LATITUDE, latitude);
            values.put(COLUMN_LONGITUDE, longitude);
            values.put(COLUMN_TYPE, type);

            db.insert(TABLE_NAME, null, values);
        }
    }