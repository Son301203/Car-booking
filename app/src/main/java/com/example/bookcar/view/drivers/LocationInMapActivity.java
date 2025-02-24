package com.example.bookcar.view.drivers;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class LocationInMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityLocationInMapBinding binding;
    private GoogleMap mMap;
    private double latitude, longitude;

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

        // Initialize the map
        initializeMap();

        // Back button
        binding.toolbarbtnBack.setOnClickListener(v -> finish());
    }

    private void initializeMap() {
        Intent intent = getIntent();
        if(intent != null){
            latitude = intent.getDoubleExtra("latitude", 0.0);
            longitude = intent.getDoubleExtra("longitude", 0.0);

            System.out.println("latitude: " + latitude);
            System.out.println("longitude: " + longitude);
        }
        if(latitude == 0.0 && longitude == 0.0) {
            Log.e(TAG, "Invalid coordinates");
        }
        else{
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragmentDriver);
            if(mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set marker
        LatLng destination = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(destination).title("Client Destination"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15));
    }
}
