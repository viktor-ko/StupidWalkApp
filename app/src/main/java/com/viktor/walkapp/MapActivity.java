package com.viktor.walkapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        //retrieving Intent data
        String routeStart = getIntent().getStringExtra("ROUTE_START");
        String routeEnd = getIntent().getStringExtra("ROUTE_END");
        long itemId = getIntent().getLongExtra("ITEM_ID", -1);

        if (routeStart != null && routeEnd != null && itemId != -1) {

            //parse routeStart to get start point latlong
            String[] latLng = routeStart.split(",");
            double latitude = Double.parseDouble(latLng[0]);
            double longitude = Double.parseDouble(latLng[1]);

            //parse routeEnd to get end point latlong
            String[] endLatLng = routeEnd.split(",");
            double endLatitude = Double.parseDouble(endLatLng[0]);
            double endLongitude = Double.parseDouble(endLatLng[1]);

            //create LatLng objects for start and end points
            LatLng startLocation = new LatLng(latitude, longitude);
            LatLng endLocation = new LatLng(endLatitude, endLongitude);

            //adding markers
            mMap.addMarker(new MarkerOptions()
                    .position(startLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                    .title("Start"));

            mMap.addMarker(new MarkerOptions()
                    .position(endLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker))
                    .title("End"));

            //get the bearing between start and end points
            float bearing = getBearing(startLocation, endLocation);

            //creating bounds for start and end points
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(startLocation);
            boundsBuilder.include(endLocation);
            LatLngBounds bounds = boundsBuilder.build();

            //set the camera parameters
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(bounds.getCenter())
                    .zoom(16)
                    .bearing(bearing)
                    .tilt(60)
                    .build();

            //adjust camera position with the new parameters
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.animateCamera(cameraUpdate);

            //get route between start and end points using openrouteservice API
            String url = "https://api.openrouteservice.org/v2/directions"
                    + "foot-walking"
                    + "?api_key=5b3ce3597851110001cf624860d4a9b68f9540bb8a7bd10b7055866e"
                    + "&start=" + startLocation.longitude + "," + startLocation.latitude
                    + "&end=" + endLocation.longitude + "," + endLocation.latitude;
            new DownloadGeoJson().execute(url);
        }
    }

    private class DownloadGeoJson extends AsyncTask<String, Void, GeoJsonLayer> {
        @Override
        protected GeoJsonLayer doInBackground(String... params) {
            try {
                InputStream stream = new URL(params[0]).openStream();

                String line;
                StringBuilder result = new StringBuilder();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream));

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                stream.close();

                return new GeoJsonLayer(mMap, new JSONObject(result.toString()));
            } catch (IOException e) {
                Log.e("mLogTag", "GeoJSON file could not be read");
            } catch (JSONException e) {
                Log.e("mLogTag",
                        "GeoJSON file could not be converted to a JSONObject");
            }
            return null;
        }

        @Override
        protected void onPostExecute(GeoJsonLayer layer) {
            if (layer != null) {
                GeoJsonLineStringStyle lineStringStyle =
                        layer.getDefaultLineStringStyle();
                lineStringStyle.setColor(Color.parseColor("#07E9CD"));
                lineStringStyle.setWidth(10f);

                layer.addLayerToMap();
            }
        }
    }
    //calculate the bearing in degrees between start and end points
    private float getBearing(LatLng start, LatLng end) {
        double startLat = Math.toRadians(start.latitude);
        double startLng = Math.toRadians(start.longitude);
        double endLat = Math.toRadians(end.latitude);
        double endLng = Math.toRadians(end.longitude);

        double deltaLng = endLng - startLng;

        double y = Math.sin(deltaLng) * Math.cos(endLat);
        double x = Math.cos(startLat) * Math.sin(endLat) -
                Math.sin(startLat) * Math.cos(endLat) * Math.cos(deltaLng);
        double bearing = Math.toDegrees(Math.atan2(y, x));

        return (float) bearing;
    }
}


