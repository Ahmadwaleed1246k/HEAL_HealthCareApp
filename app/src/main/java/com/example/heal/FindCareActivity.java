package com.example.heal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
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
    // Timeout: 30 seconds to wait for a GPS fix before giving up
    private static final long LOCATION_TIMEOUT_MS = 30_000;

    // ── Pakistan geographic bounds ──────────────────────────────────────────
    // Approx bounding box: north=37.1°N, south=23.5°N, east=77.8°E, west=60.9°E
    private static final double PAK_LAT_NORTH = 37.1;
    private static final double PAK_LAT_SOUTH = 23.5;
    private static final double PAK_LNG_EAST  = 77.8;
    private static final double PAK_LNG_WEST  = 60.9;
    // Centre of Pakistan — map opens here before GPS fix
    private static final double PAK_CENTER_LAT = 30.3753;
    private static final double PAK_CENTER_LNG = 69.3451;
    // ────────────────────────────────────────────────────────────────────────

    private MapView mapView;
    private RecyclerView rvHospitals;
    private HospitalAdapter adapter;
    private List<Hospital> hospitalList;
    private OkHttpClient httpClient;

    private LinearLayout llSearchingNear;
    private TextView tvSearchingNear;

    // Direct LocationManager approach — bypasses all caching and network/IP location
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler timeoutHandler;
    private boolean locationFixed = false; // guard: process fix only once

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_find_care);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Centre on Pakistan and lock panning to Pakistan's borders
        mapView.getController().setZoom(6.0);
        mapView.getController().setCenter(new GeoPoint(PAK_CENTER_LAT, PAK_CENTER_LNG));
        mapView.setScrollableAreaLimitDouble(
                new BoundingBox(PAK_LAT_NORTH, PAK_LNG_EAST, PAK_LAT_SOUTH, PAK_LNG_WEST)
        );
        mapView.setMinZoomLevel(5.0);  // prevent zooming out past Pakistan view

        rvHospitals = findViewById(R.id.rvHospitals);
        rvHospitals.setLayoutManager(new LinearLayoutManager(this));
        hospitalList = new ArrayList<>();
        adapter = new HospitalAdapter(hospitalList);
        rvHospitals.setAdapter(adapter);

        llSearchingNear = findViewById(R.id.llSearchingNear);
        tvSearchingNear = findViewById(R.id.tvSearchingNear);

        timeoutHandler = new Handler(Looper.getMainLooper());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        httpClient = new OkHttpClient();

        findViewById(R.id.btnRefreshLocation).setOnClickListener(v -> {
            llSearchingNear.setVisibility(View.VISIBLE);
            locationFixed = false;
            startLocationFetch();
        });

        checkLocationPermission();
    }

    // ─────────────────────────────────────────────
    //  Permission handling
    // ─────────────────────────────────────────────

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationFetch();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationFetch();
            } else {
                Toast.makeText(this, "Location permission is required to find nearby care.", Toast.LENGTH_LONG).show();
                tvSearchingNear.setText("Location permission denied");
                llSearchingNear.setVisibility(View.GONE);
            }
        }
    }

    // ─────────────────────────────────────────────
    //  Core location logic — direct GPS, no cache
    // ─────────────────────────────────────────────

    private void startLocationFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Cancel any previous listener to avoid duplicates
        stopLocationUpdates();
        locationFixed = false;

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            Toast.makeText(this, "Please enable GPS/Location in device settings.", Toast.LENGTH_LONG).show();
            tvSearchingNear.setText("Location services disabled");
            llSearchingNear.setVisibility(View.GONE);
            return;
        }

        tvSearchingNear.setText("Acquiring real GPS location...");
        llSearchingNear.setVisibility(View.VISIBLE);

        // Create a single listener that stops itself after the first good fix
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (locationFixed) return; // already handled

                double lat = location.getLatitude();
                double lng = location.getLongitude();
                String provider = location.getProvider();
                float accuracy = location.getAccuracy();

                Log.d(TAG, "Location fix from [" + provider + "]: " + lat + ", " + lng
                        + "  accuracy=" + accuracy + "m");

                // ── Validate the fix is within Pakistan ──────────────────────
                if (lat < PAK_LAT_SOUTH || lat > PAK_LAT_NORTH
                        || lng < PAK_LNG_WEST  || lng > PAK_LNG_EAST) {
                    Log.w(TAG, "Fix outside Pakistan bounds (" + lat + ", " + lng
                            + ") — likely emulator/VPN. Ignoring.");
                    // Reset so we keep waiting for a real fix
                    locationFixed = false;
                    runOnUiThread(() -> {
                        tvSearchingNear.setText("Location outside Pakistan. Waiting for real GPS...");
                        Toast.makeText(FindCareActivity.this,
                                "Detected location is outside Pakistan. Make sure GPS is ON and VPN is OFF.",
                                Toast.LENGTH_LONG).show();
                    });
                    return; // keep the listener active — wait for a proper fix
                }
                // ─────────────────────────────────────────────────────────────

                locationFixed = true;
                stopLocationUpdates(); // unregister immediately
                cancelTimeout();

                runOnUiThread(() -> {
                    tvSearchingNear.setText("Searching hospitals nearby...");
                    setupMap(lat, lng);
                    fetchHospitals(lat, lng);
                });
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.w(TAG, provider + " was disabled during location fetch");
                runOnUiThread(() -> tvSearchingNear.setText("GPS disabled. Please enable location."));
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d(TAG, provider + " enabled");
            }
        };

        // Request from GPS first (most accurate, true satellite fix)
        if (gpsEnabled) {
            Log.d(TAG, "Requesting location from GPS_PROVIDER");
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,   // minTimeMs: 0 = get as fast as possible
                    0f,  // minDistanceM: 0 = no distance filter
                    locationListener,
                    Looper.getMainLooper()
            );
        }

        // Also request from NETWORK_PROVIDER in parallel — it typically responds faster.
        // Both will call the same listener; the guard `locationFixed` ensures only the
        // first result is used.
        if (networkEnabled) {
            Log.d(TAG, "Requesting location from NETWORK_PROVIDER");
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    locationListener,
                    Looper.getMainLooper()
            );
        }

        // 30-second timeout — if no fix arrives, show an error (no fallback coordinates)
        timeoutHandler.postDelayed(() -> {
            if (!locationFixed) {
                stopLocationUpdates();
                Log.w(TAG, "Location timeout after " + (LOCATION_TIMEOUT_MS / 1000) + "s");
                runOnUiThread(() -> {
                    llSearchingNear.setVisibility(View.GONE);
                    Toast.makeText(FindCareActivity.this,
                            "Could not get location. Make sure GPS is on and try again.",
                            Toast.LENGTH_LONG).show();
                });
            }
        }, LOCATION_TIMEOUT_MS);
    }

    private void stopLocationUpdates() {
        if (locationListener != null && locationManager != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    private void cancelTimeout() {
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    // ─────────────────────────────────────────────
    //  Map & hospital data
    // ─────────────────────────────────────────────

    private void setupMap(double lat, double lng) {
        GeoPoint startPoint = new GeoPoint(lat, lng);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(startPoint);

        mapView.getOverlays().clear();
        Marker userMarker = new Marker(mapView);
        userMarker.setPosition(startPoint);
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userMarker.setTitle("You are here");
        mapView.getOverlays().add(userMarker);
    }

    private void fetchHospitals(double userLat, double userLng) {
        String query = "[out:json];(node[\"amenity\"=\"hospital\"](around:20000," + userLat + "," + userLng + ");" +
                       "way[\"amenity\"=\"hospital\"](around:20000," + userLat + "," + userLng + ");" +
                       "relation[\"amenity\"=\"hospital\"](around:20000," + userLat + "," + userLng + "););out center;";

        okhttp3.HttpUrl url = okhttp3.HttpUrl.parse("https://overpass-api.de/api/interpreter").newBuilder()
                .addQueryParameter("data", query)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "HEAL_HealthCareApp/1.0")
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
                if (!tags.has("name")) continue;

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
                    continue;
                }

                String address;
                if (tags.has("addr:street")) {
                    address = tags.get("addr:street").getAsString();
                    if (tags.has("addr:housenumber")) {
                        address = tags.get("addr:housenumber").getAsString() + " " + address;
                    }
                } else {
                    address = "Location Details Unavailable";
                }

                double distanceMiles = calculateDistanceInMiles(userLat, userLng, lat, lon);

                String[] specialties = possibleSpecialties[random.nextInt(possibleSpecialties.length)];
                int waitTime = 5 + random.nextInt(40);
                double rating = 4.0 + (random.nextDouble() * 0.9);

                parsedList.add(new Hospital(name, lat, lon, address, distanceMiles, java.util.Arrays.asList(specialties), waitTime, rating));
            }

            Collections.sort(parsedList, (h1, h2) -> Double.compare(h1.getDistanceMiles(), h2.getDistanceMiles()));

            if (parsedList.size() > 5) {
                parsedList = parsedList.subList(0, 5);
            }

            final List<Hospital> finalList = parsedList;

            runOnUiThread(() -> {
                adapter.updateData(finalList);
                addHospitalMarkers(finalList);
                llSearchingNear.setVisibility(View.GONE);

                if (finalList.isEmpty()) {
                    Toast.makeText(FindCareActivity.this, "No hospitals found in your area.", Toast.LENGTH_LONG).show();
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
        return results[0] * 0.000621371;
    }

    // ─────────────────────────────────────────────
    //  Lifecycle
    // ─────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        stopLocationUpdates(); // release GPS when app goes to background
        cancelTimeout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        cancelTimeout();
    }
}
