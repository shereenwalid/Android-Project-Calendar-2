package com.example.dailycalendar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import androidx.appcompat.widget.SearchView;


public class ViewEventsActivity extends AppCompatActivity {

    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private DatabaseHelper dbHelper;
    private SearchView searchView;
    private List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_events);
        Button goToHomeButton = findViewById(R.id.goToHomeButton);
        goToHomeButton.setOnClickListener(v -> {
            startActivity(new Intent(ViewEventsActivity.this, CalendarActivity.class));
        });

        dbHelper = new DatabaseHelper(this);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        searchView = findViewById(R.id.searchView);
        goToHomeButton = findViewById(R.id.goToHomeButton);

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        goToHomeButton.setOnClickListener(v -> finish());
        setupSearchView();
        loadAllEvents();
    }

    private void setupSearchView() {
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

    private void loadAllEvents() {
        allEvents = dbHelper.getAllEvents();
        eventAdapter = new EventAdapter(allEvents, new EventAdapter.OnEventActionListener() {

            @Override
            public void onUpdate(Event event) {
                // Open UpdateEventActivity with the event data
                Intent intent = new Intent(ViewEventsActivity.this, UpdateEventActivity.class);

                // Pass event data to UpdateEventActivity
                intent.putExtra("eventId", event.getId());
                intent.putExtra("title", event.getTitle());
                intent.putExtra("description", event.getDescription());
                intent.putExtra("date", event.getDate());
                startActivity(intent);
            }


            @Override
            public void onDelete(Event event) {
                // Handle delete
                dbHelper.deleteEvent(event.getId());
                loadAllEvents(); // Refresh the list
            }
        });
        eventsRecyclerView.setAdapter(eventAdapter);
    }

    private void filterEvents(String query) {
        List<Event> filteredList;
        if (query.isEmpty()) {
            filteredList = allEvents;
        } else {
            filteredList = dbHelper.searchEvents(query);
        }
        eventAdapter.updateList(filteredList);
    }

    private void showEditDialog(Event event) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Event");

        // Set up the input fields
        final EditText edtTitle = new EditText(this);
        edtTitle.setText(event.getTitle());
        final EditText edtDescription = new EditText(this);
        edtDescription.setText(event.getDescription());
        final EditText edtDate = new EditText(this);
        edtDate.setText(event.getDate());

        // Set up layout for dialog
        builder.setView(edtTitle);
        builder.setView(edtDescription);
        builder.setView(edtDate);

        // Set up button actions for dialog
        builder.setPositiveButton("Update", (dialog, which) -> {
            // Get updated event data
            String updatedTitle = edtTitle.getText().toString();
            String updatedDescription = edtDescription.getText().toString();
            String updatedDate = edtDate.getText().toString();

            // Update the event in the database (filePath is not updated)
            boolean isUpdated = dbHelper.updateEvent(event.getId(), updatedTitle, updatedDescription, updatedDate);

            if (isUpdated) {
                Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show();
                loadAllEvents(); // Refresh the list after update
            } else {
                Toast.makeText(this, "Failed to update event.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}