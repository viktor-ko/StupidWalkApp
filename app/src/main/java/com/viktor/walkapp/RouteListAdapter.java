package com.viktor.walkapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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
        TextView textViewRoute = view.findViewById(R.id.textViewRoute);

        // Extract data from the cursor and set it to the TextView
        String routeText = "Route " + cursor.getInt(cursor.getColumnIndex(DatabaseHelper.DatabaseContract.RouteEntry.COLUMN_ID)) +
                ": " + cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.DatabaseContract.RouteEntry.COLUMN_DISTANCE)) + " km " +
                cursor.getLong(cursor.getColumnIndex(DatabaseHelper.DatabaseContract.RouteEntry.COLUMN_DURATION)) + " min";

        textViewRoute.setText(routeText);
    }
}