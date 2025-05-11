package com.example.dailycalendar;

import android.os.Bundle;

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

        dbHelper = new DatabaseHelper(this);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        searchView = findViewById(R.id.searchView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                // Handle update
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
}