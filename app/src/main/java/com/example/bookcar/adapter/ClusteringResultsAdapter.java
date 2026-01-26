package com.example.bookcar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookcar.R;
import com.example.bookcar.api.ClusteringApiService;

import java.util.List;
import java.util.Locale;

public class ClusteringResultsAdapter extends RecyclerView.Adapter<ClusteringResultsAdapter.ViewHolder> {

    private Context context;
    private List<ClusteringApiService.SuggestedTrip> trips;
    private OnTripClickListener listener;
    private OnMapClickListener mapClickListener;

    public interface OnTripClickListener {
        void onTripClick(ClusteringApiService.SuggestedTrip trip);
        void onViewDetails(ClusteringApiService.SuggestedTrip trip);
    }

    public interface OnMapClickListener {
        void onViewOnMap(ClusteringApiService.SuggestedTrip trip, int position);
    }

    public ClusteringResultsAdapter(Context context, List<ClusteringApiService.SuggestedTrip> trips,
                                   OnTripClickListener listener) {
        this.context = context;
        this.trips = trips;
        this.listener = listener;
    }

    public void setMapClickListener(OnMapClickListener mapClickListener) {
        this.mapClickListener = mapClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggested_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClusteringApiService.SuggestedTrip trip = trips.get(position);

        holder.tvTripName.setText(trip.getTripName());
        holder.tvTripDetails.setText(String.format(Locale.getDefault(),
                "%d khách - Khởi hành: %s\nĐiểm trung tâm: %.4f, %.4f",
                trip.numPassengers,
                trip.suggestedDepartureTime,
                trip.centerLat,
                trip.centerLng));

        // Click on button to apply trip
        holder.btnApplyTrip.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTripClick(trip);
            }
        });

        // Click on view on map button
        holder.btnViewOnMap.setOnClickListener(v -> {
            if (mapClickListener != null) {
                mapClickListener.onViewOnMap(trip, position);
            }
        });

        // Click on card to view details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(trip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripName;
        TextView tvTripDetails;
        Button btnApplyTrip;
        Button btnViewOnMap;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripName = itemView.findViewById(R.id.tvTripName);
            tvTripDetails = itemView.findViewById(R.id.tvTripDetails);
            btnApplyTrip = itemView.findViewById(R.id.btnApplyTrip);
            btnViewOnMap = itemView.findViewById(R.id.btnViewOnMap);
        }
    }
}

