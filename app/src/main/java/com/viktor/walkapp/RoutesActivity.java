package com.viktor.walkapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class RoutesActivity extends AppCompatActivity {
    ListView listView;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;
    RouteListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        listView = findViewById(R.id.listViewRoutes);
        dbHelper = new DatabaseHelper(this);

        try {
            dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //extract data from the database
        database = dbHelper.getWritableDatabase();
        cursor = database.rawQuery("SELECT id as _id, * FROM " + DatabaseHelper.TABLE_NAME, null);

        adapter = new RouteListAdapter(this, cursor);
        listView.setAdapter(adapter);

        Button buttonMap = findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoutesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}


