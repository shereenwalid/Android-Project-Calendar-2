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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CalendarActivity extends AppCompatActivity {

    private Button popupButton;
    private TextView welcomeTextView;
    private TextView instructionsTextView;
    private TextView eventTextView;
    private boolean isUsingCalendar2Layout;
    private RecyclerView carouselRecyclerView;
    private static final int REQUEST_CODE_STORAGE_PERMISSIONS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitycalendar2);

        // Initialize views
        welcomeTextView = findViewById(R.id.welcome_text_view);
        instructionsTextView = findViewById(R.id.instructions_text_view);
        popupButton = findViewById(R.id.popup_button);
        eventTextView = findViewById(R.id.event_text_view);
        carouselRecyclerView = findViewById(R.id.carousel_recycler_view);

        // Set up popup button
        setupPopupButton(popupButton);
        registerForContextMenu(eventTextView);
        isUsingCalendar2Layout = true;

        // Set up carousel
        setupCarousel();
    }

    private void setupCarousel() {
        // Sample data (replace with your actual event data)
        List<CarouselItem> carouselItems = new ArrayList<>();
        carouselItems.add(new CarouselItem("Meeting", R.drawable.img1));

        carouselItems.add(new CarouselItem("Meeting", R.drawable.img2));

        carouselItems.add(new CarouselItem("Meeting", R.drawable.img3));

        // Set LayoutManager
        CarouselLayoutManager layoutManager = new CarouselLayoutManager();
        carouselRecyclerView.setLayoutManager(layoutManager);

        // Attach SnapHelper for smooth snapping
        CarouselSnapHelper snapHelper = new CarouselSnapHelper();
        snapHelper.attachToRecyclerView(carouselRecyclerView);

        // Set Adapter
        CarouselAdapter adapter = new CarouselAdapter(carouselItems);
        carouselRecyclerView.setAdapter(adapter);
    }

    private void setupPopupButton(Button popupButton) {
        popupButton.setOnClickListener(this::showPopupMenu);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_calendar, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.popup_add_event) {
                navigateToLayout("activity_main");
                return true;
            } else if (itemId == R.id.popup_view_event) {
                Intent intent = new Intent(CalendarActivity.this, ViewEventsActivity.class);
                startActivity(intent);

                return true;
            } else if (itemId == R.id.action_settings) {
                switchToCalendarLayout("activity_calendar");
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void navigateToLayout(String layout) {
        Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
        intent.putExtra("layout", layout);
        startActivity(intent);
    }

    private void switchToCalendarLayout(String layout) {
        Intent intent = new Intent(CalendarActivity.this, CalendarActivity1.class);
        intent.putExtra("layout", layout);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_calendar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_add_event) {
            navigateToLayout("dialog_add_event");
            return true;

        } else if (itemId == R.id.menu_view_event) {
            // ✅ Go to ViewEventsActivity (this shows your layout)
            Intent intent = new Intent(this, ViewEventsActivity.class);
            startActivity(intent);
            return true;

        } else if (itemId == R.id.action_filter_events) {
            Toast.makeText(this, "Filter Events clicked", Toast.LENGTH_SHORT).show();
            return true;

        } else if (itemId == R.id.action_sort_events) {
            Toast.makeText(this, "Sort Events clicked", Toast.LENGTH_SHORT).show();
            return true;

        } else if (itemId == R.id.action_settings) {
            navigateToLayout("activity_calendar");
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu_calendar, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.context_edit_event || itemId == R.id.context_delete_event ||
                itemId == R.id.action_filter_events || itemId == R.id.action_sort_events) {
            navigateToLayout("activity_calendar");
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
}

