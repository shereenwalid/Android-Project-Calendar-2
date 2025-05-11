package com.example.dailycalendar;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class CalendarActivity1 extends AppCompatActivity {

    private CalendarView calendarView;
    private Button btnSelectDate;
    private Button btnGoHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for layout extra and set the appropriate layout
        Intent intent = getIntent();
        String layout = intent.getStringExtra("layout");

        if ("activity_calendar".equals(layout)) {
            setContentView(R.layout.activity_calendar);
        } else {
            // Set a default layout or handle error case
            setContentView(R.layout.activity_calendar);
        }

        // Initialize layout elements
        calendarView = findViewById(R.id.calendarView);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnGoHome = findViewById(R.id.btn_back_home_1);

        setupCalendarView();
        setupGoHomeButton();
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            btnSelectDate.setOnClickListener(v -> {
                if (selectedDate != null) {
                    Intent intent = new Intent(CalendarActivity1.this, MainActivity.class);
                    intent.putExtra("selectedDate", selectedDate);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupGoHomeButton() {
        btnGoHome.setOnClickListener(v -> goToMainCalendarLayout());
    }

    private void goToMainCalendarLayout() {
        Intent intent = new Intent(CalendarActivity1.this, CalendarActivity.class);
        startActivity(intent);
    }
}