package com.example.dailycalendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "events.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                "filePath TEXT" +
                ")";
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }
    public boolean addEvent(String title, String description, String date, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, title);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_DATE, date);
        contentValues.put("filePath", filePath); // Store the file path

        long result = db.insert(TABLE_EVENTS, null, contentValues);
        db.close();

        return result != -1; // Return true if the insertion was successful
    }


    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String eventDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow("filePath"));

                Event event = new Event(id ,title, description, eventDate,filePath);
                eventList.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return eventList;
    }




    public boolean updateEvent(int id, String title, String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);

        int rows = db.update(TABLE_EVENTS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();

        // Debug log to help trace the issue
        Log.d("DatabaseHelper", "Updating event with id: " + id);
        Log.d("DatabaseHelper", "Rows affected: " + rows);

        return rows > 0;
    }

    public boolean deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_EVENTS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();

        // Debug log to help trace the issue
        Log.d("DatabaseHelper", "Deleting event with id: " + id);
        Log.d("DatabaseHelper", "Rows affected: " + rows);

        return rows > 0;
    }

    public List<Event> getEventsByDate(String date) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EVENTS + " WHERE date = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow("filePath")); // Get the file path
                Event event = new Event(id, title, description, date, filePath); // Pass the file path to Event object
                eventList.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return eventList;
    }

    public List<Event> searchEventsByTitle(String searchQuery, String date) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EVENTS;
        List<String> selectionArgs = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            query += " WHERE title LIKE ?";
            String likeQuery = "%" + searchQuery + "%";
            selectionArgs.add(likeQuery);
        }

        if (date != null && !date.trim().isEmpty()) {
            query += selectionArgs.isEmpty() ? " WHERE date = ?" : " AND date = ?";
            selectionArgs.add(date);
        }

        Cursor cursor = db.rawQuery(query, selectionArgs.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String eventDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow("filePath"));
                Event event = new Event(id, title, description, eventDate, filePath);
                eventList.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return eventList;
    }

    public List<Event> searchEvents(String query) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String searchQuery = "%" + query + "%";
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_EVENTS +
                        " WHERE " + COLUMN_TITLE + " LIKE ? OR " +
                        COLUMN_DESCRIPTION + " LIKE ?",
                new String[]{searchQuery, searchQuery}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow("filePath"));

                eventList.add(new Event(id, title, description, date, filePath));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return eventList;
    }


}