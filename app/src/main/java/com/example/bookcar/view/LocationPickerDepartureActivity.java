package com.example.bookcar.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.databinding.ActivityLocationPickerDepartureBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.Locale;

public class LocationPickerDepartureActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityLocationPickerDepartureBinding binding;
    private static final String TAG = "LocationPicker";
    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap mMap;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // These variables hold the user’s selected location
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private String selectedAddress = "";

    // Reference to the current marker
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationPickerDepartureBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize Places API (ensure you have your API key in strings.xml)
        Places.initialize(this, getString(R.string.google_map_api_key));
        mPlacesClient = Places.createClient(this);

        // Initialize fused location provider
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup AutocompleteSupportFragment for place search
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocompleleFragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.e(TAG, "Autocomplete error: " + status);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLatLng() != null) {
                    selectedLatitude = place.getLatLng().latitude;
                    selectedLongitude = place.getLatLng().longitude;
                    selectedAddress = place.getAddress() != null ? place.getAddress()
                            : "Lat: " + selectedLatitude + ", Lng: " + selectedLongitude;
                    addMarker(place.getLatLng(), place.getName(), selectedAddress);
                }
            }
        });

        // Back button: finish the activity
        binding.toolbarbtnBack.setOnClickListener(v -> finish());

        // GPS button: request location permission and then show current location
        binding.toolbarbtnGps.setOnClickListener(v -> {
            if (isGPSEnabled()) {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                Toast.makeText(this,
                        "Please enable GPS to see your location", Toast.LENGTH_SHORT).show();
            }
        });

        // Select button: return the selected location as a result
        binding.btnSelect.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedLatitude);
            resultIntent.putExtra("longitude", selectedLongitude);
            resultIntent.putExtra("address", selectedAddress);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Request location permission when the map is ready
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        // When the user taps on the map, update the selection without using Geocoder
        mMap.setOnMapClickListener(latLng -> {
            selectedLatitude = latLng.latitude;
            selectedLongitude = latLng.longitude;
            selectedAddress = "Lat: " + selectedLatitude + ", Lng: " + selectedLongitude;
            addMarker(latLng, "Selected Location", selectedAddress);
        });

        // Allow marker dragging to fine-tune the selection
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) { }

            @Override
            public void onMarkerDrag(Marker marker) { }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng newPos = marker.getPosition();
                selectedLatitude = newPos.latitude;
                selectedLongitude = newPos.longitude;
                selectedAddress = "Lat: " + selectedLatitude + ", Lng: " + selectedLongitude;
                marker.setSnippet(selectedAddress);
                binding.selectPlace.setText(selectedAddress);
            }
        });
    }

    /**
     * Adds a draggable marker to the map at the specified location.
     */
    private void addMarker(LatLng latLng, String title, String snippet) {
        if (mMap == null) return;
        // Remove existing marker if present
        if (currentMarker != null) {
            currentMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .draggable(true);
        currentMarker = mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

        // Update UI elements as needed
        binding.doneLl.setVisibility(View.VISIBLE);
        binding.selectPlace.setText(snippet);
    }

    /**
     * Checks if GPS or Network provider is enabled.
     */
    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "GPS check error", e);
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "Network check error", e);
        }
        return gpsEnabled || networkEnabled;
    }

    /**
     * Requests location permission and then detects the device's last known location.
     */
    private ActivityResultLauncher<String> requestLocationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        try {
                            mMap.setMyLocationEnabled(true);
                            detectDeviceLocation();
                        } catch (SecurityException e) {
                            Toast.makeText(LocationPickerDepartureActivity.this,
                                    "Permission error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LocationPickerDepartureActivity.this,
                                "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    /**
     * Detects the device's last known location and adds a marker there.
     */
    @SuppressLint("MissingPermission")
    private void detectDeviceLocation() {
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        selectedLatitude = location.getLatitude();
                        selectedLongitude = location.getLongitude();
                        LatLng latLng = new LatLng(selectedLatitude, selectedLongitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                        selectedAddress = "Lat: " + selectedLatitude + ", Lng: " + selectedLongitude;
                        addMarker(latLng, "Current Location", selectedAddress);
                    } else {
                        Log.d(TAG, "detectDeviceLocation: Location is null");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "detectDeviceLocation failure", e);
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "detectDeviceLocation: SecurityException", e);
        }
    }
}
