package com.viktor.walkapp;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "walking_routes_v4.db";
    public static final String TABLE_NAME = "walking_routes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ROUTE_START = "routeStart";
    public static final String COLUMN_ROUTE_END = "routeEnd";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_DURATION = "duration";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_ROUTE_START + " TEXT, "
                    + COLUMN_ROUTE_END + " TEXT, "
                    + COLUMN_DISTANCE + " REAL, "
                    + COLUMN_DURATION + " INTEGER)";
        db.execSQL(createTableQuery);
        }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public static final class DatabaseContract {
        private DatabaseContract() {
        }
        public static class RouteEntry implements BaseColumns {
            public static final String TABLE_NAME = "walking_routes";
            public static final String COLUMN_ID = "ID";
            public static final String COLUMN_DISTANCE = "distance";
            public static final String COLUMN_DURATION = "duration";
        }
    }

    public Cursor getAllRoutes() {
        SQLiteDatabase db = this.getReadableDatabase();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DatabaseContract.RouteEntry.TABLE_NAME);

        // Query the database to get the column names
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + DatabaseContract.RouteEntry.TABLE_NAME + ")", null);

        // Calculate the total number of columns
        int totalColumns = cursor.getCount();

        // Initialize the projection array
        String[] projection = new String[totalColumns];

        // Construct the projection array dynamically
        for (int i = 0; i < totalColumns; i++) {
            projection[i] = "COLUMN" + (i + 1);
        }

        // Close the cursor used to get column names
        cursor.close();

        // Execute the query
        Cursor resultCursor = queryBuilder.query(
                db,
                projection,
                null,
                null,
                null,
                null,
                DatabaseContract.RouteEntry.COLUMN_ID // Sort by COLUMN_ID
        );

        return resultCursor;
    }

    // delete later
    public void printDatabaseContents() {
        Log.d("DatabaseDebug", "Printing database contents...");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    String routeStart = cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE_START));
                    String routeEnd = cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE_END));
                    double distance = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISTANCE));
                    long duration = cursor.getLong(cursor.getColumnIndex(COLUMN_DURATION));

                    Log.d("DatabaseDebug", "ID: " + id + ", Route Start: " + routeStart + ", Route End: " + routeEnd + ", Distance: " + distance + ", Duration: " + duration);
                } while (cursor.moveToNext());
            } else {
                Log.d("DatabaseDebug", "No data in the database.");
            }
        } finally {
            cursor.close();
            db.close();
        }
    }

}

