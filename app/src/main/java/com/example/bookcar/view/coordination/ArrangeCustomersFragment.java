package com.example.bookcar.view.coordination;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.example.bookcar.adapter.ClusterOrderAdapter;
import com.example.bookcar.adapter.ClusteringResultsAdapter;
import com.example.bookcar.api.ClusteringApiService;
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
    private Button btnAutoCluster;

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
        btnAutoCluster = view.findViewById(R.id.btnAutoCluster);

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
        btnAutoCluster.setOnClickListener(v -> showAutoClusterDialog());

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

    private void showAutoClusterDialog() {
        if (bookedOrders.isEmpty()) {
            Toast.makeText(getContext(), "Không có khách hàng nào để phân cụm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if all orders have coordinates
        List<Order> ordersWithCoords = new ArrayList<>();
        for (Order order : bookedOrders) {
            if (order.getPickupCoordinates() != null) {
                ordersWithCoords.add(order);
            }
        }

        if (ordersWithCoords.isEmpty()) {
            Toast.makeText(getContext(), "Không có khách hàng nào có tọa độ hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ordersWithCoords.size() < bookedOrders.size()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Cảnh báo")
                    .setMessage(String.format("Chỉ có %d/%d khách hàng có tọa độ hợp lệ. Tiếp tục?",
                            ordersWithCoords.size(), bookedOrders.size()))
                    .setPositiveButton("Tiếp tục", (dialog, which) -> performAutoClustering(ordersWithCoords))
                    .setNegativeButton("Hủy", null)
                    .show();
            return;
        }

        performAutoClustering(ordersWithCoords);
    }

    private void performAutoClustering(List<Order> orders) {
        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(getContext())
                .setTitle("Đang phân cụm tự động")
                .setMessage("Vui lòng đợi...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        // Call clustering API
        ClusteringApiService.getInstance().clusterCustomers(orders, 10, new ClusteringApiService.ClusteringCallback() {
            @Override
            public void onSuccess(List<ClusteringApiService.SuggestedTrip> trips) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        showClusteringResults(trips);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        new AlertDialog.Builder(getContext())
                                .setTitle("Lỗi phân cụm tự động")
                                .setMessage("Không thể kết nối đến dịch vụ phân cụm.\n\n" +
                                        "Lỗi: " + error + "\n\n" +
                                        "Vui lòng kiểm tra:\n" +
                                        "1. Python API server đang chạy\n" +
                                        "2. Cấu hình URL trong ClusteringApiService\n\n" +
                                        "Bạn vẫn có thể phân chuyến thủ công.")
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
            }
        });
    }

    private void showClusteringResults(List<ClusteringApiService.SuggestedTrip> trips) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_clustering_results, null);
        builder.setView(dialogView);

        RecyclerView recyclerViewTrips = dialogView.findViewById(R.id.recyclerViewSuggestedTrips);
        TextView tvTotalTrips = dialogView.findViewById(R.id.tvTotalTrips);
        TextView tvTotalCustomers = dialogView.findViewById(R.id.tvTotalCustomers);
        Button btnApplyAll = dialogView.findViewById(R.id.btnApplyAllTrips);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelClustering);

        AlertDialog dialog = builder.create();

        // Set summary
        int totalCustomers = 0;
        for (ClusteringApiService.SuggestedTrip trip : trips) {
            totalCustomers += trip.numPassengers;
        }
        tvTotalTrips.setText("Tổng số chuyến đề xuất: " + trips.size());
        tvTotalCustomers.setText("Tổng số khách: " + totalCustomers);

        // Setup RecyclerView
        ClusteringResultsAdapter adapter = new ClusteringResultsAdapter(getContext(), trips, new ClusteringResultsAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(ClusteringApiService.SuggestedTrip trip) {
                // Apply single trip
                dialog.dismiss();
                showDriverSelectionForTrip(trip);
            }

            @Override
            public void onViewDetails(ClusteringApiService.SuggestedTrip trip) {
                // Show cluster details
                showClusterDetailsDialog(trip, trips);
            }
        });
        recyclerViewTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTrips.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnApplyAll.setOnClickListener(v -> {
            dialog.dismiss();
            showConfirmApplyAllTrips(trips);
        });

        dialog.show();
    }

    private void showDriverSelectionForTrip(ClusteringApiService.SuggestedTrip trip) {
        // Get orders for this trip
        List<Order> tripOrders = new ArrayList<>();
        for (String orderId : trip.customerIds) {
            for (Order order : bookedOrders) {
                if (orderId.equals(order.getDocumentId())) {
                    tripOrders.add(order);
                    break;
                }
            }
        }

        if (tripOrders.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show driver selection with pre-filled time
        showDriverSelectionDialogForOrders(tripOrders, trip.suggestedDepartureTime);
    }

    private void showDriverSelectionDialogForOrders(List<Order> orders, String suggestedTime) {
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

        // Set suggested time
        if (suggestedTime != null && !suggestedTime.isEmpty()) {
            try {
                String[] parts = suggestedTime.split(":");
                timePicker.setHour(Integer.parseInt(parts[0]));
                timePicker.setMinute(Integer.parseInt(parts[1]));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing suggested time", e);
            }
        }

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
            arrangeDriver(driverId, driverName, tripStartTime, orders);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showConfirmApplyAllTrips(List<ClusteringApiService.SuggestedTrip> trips) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận")
                .setMessage("Tự động tạo " + trips.size() + " chuyến đi?\n\n" +
                        "Bạn sẽ cần chọn tài xế cho từng chuyến.")
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    applyTripsSequentially(trips, 0);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void applyTripsSequentially(List<ClusteringApiService.SuggestedTrip> trips, int index) {
        if (index >= trips.size()) {
            Toast.makeText(getContext(), "Đã hoàn thành tất cả các chuyến!", Toast.LENGTH_SHORT).show();
            loadBookedCustomers();
            return;
        }

        ClusteringApiService.SuggestedTrip trip = trips.get(index);

        // Show dialog for this trip
        new AlertDialog.Builder(getContext())
                .setTitle("Chuyến " + (index + 1) + "/" + trips.size())
                .setMessage(trip.getTripName() + "\n" +
                        trip.getDescription() + "\n\n" +
                        "Chọn tài xế cho chuyến này")
                .setPositiveButton("Chọn tài xế", (dialog, which) -> {
                    showDriverSelectionForTripInSequence(trip, trips, index);
                })
                .setNegativeButton("Bỏ qua", (dialog, which) -> {
                    applyTripsSequentially(trips, index + 1);
                })
                .setNeutralButton("Dừng lại", null)
                .show();
    }

    private void showDriverSelectionForTripInSequence(ClusteringApiService.SuggestedTrip trip,
                                                       List<ClusteringApiService.SuggestedTrip> allTrips,
                                                       int currentIndex) {
        // Get orders for this trip
        List<Order> tripOrders = new ArrayList<>();
        for (String orderId : trip.customerIds) {
            for (Order order : bookedOrders) {
                if (orderId.equals(order.getDocumentId())) {
                    tripOrders.add(order);
                    break;
                }
            }
        }

        if (tripOrders.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            applyTripsSequentially(allTrips, currentIndex + 1);
            return;
        }

        // Show driver selection with callback to continue sequence
        showDriverSelectionWithCallback(tripOrders, trip.suggestedDepartureTime, success -> {
            if (success) {
                // Continue with next trip after a short delay
                if (getView() != null) {
                    getView().postDelayed(() -> {
                        applyTripsSequentially(allTrips, currentIndex + 1);
                    }, 500);
                }
            }
        });
    }

    private void showDriverSelectionWithCallback(List<Order> orders, String suggestedTime,
                                                  DriverSelectionCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_driver_for_trip, null);
        builder.setView(dialogView);

        TimePicker timePicker = dialogView.findViewById(R.id.timePickerTripStart);
        RadioGroup radioGroupDrivers = dialogView.findViewById(R.id.radioGroupDrivers);
        ProgressBar progressBarDrivers = dialogView.findViewById(R.id.progressBarDrivers);
        TextView tvNoDrivers = dialogView.findViewById(R.id.tvNoDrivers);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelSelectDriver);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmArrangeDriver);

        timePicker.setIs24HourView(true);

        // Set suggested time
        if (suggestedTime != null && !suggestedTime.isEmpty()) {
            try {
                String[] parts = suggestedTime.split(":");
                timePicker.setHour(Integer.parseInt(parts[0]));
                timePicker.setMinute(Integer.parseInt(parts[1]));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing suggested time", e);
            }
        }

        AlertDialog dialog = builder.create();

        loadDriversForDialog(radioGroupDrivers, progressBarDrivers, tvNoDrivers);

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            callback.onResult(false);
        });

        btnConfirm.setOnClickListener(v -> {
            int selectedDriverId = radioGroupDrivers.getCheckedRadioButtonId();
            if (selectedDriverId == -1) {
                Toast.makeText(getContext(), "Vui lòng chọn tài xế", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadio = dialogView.findViewById(selectedDriverId);
            String driverId = (String) selectedRadio.getTag();
            String driverName = selectedRadio.getText().toString();

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String tripStartTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            arrangeDriver(driverId, driverName, tripStartTime, orders);
            dialog.dismiss();
            callback.onResult(true);
        });

        dialog.show();
    }

    private interface DriverSelectionCallback {
        void onResult(boolean success);
    }

    private void showClusterDetailsDialog(ClusteringApiService.SuggestedTrip trip, List<ClusteringApiService.SuggestedTrip> allTrips) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cluster_order_details, null);
        builder.setView(dialogView);

        TextView tvClusterTitle = dialogView.findViewById(R.id.tvClusterTitle);
        TextView tvClusterInfo = dialogView.findViewById(R.id.tvClusterInfo);
        RecyclerView recyclerViewOrders = dialogView.findViewById(R.id.recyclerViewClusterOrders);
        Button btnMoveCustomers = dialogView.findViewById(R.id.btnMoveCustomers);
        Button btnClose = dialogView.findViewById(R.id.btnCloseClusterDetails);

        AlertDialog dialog = builder.create();

        // Set cluster info
        tvClusterTitle.setText(trip.getTripName());
        tvClusterInfo.setText(String.format(Locale.getDefault(),
                "%d khách - Khởi hành: %s",
                trip.numPassengers,
                trip.suggestedDepartureTime));

        // Get orders for this cluster
        List<Order> clusterOrders = new ArrayList<>();
        for (String orderId : trip.customerIds) {
            for (Order order : bookedOrders) {
                if (orderId.equals(order.getDocumentId())) {
                    clusterOrders.add(order);
                    break;
                }
            }
        }

        // Setup RecyclerView with cluster orders
        ClusterOrderAdapter orderAdapter = new ClusterOrderAdapter(getContext(), clusterOrders, false);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrders.setAdapter(orderAdapter);

        // Move customers button
        btnMoveCustomers.setOnClickListener(v -> {
            dialog.dismiss();
            showMoveCustomersDialog(trip, allTrips);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showMoveCustomersDialog(ClusteringApiService.SuggestedTrip sourceTrip, List<ClusteringApiService.SuggestedTrip> allTrips) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_move_customers, null);
        builder.setView(dialogView);

        Spinner spinnerTargetCluster = dialogView.findViewById(R.id.spinnerTargetCluster);
        RecyclerView recyclerViewSelectCustomers = dialogView.findViewById(R.id.recyclerViewSelectCustomers);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelMove);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmMove);

        AlertDialog dialog = builder.create();

        // Prepare target clusters (exclude source cluster)
        List<ClusteringApiService.SuggestedTrip> targetTrips = new ArrayList<>();
        List<String> clusterNames = new ArrayList<>();
        for (ClusteringApiService.SuggestedTrip trip : allTrips) {
            if (trip.clusterId != sourceTrip.clusterId || trip.subTripIndex != sourceTrip.subTripIndex) {
                targetTrips.add(trip);
                clusterNames.add(String.format(Locale.getDefault(),
                        "%s (%d/%d khách)",
                        trip.getTripName(),
                        trip.numPassengers,
                        10)); // Max 10 passengers
            }
        }

        // Setup spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, clusterNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTargetCluster.setAdapter(spinnerAdapter);

        // Get orders for source cluster
        List<Order> sourceOrders = new ArrayList<>();
        for (String orderId : sourceTrip.customerIds) {
            for (Order order : bookedOrders) {
                if (orderId.equals(order.getDocumentId())) {
                    sourceOrders.add(order);
                    break;
                }
            }
        }

        // Setup RecyclerView with selection mode
        ClusterOrderAdapter orderAdapter = new ClusterOrderAdapter(getContext(), sourceOrders, true);
        recyclerViewSelectCustomers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSelectCustomers.setAdapter(orderAdapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            List<Order> selectedOrders = orderAdapter.getSelectedOrders();
            if (selectedOrders.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 khách hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            int targetIndex = spinnerTargetCluster.getSelectedItemPosition();
            if (targetIndex < 0 || targetIndex >= targetTrips.size()) {
                Toast.makeText(getContext(), "Vui lòng chọn cụm đích", Toast.LENGTH_SHORT).show();
                return;
            }

            ClusteringApiService.SuggestedTrip targetTrip = targetTrips.get(targetIndex);

            // Check if target cluster will exceed max capacity
            int newTargetSize = targetTrip.numPassengers + selectedOrders.size();
            if (newTargetSize > 10) {
                Toast.makeText(getContext(),
                        String.format(Locale.getDefault(), "Cụm đích sẽ vượt quá giới hạn 10 khách (%d khách)", newTargetSize),
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Perform the transfer
            performCustomerTransfer(sourceTrip, targetTrip, selectedOrders, allTrips);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performCustomerTransfer(ClusteringApiService.SuggestedTrip sourceTrip,
                                        ClusteringApiService.SuggestedTrip targetTrip,
                                        List<Order> customersToMove,
                                        List<ClusteringApiService.SuggestedTrip> allTrips) {
        // Remove customers from source
        for (Order order : customersToMove) {
            sourceTrip.customerIds.remove(order.getDocumentId());
        }
        sourceTrip.numPassengers -= customersToMove.size();

        // Add customers to target
        for (Order order : customersToMove) {
            targetTrip.customerIds.add(order.getDocumentId());
        }
        targetTrip.numPassengers += customersToMove.size();

        Toast.makeText(getContext(),
                String.format(Locale.getDefault(), "Đã chuyển %d khách từ %s sang %s",
                        customersToMove.size(),
                        sourceTrip.getTripName(),
                        targetTrip.getTripName()),
                Toast.LENGTH_LONG).show();

        // Refresh the clustering results view
        showClusteringResults(allTrips);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload when fragment becomes visible
        loadBookedCustomers();
    }
}
