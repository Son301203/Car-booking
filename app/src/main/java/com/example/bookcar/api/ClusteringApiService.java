package com.example.bookcar.api;

import android.util.Log;

import com.example.bookcar.model.Order;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service to communicate with Python clustering API
 */
public class ClusteringApiService {
    private static final String TAG = "ClusteringApiService";

    // TODO: Update this to your server URL
    // For local development: "http://10.0.2.2:5000" (Android emulator)
    // For real device: "http://YOUR_IP:5000"
    private static final String BASE_URL = "http://10.0.2.2:5000";

    private static ClusteringApiService instance;
    private OkHttpClient client;
    private Gson gson;

    private ClusteringApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        gson = new GsonBuilder()
                .setDateFormat("dd/MM/yyyy")
                .create();
    }

    public static synchronized ClusteringApiService getInstance() {
        if (instance == null) {
            instance = new ClusteringApiService();
        }
        return instance;
    }

    /**
     * Cluster customers and get trip suggestions
     */
    public void clusterCustomers(List<Order> orders, int maxPassengers, ClusteringCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONArray ordersArray = new JSONArray();

            for (Order order : orders) {
                JSONObject orderJson = new JSONObject();
                orderJson.put("documentId", order.getDocumentId());
                orderJson.put("clientId", order.getClientId());
                orderJson.put("departureDate", order.getDepartureDate());
                orderJson.put("departureTime", order.getDepartureTime());

                // Get coordinates
                if (order.getPickupCoordinates() != null) {
                    orderJson.put("pickup_coordinates_lat", order.getPickupCoordinates().getLatitude());
                    orderJson.put("pickup_coordinates_lng", order.getPickupCoordinates().getLongitude());
                } else {
                    Log.w(TAG, "Order " + order.getDocumentId() + " has no pickup coordinates");
                    continue;
                }

                orderJson.put("customerName", order.getCustomerName());
                orderJson.put("customerPhone", order.getCustomerPhone());

                ordersArray.put(orderJson);
            }

            requestBody.put("orders", ordersArray);
            requestBody.put("max_passengers", maxPassengers);

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/cluster-customers")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            if (json.getBoolean("success")) {
                                JSONArray tripsArray = json.getJSONArray("trips");
                                List<SuggestedTrip> trips = parseTrips(tripsArray);
                                callback.onSuccess(trips);
                            } else {
                                callback.onError(json.optString("error", "Unknown error"));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error", e);
                            callback.onError("Failed to parse response: " + e.getMessage());
                        }
                    } else {
                        callback.onError("Server error: " + response.code());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create request", e);
            callback.onError("Failed to create request: " + e.getMessage());
        }
    }

    /**
     * Check if API service is available
     */
    public void checkHealth(HealthCheckCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/health")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onResult(false, "Cannot connect to clustering service");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    try {
                        JSONObject json = new JSONObject(body);
                        boolean modelLoaded = json.optBoolean("model_loaded", false);
                        callback.onResult(modelLoaded, modelLoaded ? "Service ready" : "Model not loaded");
                    } catch (JSONException e) {
                        callback.onResult(false, "Invalid response");
                    }
                } else {
                    callback.onResult(false, "Service unavailable");
                }
            }
        });
    }

    private List<SuggestedTrip> parseTrips(JSONArray tripsArray) throws JSONException {
        List<SuggestedTrip> trips = new ArrayList<>();

        for (int i = 0; i < tripsArray.length(); i++) {
            JSONObject tripJson = tripsArray.getJSONObject(i);

            SuggestedTrip trip = new SuggestedTrip();
            trip.clusterId = tripJson.getInt("cluster_id");
            trip.subTripIndex = tripJson.getInt("sub_trip_index");
            trip.numPassengers = tripJson.getInt("num_passengers");
            trip.suggestedDepartureTime = tripJson.getString("suggested_departure_time");
            trip.departureDate = tripJson.getString("departure_date");
            trip.centerLat = tripJson.getDouble("center_lat");
            trip.centerLng = tripJson.getDouble("center_lng");

            // Parse customer IDs
            JSONArray customerIdsArray = tripJson.getJSONArray("customer_ids");
            trip.customerIds = new ArrayList<>();
            for (int j = 0; j < customerIdsArray.length(); j++) {
                trip.customerIds.add(customerIdsArray.getString(j));
            }

            trips.add(trip);
        }

        return trips;
    }

    /**
     * Data class for suggested trip
     */
    public static class SuggestedTrip {
        @SerializedName("cluster_id")
        public int clusterId;

        @SerializedName("sub_trip_index")
        public int subTripIndex;

        @SerializedName("num_passengers")
        public int numPassengers;

        @SerializedName("suggested_departure_time")
        public String suggestedDepartureTime;

        @SerializedName("departure_date")
        public String departureDate;

        @SerializedName("center_lat")
        public double centerLat;

        @SerializedName("center_lng")
        public double centerLng;

        @SerializedName("customer_ids")
        public List<String> customerIds;

        public String getTripName() {
            if (subTripIndex > 0) {
                return "Cụm " + clusterId + " - Chuyến " + (subTripIndex + 1);
            }
            return "Cụm " + clusterId;
        }

        public String getDescription() {
            return numPassengers + " khách - " + suggestedDepartureTime;
        }
    }

    /**
     * Callback interface for clustering results
     */
    public interface ClusteringCallback {
        void onSuccess(List<SuggestedTrip> trips);
        void onError(String error);
    }

    /**
     * Callback for health check
     */
    public interface HealthCheckCallback {
        void onResult(boolean isHealthy, String message);
    }
}

