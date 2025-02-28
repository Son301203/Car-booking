package com.example.bookcar.view.clients;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.adapter.OrderAdapter;
import com.example.bookcar.adapter.OrderCurrentAdapter;
import com.example.bookcar.model.Order;
import com.example.bookcar.view.bottomtab.TabUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class OrderActivity extends AppCompatActivity {
    private ListView listViewCurrentOrder, listViewCompleteOrder, listViewCancelOrder;
    private OrderAdapter orderCompleteAdapter, orderCancelAdapter;
    private OrderCurrentAdapter orderCurrentAdapter;
    private ArrayList<Order> orderCurrentList, orderCompleleList, orderCancelList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // Current order
        listViewCurrentOrder = findViewById(R.id.lvCurrentOrder);
        orderCurrentList = new ArrayList<>();
        orderCurrentAdapter = new OrderCurrentAdapter(this, R.layout.layout_listview_state_current_order, orderCurrentList);
        listViewCurrentOrder.setAdapter(orderCurrentAdapter);

        // Complete order
        listViewCompleteOrder = findViewById(R.id.lvCompleteOrder);
        orderCompleleList = new ArrayList<>();
        orderCompleteAdapter = new OrderAdapter(this, R.layout.layout_listview_state_order, orderCompleleList);
        listViewCompleteOrder.setAdapter(orderCompleteAdapter);

        // Cancel order
        listViewCancelOrder = findViewById(R.id.lvCancelOrder);
        orderCancelList = new ArrayList<>();
        orderCancelAdapter = new OrderAdapter(this, R.layout.layout_listview_state_order, orderCancelList);
        listViewCancelOrder.setAdapter(orderCancelAdapter);

        // Load orders from Firestore
        loadCurrentOrders();
        loadCompleteOrders();
        loadCancelOrders();
        setupTabSelector();
        TabUtils.setupTabClientUI(this);
    }

    private void loadCurrentOrders() {
        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference ordersRef = db.collection("users").
                document(userId).
                collection("orders");

        ordersRef.whereIn("state", Arrays.asList("Booked", "Picked Up", "Arranged"))
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                orderCurrentList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String pickup = document.getString("pickup");
                    String destination = document.getString("destination");
                    String departureDate = document.getString("departureDate");
                    String returnDate = document.getString("returnDate");
                    String documentId = document.getId();

                    // Create Order object
                    Order order = new Order(documentId, pickup, destination, departureDate, returnDate);
                    orderCurrentList.add(order);
                }
                orderCurrentAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(OrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCompleteOrders() {
        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference ordersRef = db.collection("users").
                document(userId).
                collection("orders");

        ordersRef.whereIn("state", Collections.singletonList("Completed"))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderCompleleList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pickup = document.getString("pickup");
                            String destination = document.getString("destination");
                            String departureDate = document.getString("departureDate");
                            String returnDate = document.getString("returnDate");
                            String documentId = document.getId();

                            // Create Order object
                            Order order = new Order(documentId, pickup, destination, departureDate, returnDate);
                            orderCompleleList.add(order);
                        }
                        orderCompleteAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(OrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCancelOrders(){
        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference ordersRef = db.collection("users").
                document(userId).
                collection("orders");

        ordersRef.whereIn("state", Collections.singletonList("Cancel"))
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderCancelList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String pickup = document.getString("pickup");
                            String destination = document.getString("destination");
                            String departureDate = document.getString("departureDate");
                            String returnDate = document.getString("returnDate");
                            String documentId = document.getId();

                            // Create Order object
                            Order order = new Order(documentId, pickup, destination, departureDate, returnDate);
                            orderCancelList.add(order);
                        }
                        orderCancelAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(OrderActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupTabSelector() {
        TabHost tabHost = findViewById(R.id.TabOrderState);
        tabHost.setup();


        TabHost.TabSpec tab1 = tabHost.newTabSpec("Current Orders");
        tab1.setContent(R.id.tab1);
        tab1.setIndicator("Hiện tại");
        tabHost.addTab(tab1);


        TabHost.TabSpec tab2 = tabHost.newTabSpec("Past Orders");
        tab2.setContent(R.id.tab2);
        tab2.setIndicator("Đã đi");
        tabHost.addTab(tab2);

        TabHost.TabSpec tab3 = tabHost.newTabSpec("Favorites");
        tab3.setContent(R.id.tab3);
        tab3.setIndicator("Đã hủy");
        tabHost.addTab(tab3);

    }

    public void onOrderCanceled(Order order, int position) {
        orderCurrentList.remove(position);
        orderCurrentAdapter.notifyDataSetChanged();

        orderCancelList.add(order);
        orderCancelAdapter.notifyDataSetChanged();
    }
}
