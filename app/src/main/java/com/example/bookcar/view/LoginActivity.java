package com.example.bookcar.view;

import android.content.Intent;
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
import com.example.bookcar.view.clients.HomeActivity;
import com.example.bookcar.view.clients.RegisterActivity;
import com.example.bookcar.view.drivers.HomeDriversActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
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

        // UI Elements
        signupLink = findViewById(R.id.signupLink);
        loginButton = findViewById(R.id.loginButton);
        edtEmail = findViewById(R.id.emailEditText);
        edtPassword = findViewById(R.id.passwordEditText);
        progressBar = findViewById(R.id.progressBar);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // Handle Signup Click
        signupLink.setOnClickListener(view -> {
            Intent signupView = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(signupView);
        });

        // Login Button Click
        loginButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Hãy nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            db.collection("drivers")
                    .whereEqualTo("email", email)
                    .whereEqualTo("password", password)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Đăng nhập tài xế thành công", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeDriversActivity.class));
                            finish();
                        } else {
                            loginClient(email, password);
                        }
                    });
        });
    }

    private void loginClient(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            Intent homeView = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(homeView);
                        } else {
                            Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
