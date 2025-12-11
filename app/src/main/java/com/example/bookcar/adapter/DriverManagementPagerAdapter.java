package com.example.bookcar.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.bookcar.view.coordination.ArrangeCustomersFragment;
import com.example.bookcar.view.coordination.DriverListFragment;

public class DriverManagementPagerAdapter extends FragmentStateAdapter {

    public DriverManagementPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return DriverListFragment.newInstance();
            case 1:
                return ArrangeCustomersFragment.newInstance();
            default:
                return DriverListFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Driver List and Arrange Customers
    }
}

