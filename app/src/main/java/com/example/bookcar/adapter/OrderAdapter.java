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
import com.example.bookcar.model.Order;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class OrderAdapter extends ArrayAdapter<Order> {
    Activity context;
    int idLayout;
    ArrayList<Order> orderList;

    public OrderAdapter( Activity context, int idLayout, ArrayList<Order> orderList) {
        super(context, idLayout, orderList);
        this.context = context;
        this.idLayout = idLayout;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(idLayout, null);
        Order order = orderList.get(position);

        TextView depature = convertView.findViewById(R.id.txtDepature);
        depature.setText("Depature: " + order.getDeparture());

        TextView destination = convertView.findViewById(R.id.txtDestination);
        destination.setText("Destination: " + order.getDestination());

        TextView deparureDate = convertView.findViewById(R.id.txtDepatureDate);
        deparureDate.setText("Deparute Date: " + order.getDepartureDate());

        TextView returnDate = convertView.findViewById(R.id.txtReturnDate);
        returnDate.setText("Return Date: " + order.getReturnDate());

        return convertView;

    }
}
