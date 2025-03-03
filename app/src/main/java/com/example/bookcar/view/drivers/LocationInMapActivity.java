package com.example.bookcar.view.drivers;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LocationInMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityLocationInMapBinding binding;
    private GoogleMap mMap;
    private double driverLat, driverLng, pickupLat, pickupLng, destLat, destLng;
    private String driverId, tripId, clientId;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker driverMarker, pickupMarker, destinationMarker;
    private boolean isDriverLocationReady = false;
    private boolean isPickupLocationReady = false;

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
                            onSuccess.run(); // Gọi callback khi dữ liệu sẵn sàng
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
                        // Lấy tọa độ điểm đón ngay sau khi có vị trí tài xế
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

    private void updateDriverLocationOnMap() {
        if (mMap != null && isDriverLocationReady) {
            LatLng driverLocation = new LatLng(driverLat, driverLng);
            if (driverMarker != null) driverMarker.remove();
            driverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title("Driver Location"));
            Log.d(TAG, "Marker added for driver at: " + driverLat + ", " + driverLng);
        }
    }

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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 10));
        }
    }

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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destLocation, 10));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getDriverCurrentLocation(); // Lấy vị trí tài xế và điểm đón
    }

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