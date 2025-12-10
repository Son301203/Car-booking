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
        if (userRole instanceof DriverRole) {
            TabUtils.setupTabDriverUI(this);
            bottomNavigation.setVisibility(View.GONE);
            bottomNavigationDriver.setVisibility(View.VISIBLE);
        } else if (userRole instanceof ClientRole) {
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

                        if ("driver".equals(roleId)) {
                            userRole = new DriverRole();
                            setupTabDriverUI(this);
                            bottomNavigation.setVisibility(View.GONE);
                            bottomNavigationDriver.setVisibility(View.VISIBLE);
                        } else {
                            userRole = new ClientRole();
                            setupTabClientUI(this);
                            bottomNavigation.setVisibility(View.VISIBLE);
                            bottomNavigationDriver.setVisibility(View.GONE);
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

        db.collection("notifications")
                .document(userRole.getCollectionName())
                .collection(userRole.getCollectionName())
                .document(userId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String message = document.getString("message");
                            String imageName = document.getString("image");
                            Boolean read = document.getBoolean("read");

                            Notification notification = new Notification(title, message, imageName, read != null ? read : false);
                            notificationList.add(notification);
                        }
                        notificationAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("Notifications", "Error getting notifications: ", task.getException());
                    }
                });
    }
}