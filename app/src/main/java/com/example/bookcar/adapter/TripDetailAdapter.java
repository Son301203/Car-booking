package com.example.bookcar.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
                        String driverId = seat.getDriverId();
                        String tripId = seat.getTripId();
                        String clientId = seat.getClientId();

                        db.collection("drivers")
                                .document(driverId)
                                .collection("trips")
                                .document(tripId)
                                .collection("clients")
                                .document(clientId)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        String customerId = task.getResult().getString("customerId");

                                        db.collection("users")
                                                .document(customerId)
                                                .collection("orders")
                                                .whereEqualTo("tripId", tripId)
                                                .get()
                                                .addOnCompleteListener(orderTask -> {
                                                    if (orderTask.isSuccessful() && orderTask.getResult() != null) {
                                                        for (QueryDocumentSnapshot orderDoc : orderTask.getResult()) {
                                                            String orderId = orderDoc.getId();

                                                            db.collection("users")
                                                                    .document(customerId)
                                                                    .collection("orders")
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
                                                                    });
                                                        }
                                                    }
                                                });
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
                        String driverId = seat.getDriverId();
                        String tripId = seat.getTripId();
                        String clientId = seat.getClientId();

                        db.collection("drivers")
                                .document(driverId)
                                .collection("trips")
                                .document(tripId)
                                .collection("clients")
                                .document(clientId)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        String customerId = task.getResult().getString("customerId");

                                        db.collection("users")
                                                .document(customerId)
                                                .collection("orders")
                                                .whereEqualTo("tripId", tripId)
                                                .whereEqualTo("state", "Picked Up")
                                                .get()
                                                .addOnCompleteListener(orderTask -> {
                                                    if (orderTask.isSuccessful()) {
                                                        QuerySnapshot orderSnapshot = orderTask.getResult();
                                                        if (orderSnapshot != null && !orderSnapshot.isEmpty()) {
                                                            for (QueryDocumentSnapshot orderDoc : orderSnapshot) {
                                                                String orderId = orderDoc.getId();

                                                                db.collection("users")
                                                                        .document(customerId)
                                                                        .collection("orders")
                                                                        .document(orderId)
                                                                        .update("state", "Completed")
                                                                        .addOnSuccessListener(aVoid -> {
                                                                            Toast.makeText(context, "Đã xác nhận trả khách thành công", Toast.LENGTH_SHORT).show();
                                                                            // Fetch lại dữ liệu sau khi trả khách
                                                                            if (context instanceof TripDetailActivity) {
                                                                                ((TripDetailActivity) context).fetchClients();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            Toast.makeText(context, "Đã xảy ra lỗi trong quá trình xác nhận trả khách", Toast.LENGTH_SHORT).show();
                                                                        });
                                                            }
                                                        } else {
                                                            Toast.makeText(context, "Bạn phải đón khách trước khi trả", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                });
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return convertView;
    }
}