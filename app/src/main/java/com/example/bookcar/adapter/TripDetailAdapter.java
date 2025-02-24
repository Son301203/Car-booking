package com.example.bookcar.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookcar.R;
import com.example.bookcar.model.Seat;
import com.example.bookcar.view.drivers.LocationInMapActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Map;

public class TripDetailAdapter extends ArrayAdapter<Seat> {
    int idLayout;
    Activity context;
    ArrayList<Seat> tripDetailList;

    private FirebaseFirestore db;

    public TripDetailAdapter(Activity context, int idLayout,  ArrayList<Seat> tripDetailList) {
        super(context, idLayout, tripDetailList);
        this.idLayout = idLayout;
        this.context = context;
        this.tripDetailList = tripDetailList;
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
        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String driverId = seat.getDriverId();
                String tripId = seat.getTripId();
                String clientId = seat.getClientId();

                System.out.println("driverId: " + driverId);
                System.out.println("tripId: " + tripId);
                System.out.println("clientId: " + clientId);

                getCoordinates(driverId, tripId, clientId);
            }
        });

        return convertView;
    }


    private void getCoordinates(String driverId, String tripId, String clientId) {
        db = FirebaseFirestore.getInstance();

        if (driverId != null && tripId != null && clientId != null) {
            DocumentReference locationRef = db.collection("drivers")
                    .document(driverId)
                    .collection("trips")
                    .document(tripId)
                    .collection("clients")
                    .document(clientId);

            locationRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("destinationCoordinates")) {
                    Object obj = documentSnapshot.get("destinationCoordinates");
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> destinationCoordinates = (Map<String, Object>) obj;
                        Double latitude = destinationCoordinates.get("latitude") instanceof Number ?
                                ((Number) destinationCoordinates.get("latitude")).doubleValue() : null;
                        Double longitude = destinationCoordinates.get("longitude") instanceof Number ?
                                ((Number) destinationCoordinates.get("longitude")).doubleValue() : null;

                        if (latitude != null && longitude != null) {
                            Intent intent = new Intent(context, LocationInMapActivity.class);
                            intent.putExtra("latitude", latitude);
                            intent.putExtra("longitude", longitude);
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "Không có dữ liệu tọa độ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Dữ liệu tọa độ không đúng định dạng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Không có dữ liệu trên bản đồ", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(context, "Dữ liệu khách hàng không phản hồi", Toast.LENGTH_SHORT).show()
            );
        } else {
            Toast.makeText(context, "Không có dữ liệu khách hàng", Toast.LENGTH_SHORT).show();
        }
    }

}
