package com.example.bookcar.view.bottomtab;


import android.app.Activity;
import android.widget.LinearLayout;

import com.example.bookcar.R;

public class TabUtils {
    public static void setupTabs(Activity activity) {
        // Tìm các LinearLayout của từng tab
        LinearLayout homeTab = activity.findViewById(R.id.home_tab);
        LinearLayout orderTab = activity.findViewById(R.id.order_tab);
        LinearLayout notifyTab = activity.findViewById(R.id.notification_tab);
        LinearLayout accountTab = activity.findViewById(R.id.account_tab);



        // Khởi tạo TabManager
        TabManager tabManager = new TabManager(activity);

        // Kiểm tra null trước khi gọi setupTab
        tabManager.setupTab(homeTab, orderTab, notifyTab, accountTab);
    }
}
