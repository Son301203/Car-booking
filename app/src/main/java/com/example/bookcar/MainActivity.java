package com.example.bookcar;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.bookcar.view.LoginActivity;
import com.example.bookcar.view.clients.HomeActivity;
import com.example.bookcar.view.drivers.HomeDriversActivity;
import com.example.bookcar.view.setup.InitCoordinationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // OPTIONAL: Uncomment this to enable setup button for first-time coordination account creation
//         Button btnSetup = findViewById(R.id.btnSetupCoordination);
//         btnSetup.setVisibility(View.VISIBLE);
//         btnSetup.setOnClickListener(v -> {
//             startActivity(new Intent(this, InitCoordinationActivity.class));
//         });

        if(mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            checkUserRole(userId);
        }
        else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }


    private void checkUserRole(String uid) {
        // Check user's role from unified users collection
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String roleId = task.getResult().getString("role_id");

                        if (roleId == null || roleId.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Không tìm thấy role cho user", Toast.LENGTH_SHORT).show();
                            // Logout and go to login
                            mAuth.signOut();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                            return;
                        }

                        // Get role name from roles collection
                        db.collection("roles").document(roleId).get()
                                .addOnSuccessListener(roleDoc -> {
                                    if (roleDoc.exists()) {
                                        String roleName = roleDoc.getString("name");

                                        if ("coordination".equals(roleName)) {
                                            Log.d("MainActivity", "Coordination user detected, redirecting to ManageDriverActivity");
                                            startActivity(new Intent(this, com.example.bookcar.view.coordination.ManageDriverActivity.class));
                                            finish();
                                        } else if ("drivers".equals(roleName)) {
                                            Log.d("MainActivity", "Driver user detected, redirecting to HomeDriversActivity");
                                            startActivity(new Intent(this, HomeDriversActivity.class));
                                            finish();
                                        } else {
                                            Log.d("MainActivity", "Client user detected, redirecting to HomeActivity");
                                            startActivity(new Intent(this, HomeActivity.class));
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "Không tìm thấy role", Toast.LENGTH_SHORT).show();
                                        // Logout and go to login
                                        mAuth.signOut();
                                        startActivity(new Intent(this, LoginActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Lỗi khi kiểm tra role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("MainActivity", "Error checking role: " + e.getMessage());
                                    // Logout and go to login
                                    mAuth.signOut();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(MainActivity.this, "Không tìm thấy thông tin user", Toast.LENGTH_SHORT).show();
                        // Logout and go to login
                        mAuth.signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Lỗi khi tải thông tin user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("MainActivity", "Error loading user info: " + e.getMessage());
                    // Logout and go to login
                    mAuth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });
    }
}