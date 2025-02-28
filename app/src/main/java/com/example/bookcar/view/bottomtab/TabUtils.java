package com.example.bookcar.view.bottomtab;


import android.app.Activity;
import android.widget.LinearLayout;

import com.example.bookcar.R;

public class TabUtils {
    public static void setupTabClientUI(Activity activity) {
        LinearLayout homeTab = activity.findViewById(R.id.home_tab);
        LinearLayout orderTab = activity.findViewById(R.id.order_tab);
        LinearLayout notifyTab = activity.findViewById(R.id.notification_tab);
        LinearLayout accountTab = activity.findViewById(R.id.account_tab);
        TabManager tabManager = new TabManager(activity);
        tabManager.setupTab(homeTab, orderTab, notifyTab, accountTab);
    }

    public static void setupTabDriverUI(Activity activity) {
        LinearLayout tripTabDriver = activity.findViewById(R.id.trips_tab_driver);
        LinearLayout calenderTabDriver = activity.findViewById(R.id.calender_tab_driver);
        LinearLayout notifyTabDriver = activity.findViewById(R.id.notification_driver);
        LinearLayout accountDriver = activity.findViewById(R.id.account_driver);
        TabManager tabManager = new TabManager(activity);
        tabManager.setupTabDriver(tripTabDriver, calenderTabDriver, notifyTabDriver, accountDriver);
    }
}
