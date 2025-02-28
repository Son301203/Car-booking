package com.example.bookcar.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChangeInfoActivity extends AppCompatActivity {
    private static final String TAG = "ChangeInfoActivity";

    private TextView dateOfBirthTextView;
    private EditText nameEditText, phoneNumberEditText;
    private RadioButton maleCheckBox, femaleCheckBox;
    private Button updateButton;
    private Calendar calendar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private UserRole userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_change_info), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        ImageView backIcon = findViewById(R.id.backIcon);
        nameEditText = findViewById(R.id.fullNameEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        dateOfBirthTextView = findViewById(R.id.dateOfBirthTextView);
        maleCheckBox = findViewById(R.id.maleCheckBox);
        femaleCheckBox = findViewById(R.id.femaleCheckBox);
        updateButton = findViewById(R.id.updateBtn);

        backIcon.setOnClickListener(view -> finish());
        dateOfBirthTextView.setOnClickListener(v -> showDatePickerDialog(dateOfBirthTextView));
        updateButton.setOnClickListener(view -> updateUserInfo());

        determineUserRoleAndFetchInfo();
    }

    private void showDatePickerDialog(TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    textView.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void determineUserRoleAndFetchInfo() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("drivers")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        userRole = new DriverRole();
                    } else {
                        userRole = new ClientRole();
                    }
                    fetchUserInfo(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi kiểm tra vai trò: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserInfo(String userId) {
        db.collection(userRole.getCollectionName())
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = userRole.getDataFromSnapshot(documentSnapshot);
                        nameEditText.setText((String) data.get("name"));
                        phoneNumberEditText.setText((String) data.get("phone"));
                        dateOfBirthTextView.setText((String) data.get("dateOfBirth"));

                        String gender = (String) data.get("gender");
                        if ("male".equalsIgnoreCase(gender) || "Male".equals(gender)) {
                            maleCheckBox.setChecked(true);
                        } else if ("female".equalsIgnoreCase(gender) || "Female".equals(gender)) {
                            femaleCheckBox.setChecked(true);
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi lấy dữ liệu: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInfo() {
        String userId = auth.getCurrentUser().getUid();

        String name = nameEditText.getText().toString().trim();
        String phone = phoneNumberEditText.getText().toString().trim();
        String dateOfBirth = dateOfBirthTextView.getText().toString();
        String gender = maleCheckBox.isChecked() ? "Male" : (femaleCheckBox.isChecked() ? "Female" : "");

        Map<String, Object> updates = userRole.getUpdates(name, phone, dateOfBirth, gender);

        db.collection(userRole.getCollectionName())
                .document(userId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    String roleText = userRole instanceof DriverRole ? "tài xế" : "người dùng";
                    Toast.makeText(this, "Thông tin " + roleText + " được cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, AccountActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật thông tin: " + e.getMessage());
                    Toast.makeText(this, "Lỗi cập nhật thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}