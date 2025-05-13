package com.example.dailycalendar;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdateEventActivity extends AppCompatActivity {

    private EditText eventTitle, eventDescription;
    private TimePicker timePicker;
    private RadioGroup radioGroupPriority;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_event);  // Set the layout to the one you provided

        // Initialize UI elements
        eventTitle = findViewById(R.id.eventTitle);
        eventDescription = findViewById(R.id.eventDescription);
        timePicker = findViewById(R.id.timePicker);
        radioGroupPriority = findViewById(R.id.radio_group_priority);
        btnSave = findViewById(R.id.btnSave);

        // Retrieve the event data passed from the previous activity
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String date = getIntent().getStringExtra("date");
        String priority = getIntent().getStringExtra("priority");

        // Set the data into the UI elements
        eventTitle.setText(title);
        eventDescription.setText(description);
        // Optionally, set the date and time from the event data if needed
        // For simplicity, we are assuming hour and minute are already extracted or passed from the event data
        timePicker.setHour(12);  // Example value
        timePicker.setMinute(30);  // Example value

        // Set the priority in the radio group
        if (priority != null) {
            if (priority.equals("Medium")) {
                radioGroupPriority.check(R.id.radio_medium);
            } else if (priority.equals("High")) {
                radioGroupPriority.check(R.id.radio_high);
            } else {
                radioGroupPriority.check(R.id.radio_low);
            }
        }

        // Set the "Save" button functionality
        btnSave.setOnClickListener(v -> {
            String updatedTitle = eventTitle.getText().toString();
            String updatedDescription = eventDescription.getText().toString();
            int updatedHour = timePicker.getHour();
            int updatedMinute = timePicker.getMinute();
            int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
            String updatedPriority = getPriorityFromRadioButton(selectedPriorityId);

            // Update the event in the database
            updateEventInDatabase(updatedTitle, updatedDescription, updatedHour, updatedMinute, updatedPriority);

            // Show a toast message
            Toast.makeText(this, "Event Updated!", Toast.LENGTH_SHORT).show();

            // Close the activity and return to the previous screen
            finish();
        });
    }

    private String getPriorityFromRadioButton(int selectedPriorityId) {
        if (selectedPriorityId == R.id.radio_medium) {
            return "Medium";
        } else if (selectedPriorityId == R.id.radio_high) {
            return "High";
        }
        return "Low";  // Default priority
    }

    private void updateEventInDatabase(String title, String description, int hour, int minute, String priority) {
        int eventId = getIntent().getIntExtra("eventId", -1);  // Pass the event ID from the previous activity

        if (eventId != -1) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            String updatedDate = getUpdatedDate(hour, minute); // Convert time to the correct format
            boolean isUpdated = dbHelper.updateEvent(eventId, title, description, updatedDate); // Update in the DB

            if (isUpdated) {
                Toast.makeText(this, "Event Updated!", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity and return to the previous screen
            } else {
                Toast.makeText(this, "Failed to update event.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getUpdatedDate(int hour, int minute) {
        // Example: "2025-05-13 14:30" format
        int year = 2025;  // Replace with actual year from your event data
        int month = 5;    // Replace with actual month
        int day = 13;     // Replace with actual day
        return String.format("%04d-%02d-%02d %02d:%02d", year, month, day, hour, minute);
    }


}
