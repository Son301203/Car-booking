package com.example.bookcar.view.coordination;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookcar.R;
import com.example.bookcar.adapter.BookedCustomerAdapter;
import com.example.bookcar.model.Driver;
import com.example.bookcar.model.Order;
import com.example.bookcar.model.Trips;
import com.example.bookcar.utils.NotificationHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class ArrangeCustomersFragment extends Fragment {
    private static final String TAG = "ArrangeCustomersFragment";

    private RecyclerView recyclerViewCustomers;
    private ProgressBar progressBarCustomers;
    private TextView tvNoCustomers;
    private Button btnArrangeDriver;

    private BookedCustomerAdapter adapter;
    private List<Order> bookedOrders;
    private FirebaseFirestore db;
    // Track which order documentIds have been added to avoid duplicates
    private Set<String> loadedOrderIds = new HashSet<>();

    public static ArrangeCustomersFragment newInstance() {
        return new ArrangeCustomersFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arrange_customers, container, false);

        // Initialize views
        recyclerViewCustomers = view.findViewById(R.id.recyclerViewCustomers);
        progressBarCustomers = view.findViewById(R.id.progressBarCustomers);
        tvNoCustomers = view.findViewById(R.id.tvNoCustomers);
        btnArrangeDriver = view.findViewById(R.id.btnArrangeDriver);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        bookedOrders = new ArrayList<>();
        adapter = new BookedCustomerAdapter(getContext(), bookedOrders);
        recyclerViewCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCustomers.setAdapter(adapter);

        // Load booked customers
        loadBookedCustomers();

        // Setup button click
        btnArrangeDriver.setOnClickListener(v -> showDriverSelectionDialog());

        return view;
    }

    private void loadBookedCustomers() {
        progressBarCustomers.setVisibility(View.VISIBLE);
        tvNoCustomers.setVisibility(View.GONE);
        // Clear previous data and tracking set to avoid duplicates
        bookedOrders.clear();
        loadedOrderIds.clear();
        adapter.notifyDataSetChanged();

        db.collection("orders")
                .whereEqualTo("state", "Booked")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBarCustomers.setVisibility(View.GONE);
                    // bookedOrders.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoCustomers.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = Order.fromFirestore(document);
                        // Fetch customer info
                        fetchCustomerInfo(order);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBarCustomers.setVisibility(View.GONE);
                    tvNoCustomers.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Lỗi khi tải danh sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchCustomerInfo(Order order) {
        // Guard against null and duplicate additions
        String orderId = order.getDocumentId();
        if (orderId == null) {
            // If no id, still try to add but avoid duplicates by object reference
            if (!bookedOrders.contains(order)) {
                bookedOrders.add(order);
                adapter.notifyDataSetChanged();
            }
            return;
        }

        if (loadedOrderIds.contains(orderId)) {
            // already added by a previous async callback
            return;
        }

        if (order.getClientId() == null) {
            loadedOrderIds.add(orderId);
            bookedOrders.add(order);
            adapter.notifyDataSetChanged();
            return;
        }

        db.collection("users").document(order.getClientId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        order.setCustomerName(documentSnapshot.getString("name"));
                        order.setCustomerPhone(documentSnapshot.getString("phone"));
                    }
                    // Only add if not already added
                    if (!loadedOrderIds.contains(orderId)) {
                        loadedOrderIds.add(orderId);
                        bookedOrders.add(order);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!loadedOrderIds.contains(orderId)) {
                        loadedOrderIds.add(orderId);
                        bookedOrders.add(order);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showDriverSelectionDialog() {
        List<Order> selectedOrders = adapter.getSelectedOrders();

        if (selectedOrders.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất một khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate same departure date
        String firstDate = selectedOrders.get(0).getDepartureDate();
        for (Order order : selectedOrders) {
            if (!order.getDepartureDate().equals(firstDate)) {
                Toast.makeText(getContext(), "Các khách hàng phải có cùng ngày khởi hành", Toast.LENGTH_LONG).show();
                return;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_driver_for_trip, null);
        builder.setView(dialogView);

        TimePicker timePicker = dialogView.findViewById(R.id.timePickerTripStart);
        RadioGroup radioGroupDrivers = dialogView.findViewById(R.id.radioGroupDrivers);
        ProgressBar progressBarDrivers = dialogView.findViewById(R.id.progressBarDrivers);
        TextView tvNoDrivers = dialogView.findViewById(R.id.tvNoDrivers);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelSelectDriver);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmArrangeDriver);

        // Set 24-hour format
        timePicker.setIs24HourView(true);

        AlertDialog dialog = builder.create();

        // Load drivers
        loadDriversForDialog(radioGroupDrivers, progressBarDrivers, tvNoDrivers);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            int selectedDriverId = radioGroupDrivers.getCheckedRadioButtonId();
            if (selectedDriverId == -1) {
                Toast.makeText(getContext(), "Vui lòng chọn tài xế", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadio = dialogView.findViewById(selectedDriverId);
            String driverId = (String) selectedRadio.getTag();
            String driverName = selectedRadio.getText().toString();

            // Get time
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String tripStartTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            // Arrange driver
            arrangeDriver(driverId, driverName, tripStartTime, selectedOrders);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadDriversForDialog(RadioGroup radioGroup, ProgressBar progressBar, TextView tvNoDrivers) {
        // Get drivers role id first
        db.collection("roles")
                .whereEqualTo("name", "drivers")
                .get()
                .addOnSuccessListener(roleSnapshots -> {
                    if (roleSnapshots.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        tvNoDrivers.setVisibility(View.VISIBLE);
                        return;
                    }

                    String driversRoleId = roleSnapshots.getDocuments().get(0).getId();

                    // Load drivers
                    db.collection("users")
                            .whereEqualTo("role_id", driversRoleId)
                            .get()
                            .addOnSuccessListener(driverSnapshots -> {
                                progressBar.setVisibility(View.GONE);

                                if (driverSnapshots.isEmpty()) {
                                    tvNoDrivers.setVisibility(View.VISIBLE);
                                    return;
                                }

                                radioGroup.setVisibility(View.VISIBLE);

                                for (QueryDocumentSnapshot doc : driverSnapshots) {
                                    Driver driver = Driver.fromFirestore(doc);
                                    RadioButton radioButton = new RadioButton(getContext());
                                    radioButton.setText(driver.getName() + " - " + driver.getPhone());
                                    radioButton.setTag(driver.getDocumentId());
                                    radioButton.setPadding(16, 16, 16, 16);
                                    radioGroup.addView(radioButton);
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                tvNoDrivers.setVisibility(View.VISIBLE);
                                tvNoDrivers.setText("Lỗi: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoDrivers.setVisibility(View.VISIBLE);
                    tvNoDrivers.setText("Lỗi: " + e.getMessage());
                });
    }

    private void arrangeDriver(String driverId, String driverName, String tripStartTime, List<Order> selectedOrders) {
        if (selectedOrders.isEmpty()) return;

        // Use the departure date from first selected order
        String tripDate = selectedOrders.get(0).getDepartureDate();

        // Validate inputs
        if (TextUtils.isEmpty(driverId)) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin tài xế", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(tripStartTime)) {
            Toast.makeText(getContext(), "Vui lòng nhập thời gian khởi hành", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(tripDate)) {
            Toast.makeText(getContext(), "Không tìm thấy ngày khởi hành", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create trip with validation
        Trips trip;
        try {
            trip = new Trips(tripDate, tripStartTime, selectedOrders.size(), driverId, null);

            // Validate trip before saving
            if (!trip.isValid()) {
                Toast.makeText(getContext(), "Dữ liệu chuyến đi không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error creating trip", e);
            return;
        }

        db.collection("trips")
                .add(trip.toFirestore())
                .addOnSuccessListener(tripDocRef -> {
                    String tripId = tripDocRef.getId();

                    // Update each order
                    int totalOrders = selectedOrders.size();
                    final int[] completedCount = {0};

                    for (Order order : selectedOrders) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("trip_id", tripId);
                        updates.put("state", "Arranged");

                        db.collection("orders")
                                .document(order.getDocumentId())
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    completedCount[0]++;

                                    // Send notification to user
                                    NotificationHelper.sendUserNotification(
                                            order.getClientId(),
                                            driverId,
                                            driverName,
                                            null
                                    );

                                    // When all orders are updated
                                    if (completedCount[0] == totalOrders) {
                                        // Send notification to driver
                                        NotificationHelper.sendDriverNotification(
                                                driverId,
                                                tripDate,
                                                tripStartTime,
                                                null
                                        );

                                        Toast.makeText(getContext(), "Xếp tài xế thành công!", Toast.LENGTH_SHORT).show();

                                        // Clear selection and reload
                                        adapter.clearSelection();
                                        loadBookedCustomers();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Lỗi khi cập nhật đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tạo chuyến đi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload when fragment becomes visible
        loadBookedCustomers();
    }
}
