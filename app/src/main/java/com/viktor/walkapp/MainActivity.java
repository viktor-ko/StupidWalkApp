package com.viktor.walkapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng currentLocation;
    private DatabaseHelper dbHelper;

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
    }
    //button STUPID WALK
    public void onWalkButtonClick(View view) {
        //get current location using LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                return;
            }
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                //generate 3 random points within 1-1.5 km from current location
                List<LatLng> randomPoints = generateRandomPoints(currentLocation);

                //display 3 walking routes from current location to the generated random points
                displayWalkingRoutes(currentLocation, randomPoints);

                //move camera position to fit all points
                zoomToPoints(randomPoints, currentLocation);
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //button List view
    public void onListViewButtonClick(View view) {
        Intent intent = new Intent(this, RoutesActivity.class);
        startActivity(intent);
    }

    //zoom camera to fit the current location and generated points
    private void zoomToPoints(List<LatLng> points, LatLng currentLocation) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(currentLocation);

        for (LatLng point : points) {
            builder.include(point);
        }

        LatLngBounds bounds = builder.build();
        int padding = 100; // Adjust this value as needed

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));

                    MarkerOptions myMarker = new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                            .position(currentLocation)
                            .anchor(0.5f, 1)
                            .alpha(1f)
                            .title("Start walking!");
                    mMap.addMarker(myMarker);
                }
            }
        };

        ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if (fineLocationGranted != null && fineLocationGranted) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
                        mMap.setMyLocationEnabled(true);
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    } else {
                        Toast.makeText(this, "Location cannot be obtained due to missing permission.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        locationPermissionRequest.launch(PERMISSIONS);

    }

    //generate 3 random points within walking distance from current location
    private List<LatLng> generateRandomPoints(LatLng currentLocation) {
        List<LatLng> randomPoints = new ArrayList<>();
        Random random = new Random();

        //clear map from all markers
        mMap.clear();

        //add marker for the current location
        MarkerOptions myMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                .position(currentLocation)
                .anchor(0.5f, 1)
                .alpha(1f)
                .title("Start walking!");
        mMap.addMarker(myMarker);

        //defining min and max distance from current location
        //to ensure the walk will be no less than ~15 min, but no longer than ~30 min
        double minDistanceKm = 1.0;
        double maxDistanceKm = 1.5;

        //Alternative method of generation of random coordinates - instead of generating random coordinate,
        //now I generate random angle from current location and random distance (between 1-1.5 km)
        //Used due to clustering of points and generation only on the north from current location with previous method
        for (int i = 0; i < 3; i++) {
            double distance = minDistanceKm + random.nextDouble() * (maxDistanceKm - minDistanceKm);// get random distance within the specified range
            double bearing = random.nextDouble() * 360; // adding random angle

            //get new random point based on the distance and bearing
            LatLng randomPoint = getPointAtDistanceAndBearing(currentLocation, distance, bearing);

            //add random points to the list
            randomPoints.add(randomPoint);
//                // Assign markers based on index
//                String markerResourceName;
//                switch (i) {
//                    case 0:
//                        markerResourceName = "one";
//                        break;
//                    case 1:
//                        markerResourceName = "two";
//                        break;
//                    case 2:
//                        markerResourceName = "three";
//                        break;
//                    default:
//                        markerResourceName = "one"; // Default to "one" if unexpected index
//                        break;
//                }
//
//                // Add markers for random points from PNG files
//                int markerResourceId = getResources().getIdentifier(markerResourceName, "drawable", getPackageName());
//                if (mMap != null) {

            //add marker for each random point
            mMap.addMarker(new MarkerOptions()
                    .position(randomPoint)
                    .title("Well done!")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker)));
        }
        return randomPoints;
    }
    //calculate new point based on starting point, distance, and bearing
    private LatLng getPointAtDistanceAndBearing(LatLng start, double distance, double bearing) {
        double earthRadius = 6371.0; //Earth radius in km
        //convert lat long to radians
        double lat1 = Math.toRadians(start.latitude);
        double lon1 = Math.toRadians(start.longitude);

        //calculate new latitude using Haversine formula - "very accurate way of computing distances
        //between two points on the surface of a sphere using the latitude and longitude of the two points,
        //more useful for small angles and distances."
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / earthRadius) +
                Math.cos(lat1) * Math.sin(distance / earthRadius) * Math.cos(Math.toRadians(bearing)));
        double lon2 = lon1 + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(distance / earthRadius) * Math.cos(lat1),
                Math.cos(distance / earthRadius) - Math.sin(lat1) * Math.sin(lat2));

        //convert back to degrees
        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        return new LatLng(lat2, lon2);
    }

    //get the routes from current location to generated random points using openrouteservice API
    private void displayWalkingRoutes(LatLng currentLocation, List<LatLng> randomPoints) {
        for (LatLng randomPoint : randomPoints) {
            String url = "https://api.openrouteservice.org/v2/directions"
                    + "foot-walking"
                    + "?api_key=5b3ce3597851110001cf624860d4a9b68f9540bb8a7bd10b7055866e"
                    + "&start=" + currentLocation.longitude + "," + currentLocation.latitude
                    + "&end=" + randomPoint.longitude + "," + randomPoint.latitude;

            new DownloadGeoJsonFile(randomPoint).execute(url);
        }
    }
    //downloads geojson route file and parse it to get distance and summary information
    private class DownloadGeoJsonFile extends AsyncTask<String, Void, Pair<GeoJsonLayer, JSONObject>> {
        private LatLng randomPoint;

        public DownloadGeoJsonFile(LatLng randomPoint) {
            this.randomPoint = randomPoint;
        }

        @Override
        protected Pair<GeoJsonLayer, JSONObject> doInBackground(String... params) {
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

                if (result.length() > 0) {
                    JSONObject jsonObject = new JSONObject(result.toString());
                    return new Pair<>(new GeoJsonLayer(mMap, jsonObject), jsonObject);
                } else {
                    Log.e("mLogTag", "Empty or invalid JSON response");
                    return null;
                }
            } catch (IOException e) {
                Log.e("mLogTag", "GeoJSON file could not be read");
            } catch (JSONException e) {
                Log.e("mLogTag", "Error parsing JSON response");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Pair<GeoJsonLayer, JSONObject> resultPair) {
            if (resultPair != null) {
                GeoJsonLayer layer = resultPair.first;
                JSONObject jsonObject = resultPair.second;

                // styling and adding geojson to the map
                if (layer != null) {
                    GeoJsonLineStringStyle lineStringStyle = layer.getDefaultLineStringStyle();
                    lineStringStyle.setColor(Color.parseColor("#07E9CD"));
                    lineStringStyle.setWidth(10f);

                    layer.addLayerToMap();

                    // Access GeoJSON structure
                    try {
                        JSONArray features = jsonObject.getJSONArray("features");

                        if (features.length() > 0) {
                            JSONObject feature = features.getJSONObject(0);
                            JSONObject properties = feature.optJSONObject("properties");

                            if (properties != null) {
                                JSONObject summary = properties.optJSONObject("summary");

                                if (summary != null) {

                                    //extract route distance (meters) and duration (seconds) from GeoJSON
                                    double distance = summary.getDouble("distance");
                                    double duration = summary.getDouble("duration");

                                    //convert meters and seconds to kilometers and minutes
                                    double distanceKilometers = Math.round(distance / 1000.0 * 100.0) / 100.0;
                                    long durationMinutes = Math.round(duration / 60.0);

                                    //convert current location LatLng to a string (latitude, longitude)
                                    String routeStart = currentLocation.latitude + ", " + currentLocation.longitude;

                                    //convert random point LatLng to a string (latitude,longitude)
                                    String routeEnd = randomPoint.latitude + ", " + randomPoint.longitude;

                                    //insert data into the database and gets the row ID
                                    long rowId = insertLocation(routeStart, routeEnd, distanceKilometers, durationMinutes);

                                    //get address for the random point (reverse geocoding)
                                    resolveAndInsertAddress(randomPoint, rowId);

                                } else {
                                    Log.e("mLogTag", "No 'summary' object found in properties");
                                }
                            } else {
                                Log.e("mLogTag", "No 'properties' object found in feature");
                            }
                        } else {
                            Log.e("mLogTag", "No features found in GeoJSON");
                        }
                    } catch (JSONException e) {
                        Log.e("mLogTag", "Error extracting summary information");
                        e.printStackTrace();
                    }
                } else {
                    Log.e("mLogTag", "GeoJsonLayer is null");
                }
            } else {
                Log.e("mLogTag", "AsyncTask resultPair is null");
            }
//            dbHelper.printDatabaseContents();
        }
    }
    //inserts route start and route end coordinates, route distance and duration to sqlite db
    public long insertLocation(String routeStart, String routeEnd, Double distance, Long duration) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        if (routeStart != null) {
            values.put(DatabaseHelper.COLUMN_ROUTE_START, routeStart);
        }
        if (routeEnd != null) {
            values.put(DatabaseHelper.COLUMN_ROUTE_END, routeEnd);
        }
        if (distance != null) {
            values.put(DatabaseHelper.COLUMN_DISTANCE, distance);
        }
        if (duration != null) {
            values.put(DatabaseHelper.COLUMN_DURATION, duration);
        }

        return db.insert(DatabaseHelper.TABLE_NAME, null, values);
    }

    //getting the closest address to the random point
    private void resolveAndInsertAddress(LatLng randomPoint, long rowId) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(randomPoint.latitude, randomPoint.longitude, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                StringBuilder addressStringBuilder = new StringBuilder();

                //append each address line to the StringBuilder
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressStringBuilder.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressStringBuilder.append(", ");
                    }
                }

                String addressString = addressStringBuilder.toString();
                //update the address in the database
                updateAddress(rowId, addressString);
            } else {
                //update with no address found
                updateAddress(rowId, "No address found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            //update with error message
            updateAddress(rowId, "Error getting address");
        }
    }
    // insert the closest address around random point to database
    private void updateAddress(long rowId, String address) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ADDRESS, address);
        db.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(rowId)});
    }
}