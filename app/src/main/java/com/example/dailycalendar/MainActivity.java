package com.example.dailycalendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.widget.TimePicker;
import android.widget.CalendarView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private DatabaseHelper dbHelper;
    private TextView selectedDate;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitycalendar2); // Default layout

        eventList = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);

        // Check for layout extra and set the appropriate layout
        Intent intent = getIntent();
        String layout = intent.getStringExtra("layout");


        if ("dialog_add_event".equals(layout)) {
            setContentView(R.layout.dialog_add_event);
            showAddEventDialog();
        } else if ("item_event".equals(layout)) {
            setContentView(R.layout.activity_main);
            setupGoBackButtonInItemEvent();
        } else if ("activity_calendar".equals(layout)) {
            setContentView(R.layout.activity_calendar);
            setupCalendarSelection();
            setupGoBackButtonInActivityCalendar();
        } else {
            setContentView(R.layout.activity_main);
            setupMainActivityViews();
        }


    }

    private void setupMainActivityViews() {
        selectedDate = findViewById(R.id.selectedDate);
        currentDate = getIntent().getStringExtra("selectedDate");

        if (currentDate == null) {
            currentDate = "2024-08-25";  // Default date
        }
        selectedDate.setText(currentDate);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventList = dbHelper.getEventsByDate(currentDate);  // Load events from the database
        adapter = new EventAdapter(eventList);
        recyclerView.setAdapter(adapter);

        Button addButton = findViewById(R.id.btnAddEvent);
        addButton.setOnClickListener(v -> showAddEventDialog());

        selectedDate.setOnClickListener(v -> {
            Intent calendarIntent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(calendarIntent);
        });
    }

    private void setupCalendarSelection() {
        CalendarView calendarView = findViewById(R.id.calendarView);
        Button selectDateButton = findViewById(R.id.btnSelectDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            currentDate = year + "-" + (month + 1) + "-" + dayOfMonth;
        });

        selectDateButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.putExtra("layout", "activity_main");
            intent.putExtra("selectedDate", currentDate);
            startActivity(intent);
        });

        Button goBackHomeButton = findViewById(R.id.btn_back_home_1);
        goBackHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
    }

    private void showAddEventDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(view);

        final EditText eventTitle = view.findViewById(R.id.eventTitle);
        final EditText eventDescription = view.findViewById(R.id.eventDescription);
        final TimePicker timePicker = view.findViewById(R.id.timePicker);
        Button saveButton = view.findViewById(R.id.btnSave);
        Button backHomeButton = view.findViewById(R.id.btn_back_home_3);

        timePicker.setIs24HourView(true);

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String title = eventTitle.getText().toString().trim();
            String description = eventDescription.getText().toString().trim();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);

            if (!title.isEmpty()) {
                if (dbHelper.addEvent(title, description + " at " + time, currentDate)) {
                    Toast.makeText(this, "Event Added", Toast.LENGTH_SHORT).show();
                    eventList.clear();
                    eventList.addAll(dbHelper.getEventsByDate(currentDate));
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Error Adding Event", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            } else {
                eventTitle.setError("Title is required");
            }
        });

        backHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupGoBackButtonInItemEvent() {
        Button btnGoBackItemEvent = findViewById(R.id.btn_back_home_2);
        if (btnGoBackItemEvent != null) {
            btnGoBackItemEvent.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupGoBackButtonInActivityCalendar() {
        Button btnGoBackActivityCalendar = findViewById(R.id.btn_back_home_1);
        if (btnGoBackActivityCalendar != null) {
            btnGoBackActivityCalendar.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            });
        }
    }
}
