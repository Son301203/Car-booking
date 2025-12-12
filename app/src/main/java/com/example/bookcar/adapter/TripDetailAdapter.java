package com.example.bookcar.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.bookcar.R;
import com.example.bookcar.model.Seat;
import com.example.bookcar.view.drivers.LocationInMapActivity;
import com.example.bookcar.view.drivers.TripDetailActivity; // Thêm import này
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class TripDetailAdapter extends ArrayAdapter<Seat> {
    int idLayout;
    Activity context;
    ArrayList<Seat> tripDetailList;
    private FirebaseFirestore db;

    public TripDetailAdapter(Activity context, int idLayout, ArrayList<Seat> tripDetailList) {
        super(context, idLayout, tripDetailList);
        this.idLayout = idLayout;
        this.context = context;
        this.tripDetailList = tripDetailList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(idLayout, null);
        Seat seat = tripDetailList.get(position);

        TextView guestName = convertView.findViewById(R.id.guest_name);
        guestName.setText(seat.getUsername());

        TextView guestPhone = convertView.findViewById(R.id.guest_phone);
        guestPhone.setText(seat.getPhone());

        // Get order state
        String orderState = seat.getOrderState();
        boolean isCompleted = "Completed".equals(orderState);
        boolean isCancelled = "Cancelled".equals(orderState);

        // Show/hide status indicator and buttons based on order state
        TextView orderStatus = convertView.findViewById(R.id.order_status);
        View buttonsLayout = convertView.findViewById(R.id.buttons_layout);

        if (isCompleted) {
            orderStatus.setVisibility(View.VISIBLE);
            orderStatus.setText("✓ Đã hoàn thành chuyến đi");
            orderStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            buttonsLayout.setVisibility(View.GONE);
        } else if (isCancelled) {
            orderStatus.setVisibility(View.VISIBLE);
            orderStatus.setText("✗ Đã hủy chuyến đi");
            orderStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            buttonsLayout.setVisibility(View.GONE);
        } else {
            orderStatus.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
        }

        TextView showLocation = convertView.findViewById(R.id.location_link);
        showLocation.setOnClickListener(view -> {
            String driverId = seat.getDriverId();
            String tripId = seat.getTripId();
            String clientId = seat.getClientId();

            Intent intent = new Intent(context, LocationInMapActivity.class);
            intent.putExtra("driverId", driverId);
            intent.putExtra("tripId", tripId);
            intent.putExtra("clientId", clientId);
            context.startActivity(intent);
        });

        ImageView btnCall = convertView.findViewById(R.id.call_button);
        btnCall.setOnClickListener(view -> {
            String clientPhone = seat.getPhone();
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + clientPhone));
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                return;
            }
            context.startActivity(callIntent);
            System.out.println("tel: " + clientPhone);
        });

        // Pick up
        Button btnPickup = convertView.findViewById(R.id.pickup_button);
        btnPickup.setOnClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận đón khách")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String tripId = seat.getTripId();
                        String clientId = seat.getClientId();

                        // Validate inputs
                        if (tripId == null || tripId.isEmpty()) {
                            Toast.makeText(context, "Lỗi: Không tìm thấy thông tin chuyến đi", Toast.LENGTH_SHORT).show();
                            Log.e("TripDetailAdapter", "tripId is null or empty");
                            return;
                        }

                        if (clientId == null || clientId.isEmpty()) {
                            Toast.makeText(context, "Lỗi: Không tìm thấy thông tin khách hàng", Toast.LENGTH_SHORT).show();
                            Log.e("TripDetailAdapter", "clientId is null or empty");
                            return;
                        }

                        // Query orders from root collection
                        db.collection("orders")
                                .whereEqualTo("trip_id", tripId)
                                .whereEqualTo("client_id", clientId)
                                .get()
                                .addOnCompleteListener(orderTask -> {
                                    if (orderTask.isSuccessful() && orderTask.getResult() != null) {
                                        if (orderTask.getResult().isEmpty()) {
                                            Toast.makeText(context, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        for (QueryDocumentSnapshot orderDoc : orderTask.getResult()) {
                                            String orderId = orderDoc.getId();

                                            // Update order state in root orders collection
                                            db.collection("orders")
                                                    .document(orderId)
                                                    .update("state", "Picked Up")
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(context, "Đã xác nhận đón khách thành công", Toast.LENGTH_SHORT).show();
                                                        // Fetch lại dữ liệu sau khi đón khách
                                                        if (context instanceof TripDetailActivity) {
                                                            ((TripDetailActivity) context).fetchClients();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(context, "Đã xảy ra lỗi trong quá trình xác nhận đón khách", Toast.LENGTH_SHORT).show();
                                                        Log.e("TripDetailAdapter", "Error updating order", e);
                                                    });
                                        }
                                    } else {
                                        Toast.makeText(context, "Lỗi khi tải thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                                        Log.e("TripDetailAdapter", "Error getting orders", orderTask.getException());
                                    }
                                });
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Drop off
        Button btnDropOff = convertView.findViewById(R.id.dropoff_button);
        btnDropOff.setOnClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận trả khách")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        String tripId = seat.getTripId();
                        String clientId = seat.getClientId();

                        // Validate inputs
                        if (tripId == null || tripId.isEmpty()) {
                            Toast.makeText(context, "Lỗi: Không tìm thấy thông tin chuyến đi", Toast.LENGTH_SHORT).show();
                            Log.e("TripDetailAdapter", "tripId is null or empty");
                            return;
                        }

                        if (clientId == null || clientId.isEmpty()) {
                            Toast.makeText(context, "Lỗi: Không tìm thấy thông tin khách hàng", Toast.LENGTH_SHORT).show();
                            Log.e("TripDetailAdapter", "clientId is null or empty");
                            return;
                        }

                        // Query orders from root collection - only Picked Up orders
                        db.collection("orders")
                                .whereEqualTo("trip_id", tripId)
                                .whereEqualTo("client_id", clientId)
                                .whereEqualTo("state", "Picked Up")
                                .get()
                                .addOnCompleteListener(orderTask -> {
                                    if (orderTask.isSuccessful()) {
                                        QuerySnapshot orderSnapshot = orderTask.getResult();
                                        if (orderSnapshot != null && !orderSnapshot.isEmpty()) {
                                            for (QueryDocumentSnapshot orderDoc : orderSnapshot) {
                                                String orderId = orderDoc.getId();

                                                // Update order state to Completed
                                                db.collection("orders")
                                                        .document(orderId)
                                                        .update("state", "Completed")
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(context, "Đã xác nhận trả khách thành công", Toast.LENGTH_SHORT).show();

                                                            // Send notification to user
                                                            db.collection("notifications")
                                                                    .document("users")
                                                                    .collection("users")
                                                                    .document(clientId)
                                                                    .collection("messages")
                                                                    .document(String.valueOf(System.currentTimeMillis()))
                                                                    .set(new HashMap<String, Object>() {{
                                                                        put("userId", clientId);
                                                                        put("title", "Về chuyến đi");
                                                                        put("image", "completed_trip.png");
                                                                        put("message", "Bạn đã hoàn thành chuyến đi");
                                                                        put("timestamp", System.currentTimeMillis());
                                                                        put("read", false);
                                                                    }})
                                                                    .addOnSuccessListener(documentReference -> {
                                                                        Log.d("TripDetailAdapter", "Notification sent successfully");
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.e("TripDetailAdapter", "Error sending notification", e);
                                                                    });

                                                            // Refresh the list
                                                            if (context instanceof TripDetailActivity) {
                                                                ((TripDetailActivity) context).fetchClients();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Đã xảy ra lỗi trong quá trình xác nhận trả khách", Toast.LENGTH_SHORT).show();
                                                            Log.e("TripDetailAdapter", "Error updating order", e);
                                                        });
                                            }
                                        } else {
                                            Toast.makeText(context, "Bạn phải đón khách trước khi trả", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(context, "Lỗi khi tải thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                                        Log.e("TripDetailAdapter", "Error getting orders", orderTask.getException());
                                    }
                                });
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return convertView;
    }
}