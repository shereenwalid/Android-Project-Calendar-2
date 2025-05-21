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
import androidx.appcompat.widget.SearchView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private DatabaseHelper dbHelper;
    private TextView selectedDate;
    private String currentDate;
    private static final int REQUEST_CODE_ATTACHMENT = 1;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;
    private static final int REQUEST_CODE_STORAGE_PERMISSIONS = 102;
    private RecyclerView carouselRecyclerView;
    private CarouselAdapter carouselAdapter;
    private List<CarouselItem> carouselItemList;
    private TextView currentTvFileName;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize these before setContentView
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
            setContentView(R.layout.activity_calendar33);
            setupCalendarSelection();
            setupGoBackButtonInActivityCalendar();
        } else {
            // Default case - main activity with search
            setContentView(R.layout.activity_main);
            setupMainActivityViews(); // This includes search setup
        }

        requestNotificationPermission();
        requestStoragePermissions();
    }
    private void setupMainActivityViews() {
        selectedDate = findViewById(R.id.selectedDate);
        searchView = findViewById(R.id.searchView);

        // Add null check
        if (searchView == null) {
            Toast.makeText(this, "SearchView not found in layout", Toast.LENGTH_SHORT).show();
            return;
        }

        currentDate = getIntent().getStringExtra("selectedDate");

        if (currentDate == null) {
            currentDate = "2025-05-22";
        }
        selectedDate.setText(currentDate);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventList = dbHelper.getEventsByDate(currentDate);
        adapter = new EventAdapter(eventList, new EventAdapter.OnEventActionListener() {
            @Override
            public void onUpdate(Event event) {
                showUpdateDialog(event);
            }

            @Override
            public void onDelete(Event event) {
                new EventManager(MainActivity.this).deleteEvent(event.getId());
                refreshEvents();
            }
        });
        recyclerView.setAdapter(adapter);

        Button addButton = findViewById(R.id.btnAddEvent);
        addButton.setOnClickListener(v -> showAddEventDialog());

        selectedDate.setOnClickListener(v -> {
            Intent calendarIntent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(calendarIntent);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });
    }

    private void filterEvents(String query) {
        List<Event> filteredList;
        if (query.isEmpty()) {
            filteredList = dbHelper.getEventsByDate(currentDate);
        } else {
            filteredList = dbHelper.searchEventsByTitle(query, currentDate);
        }

        eventList.clear();
        eventList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(view);

        final EditText eventTitle = view.findViewById(R.id.eventTitle);
        final EditText eventDescription = view.findViewById(R.id.eventDescription);
        final TimePicker timePicker = view.findViewById(R.id.timePicker);
        Button saveButton = view.findViewById(R.id.btnSave);

        eventTitle.setText(event.getTitle());
        eventDescription.setText(event.getDescription());

        AlertDialog dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(v -> {
            String newTitle = eventTitle.getText().toString();
            String newDesc = eventDescription.getText().toString();
            String newTime = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());

            new EventManager(this).updateEvent(event.getId(), newTitle, newDesc + " at " + newTime, event.getDate());
            dialog.dismiss();
            refreshEvents();
        });
    }

    private void refreshEvents() {
        eventList.clear();
        eventList.addAll(dbHelper.getEventsByDate(currentDate));
        adapter.notifyDataSetChanged();
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
        currentTvFileName = tvFileName; // Store dialog's TextView reference
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try {
            startActivityForResult(intent, REQUEST_CODE_ATTACHMENT);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening file picker", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ATTACHMENT && resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            if (selectedUri != null) {
                // Persist URI permissions
                try {
                    getContentResolver().takePersistableUriPermission(
                            selectedUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String fileName = getFileNameFromUri(selectedUri);
                if (fileName != null && currentTvFileName != null) {
                    currentTvFileName.setText("Selected file: " + fileName);
                } else {
                    Toast.makeText(this, "Error: Could not retrieve file name", Toast.LENGTH_SHORT).show();
                }
            }
        }
        currentTvFileName = null; // Clear reference
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri == null) return null;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName != null ? fileName : "Unknown file";
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS
                );
            }
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            // Android 10 and below
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_CODE_STORAGE_PERMISSIONS
                );
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            // Android 11-12
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSIONS
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
        } else if (requestCode == REQUEST_CODE_STORAGE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

