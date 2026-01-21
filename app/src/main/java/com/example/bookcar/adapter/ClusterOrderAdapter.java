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

public class ClusterOrderAdapter extends RecyclerView.Adapter<ClusterOrderAdapter.ViewHolder> {
    private Context context;
    private List<Order> orders;
    private List<Order> selectedOrders;
    private boolean selectionMode;

    public ClusterOrderAdapter(Context context, List<Order> orders, boolean selectionMode) {
        this.context = context;
        this.orders = orders;
        this.selectedOrders = new ArrayList<>();
        this.selectionMode = selectionMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cluster_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        // Display customer info
        holder.tvCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "N/A");
        holder.tvCustomerPhone.setText("📞 " + (order.getCustomerPhone() != null ? order.getCustomerPhone() : "N/A"));

        // Display pickup location - fix for the N/A issue
        String pickupLocation = order.getDeparture();
        if (pickupLocation == null || pickupLocation.isEmpty()) {
            pickupLocation = "N/A";
        }
        holder.tvPickupLocation.setText("📍 Điểm đón: " + pickupLocation);

        // Display destination
        holder.tvDestination.setText("🎯 Điểm đến: " + (order.getDestination() != null ? order.getDestination() : "N/A"));

        // Display time
        String timeText = "";
        if (order.getDepartureDate() != null && !order.getDepartureDate().isEmpty()) {
            timeText = "📅 " + order.getDepartureDate();
            if (order.getDepartureTime() != null && !order.getDepartureTime().isEmpty()) {
                timeText += " - " + order.getDepartureTime();
            }
        } else {
            timeText = "📅 N/A";
        }
        holder.tvDateTime.setText(timeText);

        // Show/hide checkbox based on selection mode
        holder.checkboxSelect.setVisibility(selectionMode ? View.VISIBLE : View.GONE);

        if (selectionMode) {
            holder.checkboxSelect.setChecked(selectedOrders.contains(order));
            holder.checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedOrders.contains(order)) {
                        selectedOrders.add(order);
                    }
                } else {
                    selectedOrders.remove(order);
                }
            });

            holder.itemView.setOnClickListener(v -> {
                holder.checkboxSelect.setChecked(!holder.checkboxSelect.isChecked());
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
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
        TextView tvDestination;
        TextView tvDateTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelectOrder);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvPickupLocation = itemView.findViewById(R.id.tvPickupLocation);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
        }
    }
}

