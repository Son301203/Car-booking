package com.example.bookcar.view.drivers;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.adapter.TripDetailAdapter;
import com.example.bookcar.databinding.ActivityTripDetailBinding;
import com.example.bookcar.model.Seat;
import com.example.bookcar.view.animations.FadeIn;
import com.example.bookcar.view.bottomtab.TabUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TripDetailActivity extends AppCompatActivity {
    private ActivityTripDetailBinding binding;
    private FirebaseFirestore db;
    private TripDetailAdapter tripDetailAdapter;
    private ArrayList<Seat> tripsDetailList;
    private String driverId, tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDetailBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_trip_detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        tripsDetailList = new ArrayList<>();
        tripDetailAdapter = new TripDetailAdapter(this, R.layout.layout_listview_trip_detail_drivers, tripsDetailList);
        binding.tripDetailListView.setAdapter(tripDetailAdapter);

        FadeIn fadeIn = new FadeIn(this);
        fadeIn.fadeIn(binding.tripDetailListView);

        driverId = getIntent().getStringExtra("driverId");
        tripId = getIntent().getStringExtra("tripId");

        if (driverId != null && tripId != null) {
            fetchClients();
        } else {
            Toast.makeText(this, "Error loading trip details", Toast.LENGTH_SHORT).show();
        }

        // back
        binding.backIcon.setOnClickListener(v -> finish());
    }

    public void fetchClients() {
        tripsDetailList.clear();
        tripDetailAdapter.notifyDataSetChanged();

        // Get all orders for this trip
        db.collection("orders")
                .whereEqualTo("trip_id", tripId)
                .whereIn("state", Arrays.asList("Booked", "Picked Up", "Arranged"))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot orderDoc : task.getResult()) {
                            String clientId = orderDoc.getString("client_id");
                            String orderId = orderDoc.getId();

                            // Fetch user details
                            Task<DocumentSnapshot> userTask = db.collection("users")
                                    .document(clientId)
                                    .get();

                            userTasks.add(userTask);
                        }

                        Tasks.whenAllSuccess(userTasks).addOnCompleteListener(allTasks -> {
                            if (allTasks.isSuccessful()) {
                                int index = 0;
                                for (QueryDocumentSnapshot orderDoc : task.getResult()) {
                                    String clientId = orderDoc.getString("client_id");
                                    String orderId = orderDoc.getId();

                                    DocumentSnapshot userSnapshot = (DocumentSnapshot) allTasks.getResult().get(index);
                                    if (userSnapshot.exists()) {
                                        String customerName = userSnapshot.getString("name");
                                        String phone = userSnapshot.getString("phone");

                                        Seat seat = new Seat(clientId, driverId, tripId);
                                        seat.setUsername(customerName);
                                        seat.setPhone(phone);

                                        tripsDetailList.add(seat);
                                    }
                                    index++;
                                }
                                tripDetailAdapter.notifyDataSetChanged();
                            } else {
                                Log.e(TAG, "Error in user tasks", allTasks.getException());
                            }
                        });
                    } else {
                        Log.e(TAG, "Error getting orders", task.getException());
                    }
                });
    }
}