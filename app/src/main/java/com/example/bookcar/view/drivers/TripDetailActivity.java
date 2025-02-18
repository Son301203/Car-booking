package com.example.bookcar.view.drivers;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.adapter.TripDetailAdapter;
import com.example.bookcar.databinding.ActivityTripDetailBinding;
import com.example.bookcar.model.Trips;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TripDetailActivity extends AppCompatActivity {
    private ActivityTripDetailBinding binding;
    private FirebaseFirestore db;
    private TripDetailAdapter tripDetailAdapter;
    private ArrayList<Trips> tripsDetailList;
    private String driverId, tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDetailBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        tripsDetailList = new ArrayList<>();
        tripDetailAdapter = new TripDetailAdapter(this, R.layout.layout_listview_trip_detail_drivers, tripsDetailList);
        binding.tripDetailListView.setAdapter(tripDetailAdapter);

        // Retrieve driverId and tripId from Intent
        driverId = getIntent().getStringExtra("driverId");
        tripId = getIntent().getStringExtra("tripId");

        if (driverId != null && tripId != null) {
            fetchClients();
        } else {
            Toast.makeText(this, "Error loading trip details", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchClients() {
        db.collection("drivers")
                .document(driverId)
                .collection("trips")
                .document(tripId)
                .collection("clients")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        for(QueryDocumentSnapshot clientDoc : task.getResult()) {
                            String clientName = clientDoc.getString("customerName");
                            String clientPhone = clientDoc.getString("phone");

                            Trips clientTrip = new Trips(clientName, clientPhone);
                            tripsDetailList.add(clientTrip);
                        }
                        tripDetailAdapter.notifyDataSetChanged();
                    }
                    else{
                        Log.e(TAG, "Error getting clients", task.getException());
                    }
                });
    }
}
