package com.example.bookcar.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookcar.R;
import com.example.bookcar.model.Notification;

import java.util.ArrayList;

public class NotificationAdapter extends ArrayAdapter<Notification> {
    private int idLayout;
    Activity context;
    ArrayList<Notification> listNotifications;

    public NotificationAdapter(Activity context, int idLayout, ArrayList<Notification> listNotifications) {
        super(context, idLayout, listNotifications);
        this.idLayout = idLayout;
        this.context = context;
        this.listNotifications = listNotifications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(idLayout, parent, false);
        }

        Notification notify = listNotifications.get(position);

        TextView title = convertView.findViewById(R.id.notification_title);
        title.setText(notify.getTitle());

        TextView message = convertView.findViewById(R.id.notification_message);
        message.setText(notify.getMessage());

        ImageView image = convertView.findViewById(R.id.notification_image);
        image.setImageResource(notify.getImageResource());

        return convertView;
    }
}