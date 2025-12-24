package com.example.bookcar.view;

import static com.example.bookcar.view.bottomtab.TabUtils.setupTabClientUI;
import static com.example.bookcar.view.bottomtab.TabUtils.setupTabDriverUI;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.adapter.NotificationAdapter;
import com.example.bookcar.model.Notification;
import com.example.bookcar.model.roles.ClientRole;
import com.example.bookcar.model.roles.DriverRole;
import com.example.bookcar.model.roles.UserRole;
import com.example.bookcar.view.animations.FadeIn;
import com.example.bookcar.view.bottomtab.TabUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {
    private ListView listViewNotification;
    private NotificationAdapter notificationAdapter;
    private ArrayList<Notification> notificationList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserRole userRole;
    private boolean isDriverMode = false;

    private View bottomNavigation, bottomNavigationDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get driver mode flag from Intent
        isDriverMode = getIntent().getBooleanExtra("isDriverMode", false);
        Log.d("NotificationsActivity", "isDriverMode: " + isDriverMode);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigationDriver = findViewById(R.id.bottomNavigationDriver);

        listViewNotification = findViewById(R.id.lvNotification);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, R.layout.layout_listview_notification, notificationList);
        listViewNotification.setAdapter(notificationAdapter);

        FadeIn fadeIn = new FadeIn(this);
        fadeIn.fadeIn(listViewNotification);

        determineUserRole();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Always setup UI based on isDriverMode flag, regardless of userRole
        if (isDriverMode) {
            TabUtils.setupTabDriverUI(this);
            bottomNavigation.setVisibility(View.GONE);
            bottomNavigationDriver.setVisibility(View.VISIBLE);
        } else {
            TabUtils.setupTabClientUI(this);
            bottomNavigation.setVisibility(View.VISIBLE);
            bottomNavigationDriver.setVisibility(View.GONE);
        }
    }

    private void determineUserRole() {
        String userId = mAuth.getCurrentUser().getUid();

        // Check user's role from unified users collection
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String roleId = task.getResult().getString("role_id");
                        Log.d("NotificationsActivity", "User role_id: " + roleId);

                        if ("driver".equals(roleId)) {
                            userRole = new DriverRole();
                        } else {
                            userRole = new ClientRole();
                        }

                        // Setup UI based on isDriverMode flag
                        Log.d("NotificationsActivity", "Setting up UI with isDriverMode: " + isDriverMode);
                        if (isDriverMode) {
                            setupTabDriverUI(this);
                            bottomNavigation.setVisibility(View.GONE);
                            bottomNavigationDriver.setVisibility(View.VISIBLE);
                            Log.d("NotificationsActivity", "Driver UI setup completed");
                        } else {
                            setupTabClientUI(this);
                            bottomNavigation.setVisibility(View.VISIBLE);
                            bottomNavigationDriver.setVisibility(View.GONE);
                            Log.d("NotificationsActivity", "Client UI setup completed");
                        }

                        fetchNotificationInfo(userId);
                    } else {
                        Log.d("role", "User not found");
                    }
                });
    }

    private void fetchNotificationInfo(String userId) {
        if (userRole == null) {
            Log.e("Notifications", "User role is null, cannot fetch notifications");
            return;
        }

        // Determine notification collection name based on role
        String notificationCollection = isDriverMode ? "drivers" : "users";

        Log.d("Notifications", "Fetching notifications from collection: " + notificationCollection + " for userId: " + userId);

        db.collection("notifications")
                .document(notificationCollection)
                .collection(notificationCollection)
                .document(userId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationList.clear();
                        Log.d("Notifications", "Successfully fetched " + task.getResult().size() + " notifications");
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String message = document.getString("message");
                            String imageName = document.getString("image");
                            Boolean read = document.getBoolean("read");

                            Notification notification = new Notification(title, message, imageName, read != null ? read : false);
                            notificationList.add(notification);
                            Log.d("Notifications", "Added notification: " + title);
                        }
                        notificationAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Notifications", "Error getting notifications: ", task.getException());
                    }
                });
    }
}