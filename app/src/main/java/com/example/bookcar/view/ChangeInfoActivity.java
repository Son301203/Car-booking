package com.example.bookcar.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChangeInfoActivity extends AppCompatActivity {

    private TextView dateOfBirthTextView;
    private EditText nameEditText, phoneNumberEditText;
    private RadioButton maleCheckBox, femaleCheckBox;
    private Button updateButton;
    private Calendar calendar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

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

        fetchUserInfo();

        updateButton.setOnClickListener(view -> updateUserInfo());
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

    private void fetchUserInfo() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("username");
                        String phoneNumber = documentSnapshot.getString("phone");
                        String dateOfBirth = documentSnapshot.getString("date of birth");
                        String gender = documentSnapshot.getString("gender");

                        nameEditText.setText(fullName);
                        phoneNumberEditText.setText(phoneNumber);
                        dateOfBirthTextView.setText(dateOfBirth);

                        if ("Male".equalsIgnoreCase(gender)) {
                            maleCheckBox.setChecked(true);
                        } else if ("Female".equalsIgnoreCase(gender)) {
                            femaleCheckBox.setChecked(true);
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        String userId = auth.getCurrentUser().getUid();
        String fullName = nameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String dateOfBirth = dateOfBirthTextView.getText().toString();
        String gender = "";

        if (maleCheckBox.isChecked()) gender = "Male";
        if (femaleCheckBox.isChecked()) gender = "Female";

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", fullName);
        updates.put("phone", phoneNumber);
        updates.put("date of birth", dateOfBirth);
        updates.put("gender", gender);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(
                        unused -> {
                            Toast.makeText(this, "Thông tin người dùng được cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, AccountActivity.class);
                            startActivity(intent);
                            finish();
                        })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}