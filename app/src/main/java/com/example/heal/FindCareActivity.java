package com.example.heal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FindCareActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "FindCareActivity";

    private MapView mapView;
    private RecyclerView rvHospitals;
    private HospitalAdapter adapter;
    private List<Hospital> hospitalList;
    private FusedLocationProviderClient fusedLocationClient;
    private OkHttpClient httpClient;
    
    private LinearLayout llSearchingNear;
    private TextView tvSearchingNear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize osmdroid configuration
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_find_care);

        // UI Initialization
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        rvHospitals = findViewById(R.id.rvHospitals);
        rvHospitals.setLayoutManager(new LinearLayoutManager(this));
        hospitalList = new ArrayList<>();
        adapter = new HospitalAdapter(hospitalList);
        rvHospitals.setAdapter(adapter);
        
        llSearchingNear = findViewById(R.id.llSearchingNear);
        tvSearchingNear = findViewById(R.id.tvSearchingNear);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        httpClient = new OkHttpClient();

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission is required to find nearby care.", Toast.LENGTH_LONG).show();
                tvSearchingNear.setText("Location permission denied");
            }
        }
    }

    private void getUserLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                // If on emulator or no location, default to Lahore, Pakistan
                double lat = 31.5204;
                double lng = 74.3587;
                
                if (location != null) {
                    // Use real location if we want, but for demo let's use the actual location 
                    // ONLY if it's not the default emulator Mountain View location
                    if (Math.abs(location.getLatitude() - 37.422) > 0.1) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                }
                
                double finalLat = lat;
                double finalLng = lng;
                
                tvSearchingNear.setText("Searching nearby hospitals...");
                
                setupMap(finalLat, finalLng);
                fetchHospitals(finalLat, finalLng);
            }).addOnFailureListener(e -> {
                // Fallback to Lahore, Pakistan
                double lat = 31.5204;
                double lng = 74.3587;
                setupMap(lat, lng);
                fetchHospitals(lat, lng);
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void setupMap(double lat, double lng) {
        GeoPoint startPoint = new GeoPoint(lat, lng);
        mapView.getController().setZoom(14.0);
        mapView.getController().setCenter(startPoint);

        // Add "You are here" marker
        Marker userMarker = new Marker(mapView);
        userMarker.setPosition(startPoint);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userMarker.setTitle("You are here");
        mapView.getOverlays().add(userMarker);
    }

    private void fetchHospitals(double userLat, double userLng) {
        // Query overpass API for hospitals within 8000 meters. 
        // Use node, way, and relation, and 'out center' so ways have a lat/lon.
        String query = "[out:json];(node[\"amenity\"=\"hospital\"](around:8000," + userLat + "," + userLng + ");" +
                       "way[\"amenity\"=\"hospital\"](around:8000," + userLat + "," + userLng + ");" +
                       "relation[\"amenity\"=\"hospital\"](around:8000," + userLat + "," + userLng + "););out center;";
                       
        okhttp3.HttpUrl url = okhttp3.HttpUrl.parse("https://overpass-api.de/api/interpreter").newBuilder()
                .addQueryParameter("data", query)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "HEAL_HealthCareApp/1.0 (murtaza@example.com)")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch hospitals: " + e.getMessage());
                runOnUiThread(() -> {
                    llSearchingNear.setVisibility(View.GONE);
                    Toast.makeText(FindCareActivity.this, "Failed to load hospitals.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonStr = response.body().string();
                    parseHospitalData(jsonStr, userLat, userLng);
                } else {
                    final int code = response.code();
                    final String msg = response.message();
                    runOnUiThread(() -> {
                        llSearchingNear.setVisibility(View.GONE);
                        Toast.makeText(FindCareActivity.this, "Error " + code + ": " + msg, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void parseHospitalData(String jsonStr, double userLat, double userLng) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
            JsonArray elements = jsonObject.getAsJsonArray("elements");
            
            List<Hospital> parsedList = new ArrayList<>();
            Random random = new Random();
            
            String[][] possibleSpecialties = {
                {"CARDIOLOGY", "NEUROLOGY", "PEDIATRICS"},
                {"ONCOLOGY", "DERMATOLOGY"},
                {"ORTHOPEDICS", "PHYSIOTHERAPY"},
                {"EMERGENCY", "TRAUMA", "SURGERY"},
                {"MATERNITY", "PEDIATRICS"}
            };

            for (JsonElement element : elements) {
                JsonObject obj = element.getAsJsonObject();
                if (!obj.has("tags")) continue;
                
                JsonObject tags = obj.getAsJsonObject("tags");
                if (!tags.has("name")) continue; // Skip if no name
                
                String name = tags.get("name").getAsString();
                
                double lat;
                double lon;
                if (obj.has("lat") && obj.has("lon")) {
                    lat = obj.get("lat").getAsDouble();
                    lon = obj.get("lon").getAsDouble();
                } else if (obj.has("center")) {
                    JsonObject center = obj.getAsJsonObject("center");
                    lat = center.get("lat").getAsDouble();
                    lon = center.get("lon").getAsDouble();
                } else {
                    continue; // Skip if no valid location
                }
                
                // Get address if available
                String address = "";
                if (tags.has("addr:street")) {
                    address = tags.get("addr:street").getAsString();
                    if (tags.has("addr:housenumber")) {
                        address = tags.get("addr:housenumber").getAsString() + " " + address;
                    }
                } else {
                    address = "Location Details Unavailable";
                }

                // Calculate distance
                double distanceMiles = calculateDistanceInMiles(userLat, userLng, lat, lon);
                
                // Mock data for UI
                String[] specialties = possibleSpecialties[random.nextInt(possibleSpecialties.length)];
                int waitTime = 5 + random.nextInt(40); // 5 to 45 mins
                double rating = 4.0 + (random.nextDouble() * 0.9); // 4.0 to 4.9

                parsedList.add(new Hospital(name, lat, lon, address, distanceMiles, specialties, waitTime, rating));
            }

            // Sort by distance
            Collections.sort(parsedList, new Comparator<Hospital>() {
                @Override
                public int compare(Hospital h1, Hospital h2) {
                    return Double.compare(h1.getDistanceMiles(), h2.getDistanceMiles());
                }
            });

            // Take top 5
            if (parsedList.size() > 5) {
                parsedList = parsedList.subList(0, 5);
            }

            final List<Hospital> finalList = parsedList;
            
            runOnUiThread(() -> {
                adapter.updateData(finalList);
                addHospitalMarkers(finalList);
                llSearchingNear.setVisibility(View.GONE); // Hide the loading overlay when done
                
                if (finalList.isEmpty()) {
                    Toast.makeText(FindCareActivity.this, "No hospitals found in this area.", Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            runOnUiThread(() -> {
                llSearchingNear.setVisibility(View.GONE);
                Toast.makeText(FindCareActivity.this, "Error fetching hospital data.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void addHospitalMarkers(List<Hospital> hospitals) {
        for (Hospital h : hospitals) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(h.getLatitude(), h.getLongitude()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(h.getName());
            marker.setSnippet(String.format(Locale.US, "%.1f miles away", h.getDistanceMiles()));
            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private double calculateDistanceInMiles(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        // results[0] is in meters
        return results[0] * 0.000621371; // convert to miles
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
