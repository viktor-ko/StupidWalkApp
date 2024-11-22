package com.viktor.walkapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
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

        //access database by clicking the item in list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                String routeStart = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ROUTE_START));
                String routeEnd = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ROUTE_END));
                long itemId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));

                Intent intent = new Intent(RoutesActivity.this, MapActivity.class);

                intent.putExtra("ROUTE_START", routeStart);
                intent.putExtra("ROUTE_END", routeEnd);
                intent.putExtra("ITEM_ID", itemId);

                startActivity(intent);
            }
        });

        Button buttonMap = findViewById(R.id.buttonMap);
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes the current activity and returns to the previous one in the back stack
            }
        });
    }
}