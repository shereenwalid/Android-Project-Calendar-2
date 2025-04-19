package com.example.dailycalendar;

import android.content.Context;
import android.widget.Toast;

public class EventManager {

    private DatabaseHelper dbHelper;
    private Context context;

    public EventManager(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public void updateEvent(int eventId, String newTitle, String newDescription, String newDate) {
        boolean isUpdated = dbHelper.updateEvent(eventId, newTitle, newDescription, newDate);
        if (isUpdated) {
            Toast.makeText(context, "Event Updated Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to Update Event", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteEvent(int eventId) {
        boolean isDeleted = dbHelper.deleteEvent(eventId);
        if (isDeleted) {
            Toast.makeText(context, "Event Deleted Successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to Delete Event", Toast.LENGTH_SHORT).show();
        }
    }
}
