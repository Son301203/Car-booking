package com.example.bookcar.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

    private TextView tvDepartureDate, tvReturnDate;
    private EditText etPickup, etDestination;
    private Switch switchRoundTrip;
    private Button btnBook;
    private Calendar calendar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        switchRoundTrip = findViewById(R.id.switch_round_trip);
        btnBook = findViewById(R.id.btn_book);
        calendar = Calendar.getInstance();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


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

        TabUtils.setupTabs(this);
    }

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

        // Check existing bookings
        db.collection("users").document(userId)
                .collection("orders")
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
            Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order details
        Map<String, Object> order = new HashMap<>();
        order.put("pickup", pickup);
        order.put("destination", destination);
        order.put("departureDate", departureDate);
        order.put("returnDate", returnDate);
        order.put("state", "Booked");
        order.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(userId)
                .collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(HomeActivity.this, "Booking Successful!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "Failed to book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        etPickup.setText("");
        etDestination.setText("");
        tvDepartureDate.setText("");
        tvReturnDate.setText("");

    }
}
