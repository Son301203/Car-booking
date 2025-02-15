package com.example.bookcar.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookcar.model.Trips;

import java.util.ArrayList;

public class TripAdapter extends ArrayAdapter<Trips> {
    int idlayout;
    Activity context;
    ArrayList<Trips> tripsArrayList;

    public TripAdapter(int idlayout, Activity context, ArrayList<Trips> tripsArrayList) {
        super(context, idlayout, tripsArrayList);
        this.idlayout = idlayout;
        this.context = context;
        this.tripsArrayList = tripsArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
