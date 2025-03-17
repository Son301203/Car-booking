package com.example.bookcar.view;

import static com.example.bookcar.view.bottomtab.TabUtils.setupTabClientUI;
import static com.example.bookcar.view.bottomtab.TabUtils.setupTabDriverUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.model.roles.ClientRole;
import com.example.bookcar.model.roles.DriverRole;
import com.example.bookcar.model.roles.UserRole;
import com.example.bookcar.view.bottomtab.TabManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserRole userRole;

    private ImageView btnLogout;
    private TextView profileUsername, changeInfo, changePassword;
    private View bottomNavigation, bottomNavigationDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_account), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogout = findViewById(R.id.logoutBtn);
        profileUsername = findViewById(R.id.profileUsername);
        changeInfo = findViewById(R.id.changeInfo);
        changePassword = findViewById(R.id.changePassword);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigationDriver = findViewById(R.id.bottomNavigationDriver);


        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        changeInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeInfoActivity.class);
            startActivity(intent);
        });

        changePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        determineUserRoleAndFetchInfo();
    }

    private void determineUserRoleAndFetchInfo() {
        String userId = mAuth.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("drivers")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        userRole = new DriverRole();
                        Log.d(TAG, "Xác định vai trò: Tài xế");
                        setupTabDriverUI(this);
                        bottomNavigation.setVisibility(View.GONE);
                        bottomNavigationDriver.setVisibility(View.VISIBLE);
                    } else {
                        userRole = new ClientRole();
                        Log.d(TAG, "Xác định vai trò: Khách hàng");
                        setupTabClientUI(this);
                        bottomNavigation.setVisibility(View.VISIBLE);
                        bottomNavigationDriver.setVisibility(View.GONE);
                    }
                    fetchAccountInfo(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi kiểm tra vai trò: " + e.getMessage());
                    profileUsername.setText("Lỗi tải tên");
                });
    }

    private void fetchAccountInfo(String userId) {
        db.collection(userRole.getCollectionName())
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = userRole.getDataFromSnapshot(documentSnapshot);
                        String name = (String) data.get("name");
                        Log.d(TAG, "Tên: " + name);
                        profileUsername.setText(name != null ? name : "Không có tên");
                    } else {
                        Log.d(TAG, "Không tìm thấy thông tin trong " + userRole.getCollectionName());
                        profileUsername.setText("Không có tên");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lấy thông tin: " + e.getMessage());
                    profileUsername.setText("Lỗi tải tên");
                });
    }
}