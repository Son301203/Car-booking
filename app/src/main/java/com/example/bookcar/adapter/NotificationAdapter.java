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
    private Activity context;
    private ArrayList<Notification> listNotifications;

    public NotificationAdapter(Activity context, int idLayout, ArrayList<Notification> listNotifications) {
        super(context, idLayout, listNotifications);
        this.idLayout = idLayout;
        this.context = context;
        this.listNotifications = listNotifications;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(idLayout, parent, false);

            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.notification_title);
            holder.message = convertView.findViewById(R.id.notification_message);
            holder.image = convertView.findViewById(R.id.notification_image);
            holder.menuIcon = convertView.findViewById(R.id.menu_icon);
            holder.unreadBadge = convertView.findViewById(R.id.unread_badge);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification notify = listNotifications.get(position);
        if (notify != null) {
            // Thiết lập tiêu đề
            holder.title.setText(notify.getTitle());

            // Thiết lập nội dung
            holder.message.setText(notify.getMessage());

            // Thiết lập hình ảnh
            holder.image.setImageResource(notify.getImageResource());

            // Hiển thị badge nếu chưa đọc
            holder.unreadBadge.setVisibility(notify.isRead() ? View.GONE : View.VISIBLE);

            // (Tùy chọn) Xử lý nhấn vào menu icon nếu cần
            holder.menuIcon.setOnClickListener(v -> {
                // Ví dụ: Thêm logic cho menu (PopupMenu hoặc hành động khác)
            });
        }

        return convertView;
    }

    // ViewHolder để tối ưu hóa hiệu suất
    private static class ViewHolder {
        TextView title;
        TextView message;
        ImageView image;
        ImageView menuIcon;
        View unreadBadge;
    }
}