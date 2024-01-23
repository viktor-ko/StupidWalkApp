package com.viktor.walkapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import android.widget.Button;

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


    }

    public void onWalkButtonClick(View view) {
        // Get the current location using LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                // Generate 3 random points within 1-1.5 km from current location
                List<LatLng> randomPoints = generateRandomPoints(currentLocation);

                // Display 3 walking routes from current location to the generated random points
                displayWalkingRoutes(currentLocation, randomPoints);
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        }
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

    public void onListViewButtonClick(View view) {
        Intent intent = new Intent(this, RoutesActivity.class);
        startActivity(intent);
    }

    private List<LatLng> generateRandomPoints(LatLng currentLocation) {
        List<LatLng> randomPoints = new ArrayList<>();
        Random random = new Random();

        //clear map from all markers
        mMap.clear();

        MarkerOptions myMarker = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                .position(currentLocation)
                .anchor(0.5f, 1)
                .alpha(1f)
                .title("Start walking!");
        mMap.addMarker(myMarker);

        double minDistanceKm = 1.0;
        double maxDistanceKm = 1.5;

        for (int i = 0; i < 3; i++) {
            double distance = minDistanceKm + random.nextDouble() * (maxDistanceKm - minDistanceKm);
            double bearing = random.nextDouble() * 360; // to avoid clustering of points and generation only on the north from current location

            LatLng randomPoint = getPointAtDistanceAndBearing(currentLocation, distance, bearing);

            if (randomPoint != null) {
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
                mMap.addMarker(new MarkerOptions()
                        .position(randomPoint)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.point_marker)));

            }
        }
        return randomPoints;
    }

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

    private LatLng getPointAtDistanceAndBearing(LatLng start, double distance, double bearing) {
        double earthRadius = 6371.0; // Radius of the Earth in km
        double lat1 = Math.toRadians(start.latitude);
        double lon1 = Math.toRadians(start.longitude);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / earthRadius) +
                Math.cos(lat1) * Math.sin(distance / earthRadius) * Math.cos(Math.toRadians(bearing)));
        double lon2 = lon1 + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(distance / earthRadius) * Math.cos(lat1),
                Math.cos(distance / earthRadius) - Math.sin(lat1) * Math.sin(lat2));

        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);

        return new LatLng(lat2, lon2);
    }

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

                                        //convert current location LatLng to a string (latitude,longitude)
                                        String routeStart = currentLocation.latitude + "," + currentLocation.longitude;

                                        //convert random point LatLng to a string (latitude,longitude)
                                        String routeEnd = randomPoint.latitude + "," + randomPoint.longitude;

                                        //insert data into the database
                                        long rowId =insertLocation(routeStart, routeEnd, distanceKilometers, durationMinutes);

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
                dbHelper.printDatabaseContents(); //delete
        }
    }

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

    private void updateAddress(long rowId, String address) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ADDRESS, address);
        db.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(rowId)});
    }
}
