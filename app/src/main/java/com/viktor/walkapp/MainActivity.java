package com.viktor.walkapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng newPos) {
                Button main_button = findViewById(R.id.main_button);
                main_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        generateRandomPoints(currentLocation);
                    }
                });
            }
        });
    }

    public void onWalkButtonClick(View view) {
        // Get the current location using LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                LatLng currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                // Generate 3 random points and display 3 walking routes from current location
                generateRandomPoints(currentLocation);
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
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));

                    MarkerOptions myMarker = new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                            .position(currentLocation)
                            .anchor(0.5f, 1)
                            .alpha(1f)
                            .title("Start walking!");
                    mMap.addMarker(myMarker);
                }
            }
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//            }
//
//            public void onProviderEnabled(String provider) {
//            }
//
//            public void onProviderDisabled(String provider) {
//            }
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
    private List<LatLng> generateRandomPoints(LatLng currentLocation) {
        List<LatLng> randomPoints = new ArrayList<>();
        Random random = new Random();

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

        insertLocation(currentLocation.latitude, currentLocation.longitude, "start");

        for (int i = 0; i < 3; i++) {
            double distance = minDistanceKm + random.nextDouble() * (maxDistanceKm - minDistanceKm);
            double bearing = random.nextDouble() * 360; // To avoid clustering of points and generation only on the north

            LatLng randomPoint = getPointAtDistanceAndBearing(currentLocation, distance, bearing);

            if (randomPoint != null) {
                randomPoints.add(randomPoint);

                insertLocation(randomPoint.latitude, randomPoint.longitude, "random");

//        for (int i = 0; i < 3; i++) {
//            double latOffset = random.nextDouble() * 0.01;  // 0.01 degrees is around 1 km
//            double lngOffset = random.nextDouble() * 0.01;
//
//            LatLng randomPoint = new LatLng(
//                    currentLocation.latitude + latOffset,
//                    currentLocation.longitude + lngOffset
//            );
//
//            randomPoints.add(randomPoint);

                // Assign markers based on index
                String markerResourceName;
                switch (i) {
                    case 0:
                        markerResourceName = "one";
                        break;
                    case 1:
                        markerResourceName = "two";
                        break;
                    case 2:
                        markerResourceName = "three";
                        break;
                    default:
                        markerResourceName = "one"; // Default to "one" if unexpected index
                        break;
                }

                // Add markers for random points with individual PNG files
                int markerResourceId = getResources().getIdentifier(markerResourceName, "drawable", getPackageName());
                if (mMap != null) {
                    mMap.addMarker(new MarkerOptions()
                            .position(randomPoint)
                            .icon(BitmapDescriptorFactory.fromResource(markerResourceId)));
                }

            }
        }

        // Call a method to display walking routes for these random points
        displayWalkingRoutes(currentLocation, randomPoints);

        dbHelper.printDatabaseContents(); //delete

        return randomPoints;
    }
    private void insertLocation(double latitude, double longitude, String type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_LATITUDE, latitude);
        values.put(DatabaseHelper.COLUMN_LONGITUDE, longitude);
        values.put(DatabaseHelper.COLUMN_TYPE, type);

        db.insert(DatabaseHelper.TABLE_NAME, null, values);

        db.close();
    }
    private LatLng getPointAtDistanceAndBearing(LatLng start, double distance, double bearing) {
        double earthRadius = 6371.0; // Radius of the Earth in kilometers
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

            new DownloadGeoJsonFile().execute(url);
    }
    }
    private class DownloadGeoJsonFile extends AsyncTask<String, Void, GeoJsonLayer> {

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
}

//random points
//    double lat_min = -90;
//    double lat_max = 90;
//    double lng_min = -180;
//    double lng_max = 180;
//
//    Random r = new Random();
//    double random_lat = lat_min + (lat_max - lat_min) * r.nextDouble();
//    double random_lng = lng_min + (lng_max - lng_min) * r.nextDouble();
//
//    Location random_location = new Location("");
//random_location.setLatitude(random_lat);
//        random_location.setLongitude(random_lng);




//public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//    private FusedLocationProviderClient fusedLocationProviderClient;
//
//    ActivityResultLauncher<String[]> locationPermissionRequest;
//
//    @SuppressLint("MissingPermission")
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        SupportMapFragment mapFragment = (SupportMapFragment)
//                getSupportFragmentManager().findFragmentById(R.id.map);
//
//        mapFragment.getMapAsync(this);
//
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//
//        locationPermissionRequest = registerForActivityResult(
//                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
//                    Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
//                    Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
//
//                    if ((fineLocationGranted != null && fineLocationGranted) || (coarseLocationGranted != null && coarseLocationGranted)) {
//                        mMap.setMyLocationEnabled(true);
//                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//                    } else {
//                        Toast.makeText(this,
//                                "Location cannot be obtained due to missing permission.",
//                                Toast.LENGTH_LONG).show();
//                    }
//                }
//        );
//    }
//
//    @Override
//    public void onMapReady(GoogleMap map) {
//        mMap = map;
//
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//
//        mMap.getUiSettings().setMapToolbarEnabled(true);
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        fusedLocationProviderClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
//
//                        MarkerOptions myMarker = new MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.star))
//                                .position(currentLocation)
//                                .anchor(0.5f, 1)
//                                .alpha(0.7f)
//                                .title("Start");
//                        mMap.addMarker(myMarker);
//                        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                            @Override
//                            public void onMapClick(LatLng newPos) {
//                                MarkerOptions clickMarker = new MarkerOptions().position(newPos);
//                                mMap.addMarker(clickMarker);
//                                String url = "https://api.openrouteservice.org/v2/directions"
//                                        + "foot-walking"
//                                        + "?api_key=5b3ce3597851110001cf624860d4a9b68f9540bb8a7bd10b7055866e"
//                                        + "&start="
//                                        + myMarker.getPosition().longitude + ","
//                                        + myMarker.getPosition().latitude
//                                        + "&end="
//                                        + clickMarker.getPosition().longitude + ","
//                                        + clickMarker.getPosition().latitude;
//                                new DownloadGeoJsonFile().execute(url);
//                            }
//                        });
//                    }
//                });
//    }
//
//
