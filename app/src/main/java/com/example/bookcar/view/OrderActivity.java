package com.example.bookcar.view;

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
import com.example.bookcar.model.Order;
import com.example.bookcar.view.bottomtab.TabUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {
    private ListView listView;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> orderList;

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

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        listView = findViewById(R.id.lvCurrentOrder);
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, R.layout.layout_listview_state_current_order, orderList);
        listView.setAdapter(orderAdapter);

        // Load orders from Firestore
        loadOrders();
        setupTabSelector();
        TabUtils.setupTabs(this);
    }

    private void loadOrders() {
        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference ordersRef = db.collection("users").
                document(userId).
                collection("orders");

        ordersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                orderList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String pickup = document.getString("pickup");
                    String destination = document.getString("destination");
                    String departureDate = document.getString("departureDate");
                    String returnDate = document.getString("returnDate");

                    // Create Order object
                    Order order = new Order(pickup, destination, departureDate, returnDate);
                    orderList.add(order);
                }
                orderAdapter.notifyDataSetChanged();
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
        tab1.setIndicator("Current Orders");
        tabHost.addTab(tab1);


        TabHost.TabSpec tab2 = tabHost.newTabSpec("Past Orders");
        tab2.setContent(R.id.tab2);
        tab2.setIndicator("Past Orders");
        tabHost.addTab(tab2);

        TabHost.TabSpec tab3 = tabHost.newTabSpec("Favorites");
        tab3.setContent(R.id.tab3);
        tab3.setIndicator("Cancel Orders");
        tabHost.addTab(tab3);

    }

}
