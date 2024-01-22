package com.viktor.walkapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.database.Cursor;
import android.widget.ListView;

import java.io.IOException;

public class RoutesActivity extends AppCompatActivity {
    ListView listView;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        listView = findViewById(R.id.list_item_route);
        dbHelper = new DatabaseHelper(this);

        try {
            dbHelper.createDataBase();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        database = dbHelper.getDataBase();
        cursor = database.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME, null);

        String[] fromColumns = {DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_DISTANCE, DatabaseHelper.COLUMN_DURATION};
        int[] toViews = {R.id.text_id, R.id.text_distance, R.id.text_duration};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.activity_routes,
                cursor,
                fromColumns,
                toViews,
                0
        );

        listView.setAdapter(adapter);
    }
}
