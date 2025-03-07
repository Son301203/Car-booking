package com.example.bookcar.adapter;

import android.app.Activity;
import android.content.Context;
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

public class TripAdapter extends ArrayAdapter<Trips> {
    int idlayout;
    Activity context;
    ArrayList<Trips> tripsArrayList;

    public TripAdapter(Activity context, int idlayout, ArrayList<Trips> tripsArrayList) {
        super(context, idlayout, tripsArrayList);
        this.context = context;
        this.idlayout = idlayout;
        this.tripsArrayList = tripsArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(idlayout, null);
        Trips trips = tripsArrayList.get(position);

        TextView departureDate = convertView.findViewById(R.id.departure_date);
        departureDate.setText("Ngày: " + trips.getDateTrips());

        TextView departureTime = convertView.findViewById(R.id.departure_time);
        departureTime.setText("Giờ: " + trips.getTimeTrips());

        TextView quantity = convertView.findViewById(R.id.num_passengers);
        quantity.setText("Tổng số lượng khách: " + trips.getQuantity());

        return convertView;
    }
}
