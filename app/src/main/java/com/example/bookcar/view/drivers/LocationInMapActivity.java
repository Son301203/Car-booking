package com.example.bookcar.view.drivers;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.databinding.ActivityLocationInMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class LocationInMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityLocationInMapBinding binding;
    private GoogleMap mMap;
    private double latitude, longitude;
    private String driverId, tripId, clientId;
    private FirebaseFirestore db;

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

        Intent intent = getIntent();
        if(intent != null){
            driverId = intent.getStringExtra("driverId");
            tripId = intent.getStringExtra("tripId");
            clientId = intent.getStringExtra("clientId");
        }

        // Initialize the map
        initializeMap();

        binding.btnDestination.setOnClickListener(v -> fetchCoordinates("destinationCoordinates"));

        binding.btnPickUpPoint.setOnClickListener(v -> fetchCoordinates("pickupCoordinates"));

        // Back button
        binding.toolbarbtnBack.setOnClickListener(v -> finish());
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragmentDriver);
            if(mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

    }

    private void fetchCoordinates(String key) {
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
                            latitude = lat;
                            longitude = lng;
                            updateMap();
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
    private void updateMap() {
        if (mMap != null) {
            LatLng destination = new LatLng(latitude, longitude);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(destination).title("Client Destination"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fetchCoordinates("pickupCoordinates");
        updateMap();
    }
}
