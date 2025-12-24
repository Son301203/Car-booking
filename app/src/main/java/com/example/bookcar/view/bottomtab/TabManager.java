package com.example.bookcar.view.bottomtab;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.bookcar.view.animations.TabAnimation;
import com.example.bookcar.view.AccountActivity;
import com.example.bookcar.view.clients.HomeActivity;
import com.example.bookcar.view.NotificationsActivity;
import com.example.bookcar.view.clients.OrderActivity;
import com.example.bookcar.view.drivers.CalenderDriverActivity;
import com.example.bookcar.view.drivers.HomeDriversActivity;

import java.util.Arrays;
import java.util.List;

public class TabManager {
    private final Context context;
    private static final String TAG = "TabManager";

    public TabManager(Context context) {
        this.context = context;
    }

    private String getCurrentTabId(boolean isDriverMode) {
        if (isDriverMode) {
            if (context instanceof HomeDriversActivity) return "trip";
            if (context instanceof CalenderDriverActivity) return "calender";
            if (context instanceof NotificationsActivity) return "notify_driver";
            if (context instanceof AccountActivity) return "account_driver";
            return "trip";
        } else {
            if (context instanceof HomeActivity) return "home";
            if (context instanceof OrderActivity) return "order";
            if (context instanceof NotificationsActivity) return "notify";
            if (context instanceof AccountActivity) return "account";
            return "home";
        }
    }

    public void setupTab(LinearLayout homeTab, LinearLayout orderTab, LinearLayout notifyTab, LinearLayout accountTab) {
        List<LinearLayout> allTabs = Arrays.asList(homeTab, orderTab, notifyTab, accountTab);

        if (homeTab != null) homeTab.setTag("home");
        if (orderTab != null) orderTab.setTag("order");
        if (notifyTab != null) notifyTab.setTag("notify");
        if (accountTab != null) accountTab.setTag("account");

        if (homeTab != null) {
            homeTab.setOnClickListener(v -> {
                TabAnimation.animateTab(homeTab, allTabs);
                if (!(context instanceof HomeActivity)) {
                    Intent intent = new Intent(context, HomeActivity.class);
                    context.startActivity(intent);
                }
            });
        }

        if (orderTab != null) {
            orderTab.setOnClickListener(v -> {
                TabAnimation.animateTab(orderTab, allTabs);
                if (!(context instanceof OrderActivity)) {
                    Intent intent = new Intent(context, OrderActivity.class);
                    context.startActivity(intent);
                }
            });
        }

        if (notifyTab != null) {
            notifyTab.setOnClickListener(v -> {
                TabAnimation.animateTab(notifyTab, allTabs);
                if (!(context instanceof NotificationsActivity)) {
                    Intent intent = new Intent(context, NotificationsActivity.class);
                    intent.putExtra("isDriverMode", false);
                    context.startActivity(intent);
                }
            });
        }

        if (accountTab != null) {
            accountTab.setOnClickListener(v -> {
                TabAnimation.animateTab(accountTab, allTabs);
                if (!(context instanceof AccountActivity)) {
                    Intent intent = new Intent(context, AccountActivity.class);
                    intent.putExtra("isDriverMode", false);
                    context.startActivity(intent);
                }
            });
        }

        updateCurrentTab(allTabs, false);
    }

    public void setupTabDriver(LinearLayout tripTabDriver, LinearLayout calenderTabDriver, LinearLayout notifyTabDriver, LinearLayout accountDriver) {
        List<LinearLayout> allTabs = Arrays.asList(tripTabDriver, calenderTabDriver, notifyTabDriver, accountDriver);

        if (tripTabDriver != null) tripTabDriver.setTag("trip");
        if (calenderTabDriver != null) calenderTabDriver.setTag("calender");
        if (notifyTabDriver != null) notifyTabDriver.setTag("notify_driver");
        if (accountDriver != null) accountDriver.setTag("account_driver");

        // Thiết lập listener
        if (tripTabDriver != null) {
            tripTabDriver.setOnClickListener(v -> {
                TabAnimation.animateTab(tripTabDriver, allTabs);
                if (!(context instanceof HomeDriversActivity)) {
                    Intent intent = new Intent(context, HomeDriversActivity.class);
                    context.startActivity(intent);
                }
            });
        }

        if (calenderTabDriver != null) {
            calenderTabDriver.setOnClickListener(v -> {
                TabAnimation.animateTab(calenderTabDriver, allTabs);
                if (!(context instanceof CalenderDriverActivity)) {
                    Intent intent = new Intent(context, CalenderDriverActivity.class);
                    context.startActivity(intent);
                }
            });
        }

        if (notifyTabDriver != null) {
            notifyTabDriver.setOnClickListener(v -> {
                TabAnimation.animateTab(notifyTabDriver, allTabs);
                if (!(context instanceof NotificationsActivity)) {
                    Intent intent = new Intent(context, NotificationsActivity.class);
                    intent.putExtra("isDriverMode", true);
                    context.startActivity(intent);
                }
            });
        }

        if (accountDriver != null) {
            accountDriver.setOnClickListener(v -> {
                TabAnimation.animateTab(accountDriver, allTabs);
                if (!(context instanceof AccountActivity)) {
                    Intent intent = new Intent(context, AccountActivity.class);
                    intent.putExtra("isDriverMode", true);
                    context.startActivity(intent);
                }
            });
        }

        updateCurrentTab(allTabs, true);
    }

    private void updateCurrentTab(List<LinearLayout> allTabs, boolean isDriverMode) {
        String currentTabId = getCurrentTabId(isDriverMode);
        LinearLayout currentTab = findTabById(allTabs, currentTabId);
        if (currentTab != null) {
            TabAnimation.animateTab(currentTab, allTabs);
        }
    }

    private LinearLayout findTabById(List<LinearLayout> tabs, String tabId) {
        for (LinearLayout tab : tabs) {
            if (tab != null && tabId.equals(tab.getTag())) {
                return tab;
            }
        }
        return null;
    }
}