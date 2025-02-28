package com.example.bookcar.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.view.clients.HomeActivity;
import com.example.bookcar.view.clients.RegisterActivity;
import com.example.bookcar.view.drivers.HomeDriversActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText edtEmail, edtPassword;
    private TextView signupLink;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signupLink = findViewById(R.id.signupLink);
        loginButton = findViewById(R.id.loginButton);
        edtEmail = findViewById(R.id.emailEditText);
        edtPassword = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signupLink.setOnClickListener(view -> {
            Intent signupView = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(signupView);
        });

        loginButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Hãy nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            loginUser(email, password);
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        Log.d(TAG, "UID sau đăng nhập: " + uid);
                        checkUserRole(uid);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Đăng nhập thất bại: " + task.getException().getMessage());
                    }
                });
    }

    private void checkUserRole(String uid) {
        db.collection("drivers").document(uid).get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Log.d(TAG, "Tài xế được tìm thấy với UID: " + uid);
                        Toast.makeText(LoginActivity.this, "Đăng nhập tài xế thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeDriversActivity.class));
                        finish();
                    } else {
                        Log.d(TAG, "Không tìm thấy tài xế với UID: " + uid + " -> Xác định là khách hàng");
                        Toast.makeText(LoginActivity.this, "Đăng nhập khách hàng thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    }
                });
    }
}