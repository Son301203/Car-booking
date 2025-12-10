package com.example.bookcar.view.clients;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.view.bottomtab.TabUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private TextView tvDepartureDate, tvReturnDate, tvLocationPickerDeparture, tvLocationPickerDestination;
    private EditText etPickup, etDestination;
    private Switch switchRoundTrip;
    private Button btnBook;
    private Calendar calendar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private double latitude = 0.0;
    private double longitude = 0.0;

    private double departureLatitude = 0.0, departureLongitude = 0.0;
    private double destinationLatitude = 0.0, destinationLongitude = 0.0;
    private String address = "";

    private boolean isPickingDeparture = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etPickup = findViewById(R.id.et_pickup);
        etDestination = findViewById(R.id.et_destination);
        tvDepartureDate = findViewById(R.id.tv_departure_date);
        tvReturnDate = findViewById(R.id.tv_return_date);
        tvLocationPickerDeparture = findViewById(R.id.tv_select_pickup_map);
        tvLocationPickerDestination = findViewById(R.id.tv_select_destination_map);
        switchRoundTrip = findViewById(R.id.switch_round_trip);
        btnBook = findViewById(R.id.btn_book);
        calendar = Calendar.getInstance();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Handle Location picker departure
        tvLocationPickerDeparture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPickingDeparture = true;
                Intent intent = new Intent(HomeActivity.this, LocationPickerDepartureActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });

        tvLocationPickerDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPickingDeparture = false;
                Intent intent = new Intent(HomeActivity.this, LocationPickerDepartureActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });

        // Handle Departure Date Selection
        tvDepartureDate.setOnClickListener(v -> showDatePickerDialog(tvDepartureDate));

        // Handle Return Date Selection
        tvReturnDate.setOnClickListener(v -> showDatePickerDialog(tvReturnDate));

        // Handle Round Trip Toggle
        switchRoundTrip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvReturnDate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Handle Booking
        btnBook.setOnClickListener(v -> checkStateOrder());

        TabUtils.setupTabClientUI(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TabUtils.setupTabClientUI(this);
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if(o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();

                        if(data != null) {
                            latitude = data.getDoubleExtra("latitude", 0.0);
                            longitude = data.getDoubleExtra("longitude", 0.0);
                            address = data.getStringExtra("address");

                            if(isPickingDeparture) {
                                tvLocationPickerDeparture.setText(address);
                                departureLatitude = latitude;
                                departureLongitude = longitude;
                            }else{
                                tvLocationPickerDestination.setText(address);
                                destinationLatitude = latitude;
                                destinationLongitude = longitude;
                            }
                        }
                    }
                    else{
                        Toast.makeText(HomeActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void showDatePickerDialog(TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                HomeActivity.this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    textView.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void checkStateOrder() {
        String userId = mAuth.getCurrentUser().getUid();

        // Check existing bookings from root orders collection
        db.collection("orders")
                .whereEqualTo("client_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean hasActiveOrder = false;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String orderStatus = document.getString("state");
                        if ("Booked".equals(orderStatus) || "Picked Up".equals(orderStatus)) {
                            hasActiveOrder = true;
                            break;
                        }
                    }

                    if (hasActiveOrder) {
                        Toast.makeText(HomeActivity.this, "You already have an booking. Complete it before booking again!", Toast.LENGTH_LONG).show();
                    } else {
                        bookCar(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Failed to check existing bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void bookCar(String userId) {
        String pickup = etPickup.getText().toString();
        String destination = etDestination.getText().toString();
        String departureDate = tvDepartureDate.getText().toString();
        String returnDate = switchRoundTrip.isChecked() ? tvReturnDate.getText().toString() : "";

        if (pickup.isEmpty() || destination.isEmpty() || departureDate.equals("Select Departure Date")) {
            Toast.makeText(this, "Bạn hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> order = new HashMap<>();
        order.put("client_id", userId); // Reference to users/{userId}
        order.put("pickup", pickup);
        order.put("destination", destination);
        order.put("departureDate", departureDate);
        order.put("returnDate", returnDate);
        order.put("state", "Booked");
        order.put("timestamp", System.currentTimeMillis());
        order.put("created_at", com.google.firebase.Timestamp.now());

        // Store coordinates as GeoPoint
        order.put("pickup_coordinates", new com.google.firebase.firestore.GeoPoint(departureLatitude, departureLongitude));
        order.put("destination_coordinates", new com.google.firebase.firestore.GeoPoint(destinationLatitude, destinationLongitude));

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(HomeActivity.this, "Bạn đã đặt xe thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Đặt xe thất bại " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        etPickup.setText("");
        etDestination.setText("");
        tvDepartureDate.setText("Ngày đi");
        tvReturnDate.setText("Ngày về");
        tvLocationPickerDeparture.setText("Chọn điểm đón trên bản đồ");
        tvLocationPickerDestination.setText("Chọn điểm đến trên bản đồ");

    }

}
