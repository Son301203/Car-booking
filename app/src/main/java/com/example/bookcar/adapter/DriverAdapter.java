package com.example.bookcar.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookcar.R;
import com.example.bookcar.model.Driver;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class DriverAdapter extends ArrayAdapter<Driver> {
    private Activity context;
    private int idLayout;
    private ArrayList<Driver> driverList;
    private FirebaseFirestore db;
    private OnDriverActionListener listener;

    public interface OnDriverActionListener {
        void onEditDriver(Driver driver, int position);
        void onDriverDeleted(int position);
    }

    public DriverAdapter(Activity context, int idLayout, ArrayList<Driver> driverList, OnDriverActionListener listener) {
        super(context, idLayout, driverList);
        this.context = context;
        this.idLayout = idLayout;
        this.driverList = driverList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(idLayout, parent, false);
        }

        Driver driver = driverList.get(position);

        TextView txtName = convertView.findViewById(R.id.txtDriverName);
        txtName.setText("Tên: " + driver.getName());

        TextView txtPhone = convertView.findViewById(R.id.txtDriverPhone);
        txtPhone.setText("SĐT: " + driver.getPhone());

        TextView txtLicense = convertView.findViewById(R.id.txtDriverLicense);
        txtLicense.setText("Bằng lái: " + driver.getLicense());

        TextView txtEmail = convertView.findViewById(R.id.txtDriverEmail);
        txtEmail.setText("Email: " + driver.getEmail());

        // Edit button
        ImageView btnEdit = convertView.findViewById(R.id.btnEditDriver);
        btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditDriver(driver, position);
            }
        });

        // Delete button
        ImageView btnDelete = convertView.findViewById(R.id.btnDeleteDriver);
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa tài xế")
                    .setMessage("Bạn chắc chắn muốn xóa tài xế " + driver.getName() + "?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        deleteDriver(driver, position);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        return convertView;
    }

    private void deleteDriver(Driver driver, int position) {
        db.collection("users").document(driver.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Xóa tài xế thành công", Toast.LENGTH_SHORT).show();
                    driverList.remove(position);
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onDriverDeleted(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Xóa tài xế thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

