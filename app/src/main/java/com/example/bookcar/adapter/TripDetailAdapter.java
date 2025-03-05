package com.example.bookcar.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.bookcar.R;
import com.example.bookcar.model.Seat;
import com.example.bookcar.view.drivers.LocationInMapActivity;
import com.google.firebase.firestore.FirebaseFirestore;

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
        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String driverId = seat.getDriverId();
                String tripId = seat.getTripId();
                String clientId = seat.getClientId();

                Intent intent = new Intent(context, LocationInMapActivity.class);
                intent.putExtra("driverId", driverId);
                intent.putExtra("tripId", tripId);
                intent.putExtra("clientId", clientId);
                context.startActivity(intent);
            }
        });

        ImageView btnCall = convertView.findViewById(R.id.call_button);
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clientPhone = seat.getPhone();
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + clientPhone));
                if (ActivityCompat.checkSelfPermission(context,
                        android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(context, new
                            String[]{android.Manifest.permission.CALL_PHONE},1);
                    return;
                }
                context.startActivity(callIntent);
                System.out.println("tel: " + clientPhone);
            }
        });

        return convertView;
    }
}
