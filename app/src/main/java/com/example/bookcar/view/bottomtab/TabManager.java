package com.example.bookcar.view.bottomtab;

import android.content.Context;
import android.content.Intent;
import android.widget.LinearLayout;

import com.example.bookcar.view.AccountActivity;
import com.example.bookcar.view.clients.HomeActivity;
import com.example.bookcar.view.NotificationsActivity;
import com.example.bookcar.view.clients.OrderActivity;
import com.example.bookcar.view.drivers.CalenderDriverActivity;
import com.example.bookcar.view.drivers.HomeDriversActivity;


public class TabManager {
    private final Context context;

    public TabManager(Context context) {
        this.context = context;
    }

    public void setupTab(LinearLayout homeTab, LinearLayout orderTab, LinearLayout notifyTab, LinearLayout accountTab) {
        if(homeTab != null){
            homeTab.setOnClickListener(v -> {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }

        if(orderTab != null){
            orderTab.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }

        if(notifyTab != null){
            notifyTab.setOnClickListener(v -> {
                Intent intent = new Intent(context, NotificationsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }

        if(accountTab != null){
            accountTab.setOnClickListener(v -> {
                Intent intent = new Intent(context, AccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }
    }

    public void setupTabDriver(LinearLayout tripTabDriver, LinearLayout calenderTabDriver, LinearLayout notifyTabDriver,  LinearLayout accountDriver){
        if(tripTabDriver != null){
            tripTabDriver.setOnClickListener(v -> {
                Intent intent = new Intent(context, HomeDriversActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }

        if(calenderTabDriver != null){
            calenderTabDriver.setOnClickListener(v -> {
                Intent intent = new Intent(context, CalenderDriverActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }

        if(notifyTabDriver != null){
            notifyTabDriver.setOnClickListener(v -> {
                Intent intent = new Intent(context, NotificationsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }

        if(accountDriver != null){
            accountDriver.setOnClickListener(v -> {
                Intent intent = new Intent(context, AccountActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }

    }
}
