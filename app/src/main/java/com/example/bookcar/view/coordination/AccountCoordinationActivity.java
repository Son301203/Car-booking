package com.example.bookcar.view.coordination;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.model.roles.CoordinationRole;
import com.example.bookcar.view.ChangeInfoActivity;
import com.example.bookcar.view.ChangePasswordActivity;
import com.example.bookcar.view.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class AccountCoordinationActivity extends AppCompatActivity {
    private static final String TAG = "AccountCoordination";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CoordinationRole coordinationRole;

    private ImageView btnLogout;
    private TextView profileUsername, changeInfo, changePassword, manageDrivers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_coordination);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_account_coordination), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        coordinationRole = new CoordinationRole();

        btnLogout = findViewById(R.id.logoutBtn);
        profileUsername = findViewById(R.id.profileUsername);
        changeInfo = findViewById(R.id.changeInfo);
        changePassword = findViewById(R.id.changePassword);
        manageDrivers = findViewById(R.id.manageDrivers);

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

        manageDrivers.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageDriverActivity.class);
            startActivity(intent);
        });

        fetchAccountInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAccountInfo();
    }

    private void fetchAccountInfo() {
        String userId = mAuth.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(coordinationRole.getCollectionName())
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = coordinationRole.getDataFromSnapshot(documentSnapshot);
                        String name = (String) data.get("name");
                        Log.d(TAG, "Tên: " + name);
                        profileUsername.setText(name != null ? name : "Điều phối viên");
                    } else {
                        Log.d(TAG, "Không tìm thấy thông tin trong " + coordinationRole.getCollectionName());
                        profileUsername.setText("Điều phối viên");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lấy thông tin: " + e.getMessage());
                    profileUsername.setText("Lỗi tải tên");
                });
    }
}

