package com.example.bookcar.view.drivers;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.databinding.ActivityLocationInMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationInMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityLocationInMapBinding binding;
    private GoogleMap mMap;
    private double driverLat, driverLng, pickupLat, pickupLng, destLat, destLng;
    private String driverId, tripId, clientId;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker driverMarker, pickupMarker, destinationMarker, distanceMarker;
    private Polyline routePolyline;
    private boolean isDriverLocationReady = false;
    private boolean isPickupLocationReady = false;
    private static final String ORS_API_KEY = "5b3ce3597851110001cf62483f6622ce9cec4a6abce66b5e5f939366";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationInMapBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        if (intent != null) {
            driverId = intent.getStringExtra("driverId");
            tripId = intent.getStringExtra("tripId");
            clientId = intent.getStringExtra("clientId");
        }

        initializeMap();

        binding.zoomInButton.setOnClickListener(v -> zoomIn());
        binding.zoomOutButton.setOnClickListener(v -> zoomOut());

        binding.btnDestination.setOnClickListener(v -> fetchCoordinates("destinationCoordinates", () -> updateMapWithDestination()));
        binding.btnPickUpPoint.setOnClickListener(v -> fetchCoordinates("pickupCoordinates", () -> updateMapWithPickup()));
        binding.toolbarbtnBack.setOnClickListener(v -> finish());
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragmentDriver);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // fetch clients's Coordinates
    private void fetchCoordinates(String key, Runnable onSuccess) {
        if (driverId != null && tripId != null && clientId != null) {
            DocumentReference locationRef = db.collection("drivers")
                    .document(driverId)
                    .collection("trips")
                    .document(tripId)
                    .collection("clients")
                    .document(clientId);

            locationRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains(key)) {
                    Object obj = documentSnapshot.get(key);
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> coordinates = (Map<String, Object>) obj;
                        Double lat = coordinates.get("latitude") instanceof Number ?
                                ((Number) coordinates.get("latitude")).doubleValue() : null;
                        Double lng = coordinates.get("longitude") instanceof Number ?
                                ((Number) coordinates.get("longitude")).doubleValue() : null;

                        if (lat != null && lng != null) {
                            if (key.equals("pickupCoordinates")) {
                                pickupLat = lat;
                                pickupLng = lng;
                                isPickupLocationReady = true;
                            } else if (key.equals("destinationCoordinates")) {
                                destLat = lat;
                                destLng = lng;
                            }
                            onSuccess.run();
                        } else {
                            Toast.makeText(this, "Invalid coordinate data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Coordinates format error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No coordinates found", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // get Driver Current Location by GPS
    private void getDriverCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        driverLat = location.getLatitude();
                        driverLng = location.getLongitude();
                        isDriverLocationReady = true;
                        Log.d(TAG, "Driver Location: " + driverLat + ", " + driverLng);
                        fetchCoordinates("pickupCoordinates", () -> {
                            if (isDriverLocationReady && isPickupLocationReady) {
                                updateMapWithPickup();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Location is null");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting location", e);
                });
    }

    // driver's Marker
    private BitmapDescriptor createDriverMarkerIcon() {
        int size = 30;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setAntiAlias(true);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint);

        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.parseColor("#4285F4"));
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, fillPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // Distance Marker ICON
    private BitmapDescriptor createDistanceMarkerIcon(String distanceText) {
        int width = 80;
        int height = 40;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        //  rectangle info
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#2196F3"));
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAntiAlias(true);
        float radius = 5f; // Giảm radius bo góc để giống Google Maps
        canvas.drawRoundRect(0, 0, width, height - 10, radius, radius, bgPaint);

        Paint trianglePaint = new Paint();
        trianglePaint.setColor(Color.parseColor("#2196F3"));
        trianglePaint.setStyle(Paint.Style.FILL);
        trianglePaint.setAntiAlias(true);
        Path trianglePath = new Path();
        trianglePath.moveTo(width / 2 - 5, height - 10);
        trianglePath.lineTo(width / 2 + 5, height - 10);
        trianglePath.lineTo(width / 2, height);
        trianglePath.close();
        canvas.drawPath(trianglePath, trianglePaint);

        // distance text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(14);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float textX = width / 2 + 5;
        float textY = height / 2 - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(distanceText, textX, textY, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void updateDriverLocationOnMap() {
        if (mMap != null && isDriverLocationReady) {
            LatLng driverLocation = new LatLng(driverLat, driverLng);
            if (driverMarker != null) driverMarker.remove();
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(driverLocation)
                    .title("Driver Location")
                    .icon(createDriverMarkerIcon()));
            Log.d(TAG, "Marker added for driver at: " + driverLat + ", " + driverLng);
        }
    }

    // API OpenRouteService
    private void fetchRouteFromORS(LatLng start, LatLng end, Runnable onSuccess) {
        new Thread(() -> {
            try {
                String urlString = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", ORS_API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/geo+json");
                conn.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                JSONArray coordinates = new JSONArray();
                coordinates.put(new JSONArray().put(start.longitude).put(start.latitude));
                coordinates.put(new JSONArray().put(end.longitude).put(end.latitude));
                jsonBody.put("coordinates", coordinates);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray routeCoordinates = jsonResponse.getJSONArray("features")
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONArray("coordinates");
                    double distance = jsonResponse.getJSONArray("features")
                            .getJSONObject(0)
                            .getJSONObject("properties")
                            .getJSONArray("segments")
                            .getJSONObject(0)
                            .getDouble("distance") / 1000; // Chuyển sang km

                    ArrayList<LatLng> routePoints = new ArrayList<>();
                    for (int i = 0; i < routeCoordinates.length(); i++) {
                        JSONArray point = routeCoordinates.getJSONArray(i);
                        double lng = point.getDouble(0);
                        double lat = point.getDouble(1);
                        routePoints.add(new LatLng(lat, lng));
                    }

                    runOnUiThread(() -> {
                        if (routePolyline != null) routePolyline.remove();
                        routePolyline = mMap.addPolyline(new PolylineOptions()
                                .addAll(routePoints)
                                .width(5)
                                .color(0xFF0000FF));

                        // Tính điểm giữa lộ trình
                        LatLng midPoint = routePoints.get(routePoints.size() / 2);
                        if (distanceMarker != null) distanceMarker.remove();
                        distanceMarker = mMap.addMarker(new MarkerOptions()
                                .position(midPoint)
                                .icon(createDistanceMarkerIcon(String.format("%.2f km", distance))));

                        onSuccess.run();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi từ ORS API", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Lỗi khi gọi ORS API: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e(TAG, "ORS API Error", e);
            }
        }).start();
    }

    //update Map With Pickup
    private void updateMapWithPickup() {
        if (mMap != null && isDriverLocationReady && isPickupLocationReady) {
            if (destinationMarker != null) {
                destinationMarker.remove();
                destinationMarker = null;
            }
            updateDriverLocationOnMap();
            LatLng pickupLocation = new LatLng(pickupLat, pickupLng);
            if (pickupMarker != null) pickupMarker.remove();
            pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Point"));

            LatLng driverLocation = new LatLng(driverLat, driverLng);
            fetchRouteFromORS(driverLocation, pickupLocation, () -> {
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boundsBuilder.include(driverLocation);
                boundsBuilder.include(pickupLocation);
                LatLngBounds bounds = boundsBuilder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null);
            });
        }
    }

    // update Map With Destination
    private void updateMapWithDestination() {
        if (mMap != null && isDriverLocationReady) {
            if (pickupMarker != null) {
                pickupMarker.remove();
                pickupMarker = null;
            }
            updateDriverLocationOnMap();
            LatLng destLocation = new LatLng(destLat, destLng);
            if (destinationMarker != null) destinationMarker.remove();
            destinationMarker = mMap.addMarker(new MarkerOptions().position(destLocation).title("Client Destination"));

            LatLng driverLocation = new LatLng(driverLat, driverLng);
            fetchRouteFromORS(driverLocation, destLocation, () -> {
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                boundsBuilder.include(driverLocation);
                boundsBuilder.include(destLocation);
                LatLngBounds bounds = boundsBuilder.build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null);
            });
        }
    }

    // Zoom in map
    private void zoomIn() {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
    }

    // Zoom out map
    private void zoomOut() {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getDriverCurrentLocation();
    }

    //Request Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getDriverCurrentLocation();
        } else {
            Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
        }
    }
}