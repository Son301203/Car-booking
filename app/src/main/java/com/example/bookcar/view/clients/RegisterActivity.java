package com.example.bookcar.view.clients;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword, edtRe_password, edtUsername, edtPhoneNum;
    Button btnRegister;
    TextView loginLink;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        edtEmail = findViewById(R.id.emailRegisterEditText);
        edtPassword = findViewById(R.id.passwordRegisterEditText);
        edtRe_password = findViewById(R.id.rePasswordRegisterEditText);
        edtUsername = findViewById(R.id.usernameRegisterEditText);
        edtPhoneNum = findViewById(R.id.phoneNumRegisterEditText);
        btnRegister = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

    }

    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);

        String email, password, re_password, username, phone;
        username = edtUsername.getText().toString();
        phone = edtPhoneNum.getText().toString();
        email = edtEmail.getText().toString();
        password = edtPassword.getText().toString();
        re_password = edtRe_password.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(re_password) || TextUtils.isEmpty(phone)) {
            Toast.makeText(RegisterActivity.this, "Các trường không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(re_password)) {
            Toast.makeText(RegisterActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            saveUser(userId, username, phone, email, password);
                            finish();

                        } else {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUser(String userId, String username, String phone, String email, String password){
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("password", password);
        user.put("phone", phone);
        user.put("gender", "");
        user.put("date of birth", "");

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to Save User" + e.getMessage(), Toast.LENGTH_SHORT).show());

    }


}
