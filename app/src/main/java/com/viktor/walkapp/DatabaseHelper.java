package com.viktor.walkapp;

import android.content.ContentValues;
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
    private SQLiteDatabase db = this.getWritableDatabase();

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

    //inserts to db route start and route end coordinates, route distance and duration
    public long insertLocation(String routeStart, String routeEnd, Double distance, Long duration) {
        ContentValues values = new ContentValues();

        if (routeStart != null) {
            values.put(COLUMN_ROUTE_START, routeStart);
        }
        if (routeEnd != null) {
            values.put(COLUMN_ROUTE_END, routeEnd);
        }
        if (distance != null) {
            values.put(COLUMN_DISTANCE, distance);
        }
        if (duration != null) {
            values.put(COLUMN_DURATION, duration);
        }

        return this.db.insert(TABLE_NAME, null, values);
    }

    //insert the closest address around random point to database
    public void updateAddress(long rowId, String address) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADDRESS, address);
        this.db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(rowId)});
    }

    //clear the database
    public void clearDatabase() {
        this.db.delete(TABLE_NAME, null, null);
    }
}