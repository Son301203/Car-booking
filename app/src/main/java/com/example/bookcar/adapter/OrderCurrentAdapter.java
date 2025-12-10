package com.example.bookcar.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookcar.R;
import com.example.bookcar.model.Order;
import com.example.bookcar.view.clients.OrderActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrderCurrentAdapter extends ArrayAdapter<Order> {
    private Activity context;
    private int idLayout;
    private ArrayList<Order> orderList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public OrderCurrentAdapter(Activity context, int idLayout, ArrayList<Order> orderList) {
        super(context, idLayout, orderList);
        this.context = context;
        this.idLayout = idLayout;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        LayoutInflater inflater = context.getLayoutInflater();
        convertView = inflater.inflate(idLayout, null);
        Order order = orderList.get(position);

        TextView depature = convertView.findViewById(R.id.txtDepatureCurrent);
        depature.setText("Điểm đón: " + order.getDeparture());

        TextView destination = convertView.findViewById(R.id.txtDestinationCurrent);
        destination.setText("Điểm đến: " + order.getDestination());

        TextView deparureDate = convertView.findViewById(R.id.txtDepatureDateCurrent);
        deparureDate.setText("Ngày đi: " + order.getDepartureDate());

        TextView returnDate = convertView.findViewById(R.id.txtReturnDateCurrent);
        returnDate.setText("Ngày về: " + order.getReturnDate());

        //cancel order
        ImageView btnDeleteItem = convertView.findViewById(R.id.btnDeleteItem);
        btnDeleteItem.setOnClickListener(v -> {
            // Optionally, add a confirmation dialog here if desired
            new AlertDialog.Builder(context)
                    .setTitle("Hủy chuyến")
                    .setMessage("Bạn chắc chắn muốn hủy chuyến không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Cancel this specific order in root orders collection using the order's document ID
                        String orderDocumentId = order.getDocumentId();

                        db.collection("orders")
                                .document(orderDocumentId)
                                .update("state", "Cancel")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Hủy chuyến thành công", Toast.LENGTH_SHORT).show();
                                    if (context instanceof OrderActivity) {
                                        ((OrderActivity) context).onOrderCanceled(order, position);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Hủy chuyến thất bại " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        return convertView;
    }
}
