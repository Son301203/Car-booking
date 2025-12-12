package com.example.bookcar.view.drivers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.adapter.TripAdapter;
import com.example.bookcar.contracts.ClientCountCallback;
import com.example.bookcar.databinding.ActivityHomeDriversBinding;
import com.example.bookcar.model.Trips;
import com.example.bookcar.view.animations.FadeIn;
import com.example.bookcar.view.bottomtab.TabUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeDriversActivity extends AppCompatActivity {
    private ActivityHomeDriversBinding binding;
    private TripAdapter tripAdapter;
    private ArrayList<Trips> tripsArrayList;
    private ArrayList<Trips> upcomingTrips;
    private ArrayList<Trips> completedTrips;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentTab = "upcoming"; // "upcoming" or "completed"
    private boolean isFetching = false; // Flag to prevent concurrent fetches

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeDriversBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tripsArrayList = new ArrayList<>();
        upcomingTrips = new ArrayList<>();
        completedTrips = new ArrayList<>();
        tripAdapter = new TripAdapter(this, R.layout.layout_listview_trips_drivers, tripsArrayList);
        binding.tripsListView.setAdapter(tripAdapter);
        FadeIn fadeIn = new FadeIn(this);
        fadeIn.fadeIn(binding.tripsListView);

        // Setup tabs
        setupTabs();

        fetchTrips();

        binding.tripsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Trips selectedTrip = tripsArrayList.get(i);
                Intent intent = new Intent(HomeDriversActivity.this, TripDetailActivity.class);

                intent.putExtra("driverId", selectedTrip.getDriversId());
                intent.putExtra("tripId", selectedTrip.getTripsId());

                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        TabUtils.setupTabDriverUI(this);
        // Refresh trips list when returning to this activity
        // Don't call setupTabs() here to prevent duplicate tabs
        fetchTrips();
    }

    private void setupTabs() {
        // Clear existing tabs first to prevent duplicates
        binding.tabLayout.removeAllTabs();

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Sắp tới"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Hoàn thành"));

        // Remove existing listener to prevent multiple listeners
        binding.tabLayout.clearOnTabSelectedListeners();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    currentTab = "upcoming";
                    // Only update display if not currently fetching
                    if (!isFetching) {
                        showUpcomingTrips();
                    }
                } else {
                    currentTab = "completed";
                    // Only update display if not currently fetching
                    if (!isFetching) {
                        showCompletedTrips();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void showUpcomingTrips() {
        tripsArrayList.clear();
        tripsArrayList.addAll(upcomingTrips);
        tripAdapter.notifyDataSetChanged();
    }

    private void showCompletedTrips() {
        tripsArrayList.clear();
        tripsArrayList.addAll(completedTrips);
        tripAdapter.notifyDataSetChanged();
    }

    private void fetchTrips() {
        // Prevent concurrent fetch operations
        if (isFetching) {
            Log.d("HomeDriversActivity", "Fetch already in progress, skipping...");
            return;
        }

        isFetching = true;
        String currentDriverId = mAuth.getCurrentUser().getUid();

        CollectionReference tripsRef = db.collection("trips");
        tripsRef.whereEqualTo("driver_id", currentDriverId)
                .get().addOnCompleteListener(tripTask -> {
            if (tripTask.isSuccessful() && tripTask.getResult() != null) {
                upcomingTrips.clear();
                completedTrips.clear();

                int totalTrips = tripTask.getResult().size();
                if (totalTrips == 0) {
                    // No trips found, update UI immediately
                    isFetching = false;
                    if ("upcoming".equals(currentTab)) {
                        showUpcomingTrips();
                    } else {
                        showCompletedTrips();
                    }
                    return;
                }

                final int[] processedCount = {0}; // Counter for processed trips

                for (QueryDocumentSnapshot tripDoc : tripTask.getResult()) {
                    String tripId = tripDoc.getId();
                    String dateTrip = tripDoc.getString("dateTrip");
                    String startTime = tripDoc.getString("startTime");
                    String tripStatus = tripDoc.getString("status");

                    // Default to pending if status is null
                    if (tripStatus == null || tripStatus.isEmpty()) {
                        tripStatus = "pending";
                    }

                    final String finalStatus = tripStatus;
                    countClients(tripId, clientCount -> {
                        Trips trip = new Trips(dateTrip, startTime, clientCount, currentDriverId, tripId);
                        trip.setStatus(finalStatus);

                        if ("completed".equals(finalStatus)) {
                            completedTrips.add(trip);
                        } else {
                            upcomingTrips.add(trip);
                        }

                        // Increment counter
                        processedCount[0]++;

                        // Only update display after all trips are processed
                        if (processedCount[0] == totalTrips) {
                            isFetching = false;
                            if ("upcoming".equals(currentTab)) {
                                showUpcomingTrips();
                            } else {
                                showCompletedTrips();
                            }
                        }
                    });
                }
            } else {
                isFetching = false;
                Log.e("Firestore", "Error getting trips", tripTask.getException());
                Toast.makeText(this, "Lỗi khi tải danh sách chuyến đi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countClients(String tripId, ClientCountCallback callback) {
        // Count orders for this trip
        db.collection("orders")
                .whereEqualTo("trip_id", tripId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int clientCount = task.getResult().size();
                        callback.onClientCountRetrieved(clientCount);
                    } else {
                        Log.e("Firestore", "Error counting clients", task.getException());
                        callback.onClientCountRetrieved(0);
                    }
                });
    }
}