package com.example.bookcar.view.drivers;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookcar.R;
import com.example.bookcar.model.Trips;
import com.example.bookcar.view.bottomtab.TabUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CalenderDriverActivity extends AppCompatActivity {
    private Spinner scheduleSpinner;
    private GridLayout timetableGrid;
    private LinearLayout timeColumn;
    private LinearLayout dayRow;
    private ImageView backIcon;

    private ArrayList<Trips> trips = new ArrayList<>();
    private ArrayList<String> schedules = new ArrayList<>();
    private Calendar selectedWeekStart;
    private Calendar selectedWeekEnd;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender_driver);

        scheduleSpinner = findViewById(R.id.scheduleSpinner);
        timetableGrid = findViewById(R.id.timetableGrid);
        timeColumn = findViewById(R.id.timeColumn);
        dayRow = findViewById(R.id.dayRow);
        backIcon = findViewById(R.id.backIcon);

        // initial firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // spinner
        setupScheduleSpinner();

        // timetable
        setupTimetable();

        // time column
        setupTimeColumn();

        // date row
        setupDayRow();

        // fetch trip
        fetchTrip();

        // back
        backIcon.setOnClickListener(v -> finish());
    }

    // set up week
    private void setupScheduleSpinner() {
        schedules.clear();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (int i = 0; i < 4; i++) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            String startDate = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_WEEK, 6);
            String endDate = dateFormat.format(calendar.getTime());
            schedules.add("Tuần: " + startDate + " - " + endDate);
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                schedules
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);

        // pick week
        scheduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                updateSelectedWeek(position);
                setupDayRow();
                updateTimetableWithTrips();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        updateSelectedWeek(0);
    }

    // update date of week
    private void updateSelectedWeek(int position) {
        String selectedSchedule = schedules.get(position);
        String startDateStr = selectedSchedule.split(": ")[1].split(" - ")[0];
        String endDateStr = selectedSchedule.split(": ")[1].split(" - ")[1];
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedWeekStart = Calendar.getInstance();
        selectedWeekEnd = Calendar.getInstance();
        try {
            selectedWeekStart.setTime(dateFormat.parse(startDateStr));
            selectedWeekEnd.setTime(dateFormat.parse(endDateStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // set up time table
    private void setupTimetable() {
        for (int i = 0; i < 21 * 7; i++) {
            TextView cell = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(i / 7),
                    GridLayout.spec(i % 7)
            );
            params.width = 100;
            params.height = 80;
            params.setMargins(1, 1, 1, 1);
            cell.setLayoutParams(params);
            cell.setBackgroundColor(0xFFEEEEEE);
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(12);
            cell.setText("");
            timetableGrid.addView(cell);
        }

        updateTimetableWithTrips();
    }

    //set up hour column
    private void setupTimeColumn() {
        for (int i = 3; i <= 23; i++) {
            TextView timeText = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    82
            );
            timeText.setLayoutParams(params);
            timeText.setGravity(Gravity.CENTER);
            timeText.setText(String.format("%02d:00", i));
            timeText.setBackgroundColor(0xFFE0E0E0);
            timeText.setPadding(4, 0, 4, 0);
            timeColumn.addView(timeText);
        }
    }

    // set up day row
    private void setupDayRow() {
        dayRow.removeAllViews();
        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        Calendar tempCalendar = (Calendar) selectedWeekStart.clone();

        for (int i = 0; i < 7; i++) {
            LinearLayout dayContainer = new LinearLayout(this);
            dayContainer.setOrientation(LinearLayout.VERTICAL);
            dayContainer.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    100,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            containerParams.setMargins(1, 0, 1, 0);
            dayContainer.setLayoutParams(containerParams);
            dayContainer.setBackgroundColor(0xFFCCCCCC);

            TextView dayText = new TextView(this);
            dayText.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            dayText.setGravity(Gravity.CENTER);
            dayText.setText(days[i]);
            dayContainer.addView(dayText);

            TextView dateText = new TextView(this);
            dateText.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            dateText.setGravity(Gravity.CENTER);
            dateText.setTextSize(12);
            String dateStr = dateFormat.format(tempCalendar.getTime());
            dateText.setText(dateStr);
            dayContainer.addView(dateText);

            dayRow.addView(dayContainer);

            tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // add and display trip into timetable
    private void fetchTrip() {
        String currentDriverId = mAuth.getCurrentUser().getUid();

        CollectionReference tripsRef = db.collection("drivers")
                                .document(currentDriverId)
                                .collection("trips");

        tripsRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null){
                for (QueryDocumentSnapshot doc : task.getResult()){
                    String dateTrip = doc.getString("dateTrip");
                    String startTime = doc.getString("startTime");

                    addTripIntoTimeTable("Chuyến đi tài xế", dateTrip, startTime);
                }
                updateTimetableWithTrips();
            }
            else {
                Log.d("fetchTrip", "Error when fetching trips");
            }
        });
    }

    // add trip
    private void addTripIntoTimeTable(String description, String dateStr, String hour) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Calendar tripCalendar = Calendar.getInstance();
            tripCalendar.setTime(dateFormat.parse(dateStr));

            int dayOfWeek = (tripCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7;

            trips.add(new Trips(description, dayOfWeek, hour, dateStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // update Timetable when have trip
    private void updateTimetableWithTrips() {
        for (int i = 0; i < timetableGrid.getChildCount(); i++) {
            TextView cell = (TextView) timetableGrid.getChildAt(i);
            cell.setText("");
            cell.setBackgroundColor(0xFFEEEEEE);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (Trips trip : trips) {
            try {
                Calendar tripCal = Calendar.getInstance();
                tripCal.setTime(dateFormat.parse(trip.getDateTrips()));
                if (tripCal.compareTo(selectedWeekStart) >= 0 && tripCal.compareTo(selectedWeekEnd) <= 0) {
                    int hourInt = Integer.parseInt(trip.getTimeTrips().split(":")[0]);
                    int row = hourInt - 3;
                    int column = trip.getDayOfWeek();
                    int index = row * 7 + column;
                    if (index >= 0 && index < timetableGrid.getChildCount()) {
                        TextView cell = (TextView) timetableGrid.getChildAt(index);
                        cell.setText(trip.getDescription());
                        cell.setBackgroundColor(0xFFFFA500);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}