package com.example.bookcar.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookcar.R;
import com.example.bookcar.model.Trips;

import java.util.ArrayList;

public class TripDetailAdapter extends ArrayAdapter<Trips> {
    int idLayout;
    Activity context;
    ArrayList<Trips> tripDetailList;

    public TripDetailAdapter(Activity context, int idLayout,  ArrayList<Trips> tripDetailList) {
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
        Trips trips = tripDetailList.get(position);

        TextView guestName = convertView.findViewById(R.id.guest_name);
        guestName.setText(trips.getUsername());

        TextView guestPhone = convertView.findViewById(R.id.guest_phone);
        guestPhone.setText(trips.getPhone());

        return convertView;
    }
}
