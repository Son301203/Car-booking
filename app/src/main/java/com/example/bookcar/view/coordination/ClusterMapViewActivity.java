package com.example.bookcar.view.coordination;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookcar.R;
import com.example.bookcar.model.ClusterCustomerInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class ClusterMapViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "ClusterMapViewActivity";

    public static final String EXTRA_CLUSTER_INDEX = "cluster_index";
    public static final String EXTRA_CLUSTER_ORDERS = "cluster_orders";
    public static final String EXTRA_CUSTOMER_COUNT = "customer_count";
    public static final String EXTRA_DEPARTURE_TIME = "departure_time";

    private GoogleMap mMap;
    private TextView tvClusterTitle;
    private TextView tvClusterInfo;
    private TextView tvCustomerCount;
    private TextView tvDepartureTime;
    private ImageButton btnBack;
    private Button btnAssignDriver;

    private int clusterIndex;
    private ArrayList<ClusterCustomerInfo> clusterCustomers;
    private int customerCount;
    private String departureTime;

    private final List<Marker> markers = new ArrayList<>();
    private Circle clusterCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_map_view);

        // Initialize views
        tvClusterTitle = findViewById(R.id.tvClusterTitle);
        tvClusterInfo = findViewById(R.id.tvClusterInfo);
        tvCustomerCount = findViewById(R.id.tvCustomerCount);
        tvDepartureTime = findViewById(R.id.tvDepartureTime);
        btnBack = findViewById(R.id.btnBack);
        btnAssignDriver = findViewById(R.id.btnAssignDriver);

        // Get data from intent
        clusterIndex = getIntent().getIntExtra(EXTRA_CLUSTER_INDEX, 0);
        @SuppressWarnings("unchecked")
        ArrayList<ClusterCustomerInfo> customers = (ArrayList<ClusterCustomerInfo>) getIntent().getSerializableExtra(EXTRA_CLUSTER_ORDERS);
        clusterCustomers = customers;
        customerCount = getIntent().getIntExtra(EXTRA_CUSTOMER_COUNT, 0);
        departureTime = getIntent().getStringExtra(EXTRA_DEPARTURE_TIME);

        // Setup UI
        tvClusterTitle.setText("Cụm " + (clusterIndex + 1));
        tvClusterInfo.setText(customerCount + " khách hàng");
        tvCustomerCount.setText("👥 Số khách: " + customerCount);
        tvDepartureTime.setText("🕐 Giờ đi: " + (departureTime != null ? departureTime : "Chưa xác định"));

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup buttons
        btnBack.setOnClickListener(v -> finish());
        btnAssignDriver.setOnClickListener(v -> {
            // TODO: Implement driver assignment
            Toast.makeText(this, "Chức năng chọn tài xế đang được phát triển", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);

        if (clusterCustomers != null && !clusterCustomers.isEmpty()) {
            displayClusterOnMap();
        }
    }

    private void displayClusterOnMap() {
        List<LatLng> pickupLocations = new ArrayList<>();

        // Add markers for each customer's pickup location
        for (int i = 0; i < clusterCustomers.size(); i++) {
            ClusterCustomerInfo customer = clusterCustomers.get(i);

            if (customer.getLatitude() != 0 && customer.getLongitude() != 0) {
                LatLng pickupLatLng = new LatLng(customer.getLatitude(), customer.getLongitude());
                pickupLocations.add(pickupLatLng);

                // Create marker
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(pickupLatLng)
                        .title(customer.getCustomerName())
                        .snippet("📍 " + customer.getPickupAddress() + "\n📞 " + customer.getCustomerPhone())
                        .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(clusterIndex)));

                Marker marker = mMap.addMarker(markerOptions);
                markers.add(marker);
            }
        }

        if (!pickupLocations.isEmpty()) {
            // Calculate center point and radius for the circle
            LatLng centerPoint = calculateCenterPoint(pickupLocations);
            double radius = calculateMaxDistance(centerPoint, pickupLocations);

            // Draw circle around the cluster
            drawClusterCircle(centerPoint, radius);

            // Auto zoom to cluster center immediately (like auto zoom to user location)
            // Calculate appropriate zoom level based on cluster radius
            float zoomLevel = calculateZoomLevel(radius);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, zoomLevel));
        }
    }

    private LatLng calculateCenterPoint(List<LatLng> locations) {
        if (locations.isEmpty()) {
            return new LatLng(0, 0);
        }

        double sumLat = 0;
        double sumLng = 0;

        for (LatLng location : locations) {
            sumLat += location.latitude;
            sumLng += location.longitude;
        }

        return new LatLng(sumLat / locations.size(), sumLng / locations.size());
    }

    private double calculateMaxDistance(LatLng center, List<LatLng> locations) {
        double maxDistance = 0;

        for (LatLng location : locations) {
            double distance = calculateDistance(center, location);
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }

        // Add 20% padding to the radius
        return maxDistance * 1.2;
    }

    private double calculateDistance(LatLng point1, LatLng point2) {
        // Haversine formula for distance calculation
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(point2.latitude - point1.latitude);
        double dLng = Math.toRadians(point2.longitude - point1.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(point1.latitude)) * Math.cos(Math.toRadians(point2.latitude)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    /**
     * Calculate appropriate zoom level based on cluster radius
     * Similar to auto-zoom to user location
     */
    private float calculateZoomLevel(double radius) {
        // radius is in meters
        // Zoom levels: 1 = World, 5 = Landmass/continent, 10 = City, 15 = Streets, 20 = Buildings
        if (radius < 500) {
            return 15.5f;  // Very close zoom for small clusters (< 500m)
        } else if (radius < 1000) {
            return 14.5f;  // Close zoom for medium clusters (< 1km)
        } else if (radius < 2000) {
            return 13.5f;  // Medium zoom for larger clusters (< 2km)
        } else if (radius < 5000) {
            return 12.5f;  // Wide zoom for large clusters (< 5km)
        } else {
            return 11.0f;  // Very wide zoom for very large clusters (> 5km)
        }
    }

    private void drawClusterCircle(LatLng center, double radius) {
        // Remove old circle if exists
        if (clusterCircle != null) {
            clusterCircle.remove();
        }

        // Create circle options
        CircleOptions circleOptions = new CircleOptions()
                .center(center)
                .radius(radius)
                .strokeColor(getCircleStrokeColor(clusterIndex))
                .strokeWidth(4)
                .fillColor(getCircleFillColor(clusterIndex));

        clusterCircle = mMap.addCircle(circleOptions);
    }

    private float getMarkerColor(int clusterIndex) {
        // Different colors for different clusters
        float[] colors = {
                BitmapDescriptorFactory.HUE_RED,
                BitmapDescriptorFactory.HUE_BLUE,
                BitmapDescriptorFactory.HUE_GREEN,
                BitmapDescriptorFactory.HUE_YELLOW,
                BitmapDescriptorFactory.HUE_ORANGE,
                BitmapDescriptorFactory.HUE_VIOLET,
                BitmapDescriptorFactory.HUE_CYAN,
                BitmapDescriptorFactory.HUE_MAGENTA
        };
        return colors[clusterIndex % colors.length];
    }

    private int getCircleStrokeColor(int clusterIndex) {
        // Different stroke colors for different clusters
        int[] colors = {
                Color.parseColor("#D32F2F"), // Red
                Color.parseColor("#1976D2"), // Blue
                Color.parseColor("#388E3C"), // Green
                Color.parseColor("#F57C00"), // Orange
                Color.parseColor("#7B1FA2"), // Purple
                Color.parseColor("#0097A7"), // Cyan
                Color.parseColor("#C2185B"), // Pink
                Color.parseColor("#5D4037")  // Brown
        };
        return colors[clusterIndex % colors.length];
    }

    private int getCircleFillColor(int clusterIndex) {
        // Semi-transparent fill colors
        int[] colors = {
                Color.parseColor("#33D32F2F"), // Red with alpha
                Color.parseColor("#331976D2"), // Blue with alpha
                Color.parseColor("#33388E3C"), // Green with alpha
                Color.parseColor("#33F57C00"), // Orange with alpha
                Color.parseColor("#337B1FA2"), // Purple with alpha
                Color.parseColor("#330097A7"), // Cyan with alpha
                Color.parseColor("#33C2185B"), // Pink with alpha
                Color.parseColor("#335D4037")  // Brown with alpha
        };
        return colors[clusterIndex % colors.length];
    }

}

