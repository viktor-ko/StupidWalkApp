package com.viktor.walkapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

public class RouteListAdapter extends CursorAdapter {

    public RouteListAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_route, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textAddress = view.findViewById(R.id.text_address);
        TextView textDistance = view.findViewById(R.id.text_distance);
        TextView textDuration = view.findViewById(R.id.text_duration);

        // Extract data from the cursor and put it to the TextViews
        @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));
        @SuppressLint("Range") double distance = cursor.getDouble(cursor.getColumnIndex("distance"));
        @SuppressLint("Range") long duration = cursor.getLong(cursor.getColumnIndex("duration"));

        textAddress.setText(address);
        textDistance.setText("Distance: " + distance + " km");
        textDuration.setText("Walking time: " + duration + " min");
    }
}
