package com.example.bookcar.view.clients;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

public class LocationPickerDepartureActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityLocationPickerDepartureBinding binding;
    private static final String TAG = "LocationPicker";
    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap mMap;
    private PlacesClient mPlacesClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Geocoder geocoder;

    // These variables hold the user’s selected location
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private String selectedAddress = "";

    // Reference to the current marker
    private Marker currentMarker;

    // ExecutorService for background tasks
    private ExecutorService executorService;

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

        // Initialize Geocoder
        geocoder = new Geocoder(this, Locale.getDefault());
        Log.d(TAG, "Geocoder initialized. isPresent: " + Geocoder.isPresent());

        // Initialize executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Test Geocoder with a sample location
        if (Geocoder.isPresent()) {
            Log.d(TAG, "Geocoder is available on this device");
        } else {
            Log.w(TAG, "Geocoder is NOT available on this device!");
            Toast.makeText(this, "Geocoder không khả dụng. Sẽ hiển thị tọa độ.", Toast.LENGTH_LONG).show();
        }

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
            Log.d(TAG, "Select button clicked");
            Log.d(TAG, "Returning - Latitude: " + selectedLatitude);
            Log.d(TAG, "Returning - Longitude: " + selectedLongitude);
            Log.d(TAG, "Returning - Address: " + selectedAddress);

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

        // When the user taps on the map, update the selection and get address
        mMap.setOnMapClickListener(latLng -> {
            selectedLatitude = latLng.latitude;
            selectedLongitude = latLng.longitude;

            // Show loading state
            binding.selectPlace.setText("Đang tải địa chỉ...");

            // Get address from coordinates asynchronously
            getAddressFromLatLngAsync(latLng.latitude, latLng.longitude, address -> {
                selectedAddress = address;
                addMarker(latLng, "Địa điểm đã chọn", selectedAddress);
            });
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

                // Show loading state
                binding.selectPlace.setText("Đang tải địa chỉ...");

                // Get address from new coordinates asynchronously
                getAddressFromLatLngAsync(newPos.latitude, newPos.longitude, address -> {
                    selectedAddress = address;
                    marker.setSnippet(selectedAddress);
                    binding.selectPlace.setText(selectedAddress);
                });
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

                        // Show loading state
                        binding.selectPlace.setText("Đang tải địa chỉ...");

                        // Get address from current location asynchronously
                        getAddressFromLatLngAsync(selectedLatitude, selectedLongitude, address -> {
                            selectedAddress = address;
                            addMarker(latLng, "Vị trí hiện tại", selectedAddress);
                        });
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

    /**
     * Interface for address callback
     */
    private interface AddressCallback {
        void onAddressReceived(String address);
    }

    /**
     * Asynchronously gets address from coordinates, trying multiple methods
     */
    private void getAddressFromLatLngAsync(double latitude, double longitude, AddressCallback callback) {
        executorService.execute(() -> {
            String address = null;

            // Try Android Geocoder first
            try {
                address = getAddressFromLatLng(latitude, longitude);
                // Check if we got coordinates back (fallback) by checking if it contains common address components
                // Real addresses usually contain letters and various characters, not just numbers, commas and dots
                if (address != null) {
                    // Check if address contains letters (a real address should have letters)
                    // and is not just coordinates (numbers with dots/commas and spaces)
                    String testAddress = address.replaceAll("[\\d.,\\s-]", ""); // Remove numbers, dots, commas, spaces, dashes
                    if (!testAddress.isEmpty()) {
                        // We got a real address with actual text, use it
                        Log.d(TAG, "Android Geocoder returned real address: " + address);
                        String finalAddress = address;
                        new Handler(Looper.getMainLooper()).post(() -> callback.onAddressReceived(finalAddress));
                        return;
                    } else {
                        Log.d(TAG, "Android Geocoder returned coordinates format, will try Google API");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Android Geocoder failed: " + e.getMessage());
            }

            // If Android Geocoder failed or returned coordinates, try Google Geocoding API
            Log.d(TAG, "Attempting Google Geocoding API...");
            try {
                address = getAddressFromGoogleGeocodingAPI(latitude, longitude);
                if (address != null && !address.isEmpty()) {
                    Log.d(TAG, "Google Geocoding API success: " + address);
                    String finalAddress = address;
                    new Handler(Looper.getMainLooper()).post(() -> callback.onAddressReceived(finalAddress));
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Google Geocoding API failed: " + e.getMessage(), e);
            }

            // If still null, fallback to coordinates
            if (address == null || address.isEmpty()) {
                address = String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
                Log.w(TAG, "All geocoding methods failed, using coordinates: " + address);
            }

            String finalAddress = address;
            new Handler(Looper.getMainLooper()).post(() -> callback.onAddressReceived(finalAddress));
        });
    }

    /**
     * Gets address using Google Geocoding API
     */
    private String getAddressFromGoogleGeocodingAPI(double latitude, double longitude) {
        try {
            String apiKey = getString(R.string.google_map_api_key);
            String urlString = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + latitude + "," + longitude
                + "&key=" + apiKey
                + "&language=vi"; // Use Vietnamese language for better results

            Log.d(TAG, "Calling Google Geocoding API for: " + latitude + ", " + longitude);

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                String status = jsonResponse.getString("status");

                if ("OK".equals(status)) {
                    JSONArray results = jsonResponse.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject result = results.getJSONObject(0);
                        String formattedAddress = result.getString("formatted_address");
                        Log.d(TAG, "Google API returned address: " + formattedAddress);
                        return formattedAddress;
                    }
                } else {
                    Log.e(TAG, "Google Geocoding API status: " + status);
                    // Log error message if available
                    if (jsonResponse.has("error_message")) {
                        String errorMessage = jsonResponse.getString("error_message");
                        Log.e(TAG, "Google API Error: " + errorMessage);
                    }
                    Log.d(TAG, "Full response: " + response.toString());
                }
            } else {
                Log.w(TAG, "Google Geocoding API response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            Log.e(TAG, "Google Geocoding API IOException: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error calling Google Geocoding API: " + e.getMessage());
        }

        return null;
    }

    /**
     * Converts latitude and longitude to a human-readable address using Geocoder.
     * Returns the address string or coordinates if Geocoder fails.
     */
    private String getAddressFromLatLng(double latitude, double longitude) {
        Log.d(TAG, "getAddressFromLatLng: Starting for " + latitude + ", " + longitude);

        try {
            // Check if Geocoder is available
            if (!Geocoder.isPresent()) {
                Log.w(TAG, "Geocoder is not present on this device");
                return String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
            }

            if (geocoder == null) {
                Log.w(TAG, "Geocoder is null");
                return String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
            }

            // Get addresses from location
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses == null || addresses.isEmpty()) {
                Log.w(TAG, "No addresses found for coordinates");
                return String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
            }

            Address address = addresses.get(0);
            Log.d(TAG, "Address found: " + address.toString());

            // Try to get the full address line first (usually the most complete)
            String addressLine = address.getAddressLine(0);
            if (addressLine != null && !addressLine.isEmpty()) {
                Log.d(TAG, "Using address line: " + addressLine);
                return addressLine;
            }

            // If no address line, build from components
            StringBuilder addressString = new StringBuilder();

            // Add feature name (building, landmark)
            if (address.getFeatureName() != null && !address.getFeatureName().matches("^[0-9.]+$")) {
                addressString.append(address.getFeatureName());
            }

            // Add street address
            if (address.getThoroughfare() != null) {
                if (addressString.length() > 0 && !addressString.toString().equals(address.getThoroughfare())) {
                    addressString.append(", ");
                }
                if (addressString.length() == 0 || !addressString.toString().contains(address.getThoroughfare())) {
                    addressString.append(address.getThoroughfare());
                }
            }

            // Add sub-locality (ward/district)
            if (address.getSubLocality() != null) {
                if (addressString.length() > 0) addressString.append(", ");
                addressString.append(address.getSubLocality());
            }

            // Add locality (city/town)
            if (address.getLocality() != null) {
                if (addressString.length() > 0) addressString.append(", ");
                addressString.append(address.getLocality());
            }

            // Add admin area (province/state)
            if (address.getAdminArea() != null) {
                if (addressString.length() > 0) addressString.append(", ");
                addressString.append(address.getAdminArea());
            }

            // Add country
            if (address.getCountryName() != null) {
                if (addressString.length() > 0) addressString.append(", ");
                addressString.append(address.getCountryName());
            }

            // Return the formatted address if we have something
            if (addressString.length() > 0) {
                String result = addressString.toString();
                Log.d(TAG, "Formatted address: " + result);
                return result;
            }

            Log.w(TAG, "No address components found");

        } catch (IOException e) {
            Log.e(TAG, "Geocoder IOException: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid coordinates: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Error getting address: " + e.getMessage(), e);
        }

        // Fallback to coordinates if everything fails
        String fallback = String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
        Log.d(TAG, "Falling back to coordinates: " + fallback);
        return fallback;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
