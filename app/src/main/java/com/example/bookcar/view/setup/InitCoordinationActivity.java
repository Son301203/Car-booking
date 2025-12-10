package com.example.bookcar.view.setup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookcar.R;
import com.example.bookcar.utils.CoordinationInitializer;

/**
 * Activity to initialize coordination role and account
 * This should only be run ONCE during initial setup
 * After creating the coordination account, you can remove this activity
 */
public class InitCoordinationActivity extends AppCompatActivity {
    private Button btnInitialize;
    private ProgressBar progressBar;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_coordination);

        btnInitialize = findViewById(R.id.btnInitializeCoordination);
        progressBar = findViewById(R.id.progressBarInit);
        txtStatus = findViewById(R.id.txtInitStatus);

        btnInitialize.setOnClickListener(v -> initializeCoordination());
    }

    private void initializeCoordination() {
        new AlertDialog.Builder(this)
                .setTitle("Khởi tạo tài khoản Coordination")
                .setMessage("Bạn chỉ cần thực hiện thao tác này MỘT LẦN duy nhất.\n\n" +
                        "Tài khoản mặc định:\n" +
                        "Email: coordination@bookcar.com\n" +
                        "Password: Coordination@2024\n\n" +
                        "Tiếp tục?")
                .setPositiveButton("Khởi tạo", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    btnInitialize.setEnabled(false);
                    txtStatus.setText("Đang khởi tạo...");

                    CoordinationInitializer.initializeCoordination(new CoordinationInitializer.InitializationCallback() {
                        @Override
                        public void onSuccess(String message) {
                            progressBar.setVisibility(View.GONE);
                            txtStatus.setText("✅ Thành công!\n\n" + message);

                            new AlertDialog.Builder(InitCoordinationActivity.this)
                                    .setTitle("Thành công")
                                    .setMessage(message + "\n\nHãy đăng nhập với tài khoản này.")
                                    .setPositiveButton("OK", (d, w) -> finish())
                                    .show();
                        }

                        @Override
                        public void onFailure(String error) {
                            progressBar.setVisibility(View.GONE);
                            btnInitialize.setEnabled(true);
                            txtStatus.setText("❌ Lỗi: " + error);

                            new AlertDialog.Builder(InitCoordinationActivity.this)
                                    .setTitle("Lỗi")
                                    .setMessage(error)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}

