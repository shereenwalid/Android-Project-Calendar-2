package com.example.dailycalendar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;



public class CalendarActivity extends AppCompatActivity {

    private Button popupButton;
    private Button backHomeButton;
    private TextView welcomeTextView;
    private TextView instructionsTextView;
    private TextView eventTextView;
    private boolean isUsingCalendar2Layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitycalendar2);

        // Initialize layout
        welcomeTextView = findViewById(R.id.welcome_text_view);
        instructionsTextView = findViewById(R.id.instructions_text_view);
        popupButton = findViewById(R.id.popup_button);
        eventTextView = findViewById(R.id.event_text_view); // Initialize eventTextView

        // Setup popup button
        setupPopupButton(popupButton);

        // Register the context menu for eventTextView
        registerForContextMenu(eventTextView);

        // Default to activity_calendar2.xml
        isUsingCalendar2Layout = true;
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
                navigateToLayout("item_event");
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
        intent.putExtra("layout", layout); // Pass the layout name
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
        } else if (itemId == R.id.action_view_events) {
            navigateToLayout("item_event");
            return true;
        } else if (itemId == R.id.action_settings) {
            navigateToLayout("activity_calendar");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
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