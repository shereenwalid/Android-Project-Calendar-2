package com.example.dailycalendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private DatabaseHelper dbHelper;
    private TextView selectedDate;
    private String currentDate;
    private static final int REQUEST_CODE_ATTACHMENT = 1;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermission(); // Request notification permission for Android 13+

        setContentView(R.layout.activitycalendar2); // Default layout

        eventList = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);

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

        eventList = dbHelper.getEventsByDate(currentDate);
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
        Button btnAttach = view.findViewById(R.id.btnAttach);
        TextView tvFileName = view.findViewById(R.id.tvFileName);

        timePicker.setIs24HourView(true);
        AlertDialog dialog = builder.create();

        btnAttach.setOnClickListener(v -> openFilePicker(tvFileName));

        saveButton.setOnClickListener(v -> {
            String title = eventTitle.getText().toString().trim();
            String description = eventDescription.getText().toString().trim();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);

            if (!title.isEmpty()) {
                String filePath = tvFileName.getText().toString();
                if (dbHelper.addEvent(title, description + " at " + time, currentDate, filePath)) {
                    Toast.makeText(this, "Event Added", Toast.LENGTH_SHORT).show();
                    eventList.clear();
                    eventList.addAll(dbHelper.getEventsByDate(currentDate));
                    adapter.notifyDataSetChanged();

                    // Schedule Reminder using WorkManager
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    long eventTimeMillis = calendar.getTimeInMillis();
                    long delayMillis = eventTimeMillis - 10 * 60 * 1000 - System.currentTimeMillis();

                    if (delayMillis > 0) {
                        Data data = new Data.Builder()
                                .putString("eventTitle", title)
                                .build();

                        OneTimeWorkRequest reminderRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                                .setInputData(data)
                                .build();

                        WorkManager.getInstance(this).enqueue(reminderRequest);
                    }

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

    private void openFilePicker(TextView tvFileName) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_ATTACHMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ATTACHMENT && resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                String fileName = getFileNameFromUri(selectedUri);
                TextView tvFileName = findViewById(R.id.tvFileName);
                tvFileName.setText("Selected file: " + fileName);
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileName = cursor.getString(columnIndex);
                cursor.close();
            }
        }
        return fileName;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
