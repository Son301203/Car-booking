package com.example.bookcar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookcar.R;
import com.example.bookcar.model.Order;

import java.util.ArrayList;
import java.util.List;

public class BookedCustomerAdapter extends RecyclerView.Adapter<BookedCustomerAdapter.ViewHolder> {
    private Context context;
    private List<Order> bookedOrders;
    private List<Order> selectedOrders;

    public BookedCustomerAdapter(Context context, List<Order> bookedOrders) {
        this.context = context;
        this.bookedOrders = bookedOrders;
        this.selectedOrders = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booked_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = bookedOrders.get(position);

        holder.tvCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "N/A");
        holder.tvCustomerPhone.setText(order.getCustomerPhone() != null ? order.getCustomerPhone() : "N/A");
        holder.tvPickupLocation.setText(order.getDeparture() != null ? order.getDeparture() : "N/A");
        holder.tvDestinationLocation.setText(order.getDestination() != null ? order.getDestination() : "N/A");

        // Display date and time
        String dateTimeText = "📅 " + (order.getDepartureDate() != null ? order.getDepartureDate() : "N/A");
        if (order.getDepartureTime() != null && !order.getDepartureTime().isEmpty()) {
            dateTimeText += " - " + order.getDepartureTime();
        }
        holder.tvDepartureDate.setText(dateTimeText);

        // Set checkbox state
        holder.checkboxSelect.setChecked(selectedOrders.contains(order));

        // Handle checkbox clicks
        holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedOrders.contains(order)) {
                    selectedOrders.add(order);
                }
            } else {
                selectedOrders.remove(order);
            }
        });

        // Make the whole item clickable
        holder.itemView.setOnClickListener(v -> {
            holder.checkboxSelect.setChecked(!holder.checkboxSelect.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return bookedOrders.size();
    }

    public List<Order> getSelectedOrders() {
        return selectedOrders;
    }

    public void clearSelection() {
        selectedOrders.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxSelect;
        TextView tvCustomerName;
        TextView tvCustomerPhone;
        TextView tvPickupLocation;
        TextView tvDestinationLocation;
        TextView tvDepartureDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelectCustomer);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvPickupLocation = itemView.findViewById(R.id.tvPickupLocation);
            tvDestinationLocation = itemView.findViewById(R.id.tvDestinationLocation);
            tvDepartureDate = itemView.findViewById(R.id.tvDepartureDate);
        }
    }
}

