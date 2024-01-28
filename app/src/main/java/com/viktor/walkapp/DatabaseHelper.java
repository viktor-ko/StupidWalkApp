package com.viktor.walkapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "walking_routes_v4.db";
    public static final String TABLE_NAME = "walking_routes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ROUTE_START = "routeStart";
    public static final String COLUMN_ROUTE_END = "routeEnd";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_ADDRESS = "address";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_ROUTE_START + " TEXT, "
                    + COLUMN_ROUTE_END + " TEXT, "
                    + COLUMN_ADDRESS + " TEXT, "
                    + COLUMN_DISTANCE + " REAL, "
                    + COLUMN_DURATION + " INTEGER)";
        db.execSQL(createTableQuery);
        }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}