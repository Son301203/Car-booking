package com.example.bookcar.view.coordination;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bookcar.R;
import com.example.bookcar.adapter.DriverAdapter;
import com.example.bookcar.adapter.DriverManagementPagerAdapter;
import com.example.bookcar.model.Driver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ManageDriverActivity extends AppCompatActivity implements DriverAdapter.OnDriverActionListener {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAddDriver;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<Driver> driverList;
    private DriverAdapter driverAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_driver);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews();

        // Load drivers
        loadDrivers();

        // FAB click listener (only show on driver list tab)
        fabAddDriver.setOnClickListener(v -> showAddDriverDialog());

        // Handle FAB visibility based on tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Show FAB only on Driver List tab (position 0)
                if (position == 0) {
                    fabAddDriver.show();
                } else {
                    fabAddDriver.hide();
                }
            }
        });
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabAddDriver = findViewById(R.id.fabAddDriver);

        // Setup driver list and adapter
        driverList = new ArrayList<>();
        driverAdapter = new DriverAdapter(this, R.layout.item_driver, driverList, this);

        // Setup ViewPager2 with adapter
        DriverManagementPagerAdapter pagerAdapter = new DriverManagementPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Danh sách tài xế");
                        break;
                    case 1:
                        tab.setText("Xếp khách hàng");
                        break;
                }
            }
        }).attach();
    }

    public DriverAdapter getDriverAdapter() {
        return driverAdapter;
    }

    private void showAddDriverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_driver, null);
        builder.setView(dialogView);

        // Initialize dialog views
        EditText edtDialogName = dialogView.findViewById(R.id.edtDialogDriverName);
        EditText edtDialogPhone = dialogView.findViewById(R.id.edtDialogDriverPhone);
        EditText edtDialogLicense = dialogView.findViewById(R.id.edtDialogDriverLicense);
        EditText edtDialogIdentification = dialogView.findViewById(R.id.edtDialogDriverIdentification);
        EditText edtDialogDateOfBirth = dialogView.findViewById(R.id.edtDialogDriverDateOfBirth);
        EditText edtDialogEmail = dialogView.findViewById(R.id.edtDialogDriverEmail);
        EditText edtDialogPassword = dialogView.findViewById(R.id.edtDialogDriverPassword);
        Spinner spinnerDialogGender = dialogView.findViewById(R.id.spinnerDialogDriverGender);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmAddDriver);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelAddDriver);
        ProgressBar progressBarDialog = dialogView.findViewById(R.id.progressBarDialog);

        // Setup gender spinner
        String[] genders = {"Chọn giới tính", "Nam", "Nữ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDialogGender.setAdapter(adapter);

        // Setup date picker
        edtDialogDateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, month1, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                        edtDialogDateOfBirth.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        // Confirm button click
        btnConfirm.setOnClickListener(v -> {
            String name = edtDialogName.getText().toString().trim();
            String phone = edtDialogPhone.getText().toString().trim();
            String license = edtDialogLicense.getText().toString().trim();
            String identification = edtDialogIdentification.getText().toString().trim();
            String dateOfBirth = edtDialogDateOfBirth.getText().toString().trim();
            String email = edtDialogEmail.getText().toString().trim();
            String password = edtDialogPassword.getText().toString().trim();
            String gender = spinnerDialogGender.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(license) ||
                    TextUtils.isEmpty(identification) || TextUtils.isEmpty(dateOfBirth) ||
                    TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || gender.equals("Chọn giới tính")) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBarDialog.setVisibility(View.VISIBLE);
            btnConfirm.setEnabled(false);

            checkAndCreateDriverRole(name, phone, license, identification, dateOfBirth, email, password, gender,
                    progressBarDialog, btnConfirm, dialog);
        });

        // Cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void checkAndCreateDriverRole(String name, String phone, String license,
                                          String identification, String dateOfBirth,
                                          String email, String password, String gender,
                                          ProgressBar progressBar, Button btnConfirm, AlertDialog dialog) {
        db.collection("roles")
                .whereEqualTo("name", "drivers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Role "drivers" already exists, get its ID
                        String roleId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        createDriverAccount(name, phone, license, identification, dateOfBirth,
                                email, password, gender, roleId, progressBar, btnConfirm, dialog);
                    } else {
                        // Role "drivers" doesn't exist, create it first
                        createDriversRole(name, phone, license, identification, dateOfBirth,
                                email, password, gender, progressBar, btnConfirm, dialog);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnConfirm.setEnabled(true);
                    Toast.makeText(this, "Lỗi khi kiểm tra role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createDriversRole(String name, String phone, String license,
                                   String identification, String dateOfBirth,
                                   String email, String password, String gender,
                                   ProgressBar progressBar, Button btnConfirm, AlertDialog dialog) {
        Map<String, Object> driversRole = new HashMap<>();
        driversRole.put("name", "drivers");
        driversRole.put("permissions", new HashMap<>());

        db.collection("roles")
                .add(driversRole)
                .addOnSuccessListener(documentReference -> {
                    String roleId = documentReference.getId();
                    createDriverAccount(name, phone, license, identification, dateOfBirth,
                            email, password, gender, roleId, progressBar, btnConfirm, dialog);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnConfirm.setEnabled(true);
                    Toast.makeText(this, "Lỗi khi tạo role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createDriverAccount(String name, String phone, String license,
                                     String identification, String dateOfBirth,
                                     String email, String password, String gender, String roleId,
                                     ProgressBar progressBar, Button btnConfirm, AlertDialog dialog) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String userId = mAuth.getCurrentUser().getUid();
                        saveDriverToFirestore(userId, name, phone, license, identification,
                                dateOfBirth, email, password, gender, roleId, progressBar, btnConfirm, dialog);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnConfirm.setEnabled(true);
                        Toast.makeText(this, "Tạo tài khoản thất bại: " +
                                (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveDriverToFirestore(String userId, String name, String phone, String license,
                                       String identification, String dateOfBirth, String email,
                                       String password, String gender, String roleId,
                                       ProgressBar progressBar, Button btnConfirm, AlertDialog dialog) {
        Map<String, Object> driver = new HashMap<>();
        driver.put("name", name);
        driver.put("phone", phone);
        driver.put("license", license);
        driver.put("identification", identification);
        driver.put("date_of_birth", dateOfBirth);
        driver.put("email", email);
        driver.put("password", password);
        driver.put("gender", gender);
        driver.put("role_id", roleId);
        driver.put("created_at", com.google.firebase.Timestamp.now());

        db.collection("users").document(userId)
                .set(driver)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnConfirm.setEnabled(true);
                    Toast.makeText(this, "Thêm tài xế thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadDrivers();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnConfirm.setEnabled(true);
                    Toast.makeText(this, "Lỗi khi lưu tài xế: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadDrivers() {
        // First get the role_id for "drivers"
        db.collection("roles")
                .whereEqualTo("name", "drivers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String driversRoleId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Now query users with this role_id
                        db.collection("users")
                                .whereEqualTo("role_id", driversRoleId)
                                .get()
                                .addOnSuccessListener(userSnapshots -> {
                                    driverList.clear();
                                    for (QueryDocumentSnapshot document : userSnapshots) {
                                        Driver driver = Driver.fromFirestore(document);
                                        driverList.add(driver);
                                    }
                                    driverAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi khi tải danh sách tài xế: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Lỗi khi tải role: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onEditDriver(Driver driver, int position) {
        showEditDialog(driver, position);
    }

    @Override
    public void onDriverDeleted(int position) {
        // Already handled in adapter
    }

    private void showEditDialog(Driver driver, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_driver, null);
        builder.setView(dialogView);

        EditText edtEditName = dialogView.findViewById(R.id.edtEditDriverName);
        EditText edtEditPhone = dialogView.findViewById(R.id.edtEditDriverPhone);
        EditText edtEditLicense = dialogView.findViewById(R.id.edtEditDriverLicense);
        EditText edtEditIdentification = dialogView.findViewById(R.id.edtEditDriverIdentification);
        EditText edtEditDateOfBirth = dialogView.findViewById(R.id.edtEditDriverDateOfBirth);
        EditText edtEditEmail = dialogView.findViewById(R.id.edtEditDriverEmail);
        Spinner spinnerEditGender = dialogView.findViewById(R.id.spinnerEditDriverGender);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdateDriver);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelEdit);

        // Setup gender spinner
        String[] genders = {"Nam", "Nữ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditGender.setAdapter(adapter);

        // Fill current data
        edtEditName.setText(driver.getName());
        edtEditPhone.setText(driver.getPhone());
        edtEditLicense.setText(driver.getLicense());
        edtEditIdentification.setText(driver.getIdentification());
        edtEditDateOfBirth.setText(driver.getDateOfBirth());
        edtEditEmail.setText(driver.getEmail());
        spinnerEditGender.setSelection(driver.getGender().equals("Nam") ? 0 : 1);

        // Setup date picker for edit
        edtEditDateOfBirth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year1, month1, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                        edtEditDateOfBirth.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        btnUpdate.setOnClickListener(v -> {
            String name = edtEditName.getText().toString().trim();
            String phone = edtEditPhone.getText().toString().trim();
            String license = edtEditLicense.getText().toString().trim();
            String identification = edtEditIdentification.getText().toString().trim();
            String dateOfBirth = edtEditDateOfBirth.getText().toString().trim();
            String gender = spinnerEditGender.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(license) ||
                    TextUtils.isEmpty(identification) || TextUtils.isEmpty(dateOfBirth)) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            updateDriver(driver.getDocumentId(), name, phone, license, identification,
                    dateOfBirth, gender);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateDriver(String driverId, String name, String phone, String license,
                             String identification, String dateOfBirth, String gender) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("license", license);
        updates.put("identification", identification);
        updates.put("date_of_birth", dateOfBirth);
        updates.put("gender", gender);

        db.collection("users").document(driverId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật tài xế thành công", Toast.LENGTH_SHORT).show();
                    loadDrivers();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}

