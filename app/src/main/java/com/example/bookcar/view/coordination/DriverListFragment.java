package com.example.bookcar.view.coordination;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookcar.R;

public class DriverListFragment extends Fragment {

    private ListView listViewDrivers;
    private ManageDriverActivity parentActivity;

    public static DriverListFragment newInstance() {
        return new DriverListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_list, container, false);

        listViewDrivers = view.findViewById(R.id.listViewDrivers);

        // Get parent activity
        if (getActivity() instanceof ManageDriverActivity) {
            parentActivity = (ManageDriverActivity) getActivity();
            // Set the adapter from parent activity
            listViewDrivers.setAdapter(parentActivity.getDriverAdapter());
        }

        return view;
    }

    public ListView getListView() {
        return listViewDrivers;
    }
}

