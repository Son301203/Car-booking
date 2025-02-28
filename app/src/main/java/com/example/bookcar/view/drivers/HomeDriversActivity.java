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
import com.example.bookcar.view.bottomtab.TabUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeDriversActivity extends AppCompatActivity {
    private ActivityHomeDriversBinding binding;
    private TripAdapter tripAdapter;
    private ArrayList<Trips> tripsArrayList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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
        tripAdapter = new TripAdapter(this, R.layout.layout_listview_trips_drivers, tripsArrayList);
        binding.tripsListView.setAdapter(tripAdapter);


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

        TabUtils.setupTabDriverUI(this);
    }

    private void fetchTrips() {
        String currentDriverId = mAuth.getCurrentUser().getUid();

        CollectionReference tripsRef = db.collection("drivers").document(currentDriverId).collection("trips");
        tripsRef.get().addOnCompleteListener(tripTask -> {
            if (tripTask.isSuccessful() && tripTask.getResult() != null) {
                tripsArrayList.clear();
                for (QueryDocumentSnapshot tripDoc : tripTask.getResult()) {
                    String tripId = tripDoc.getId();
                    String dateTrip = tripDoc.getString("dateTrip");
                    String startTime = tripDoc.getString("startTime");

                    countClients(currentDriverId, tripId, clientCount -> {
                        Trips trip = new Trips(dateTrip, startTime, clientCount, currentDriverId, tripId);
                        tripsArrayList.add(trip);
                        tripAdapter.notifyDataSetChanged();
                    });
                }
            } else {
                Log.e("Firestore", "Error getting trips", tripTask.getException());
                Toast.makeText(this, "Lỗi khi tải danh sách chuyến đi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void countClients(String driverId, String tripId, ClientCountCallback callback) {
        db.collection("drivers")
                .document(driverId)
                .collection("trips")
                .document(tripId)
                .collection("clients")
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