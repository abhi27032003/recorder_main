package com.example.recorderchunks;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.recorderchunks.Event;

import java.util.ArrayList;
import java.util.List;

// Database Helper Class
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "EventDatabase.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_NAME = "events";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CREATION_DATE = "creation_date";
    public static final String COL_CREATION_TIME = "creation_time";
    public static final String COL_EVENT_DATE = "event_date";
    public static final String COL_EVENT_TIME = "event_time";
    public static final String COL_AUDIO_PATH = "audio_path";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TITLE + " TEXT,"
                + COL_DESCRIPTION + " TEXT,"
                + COL_CREATION_DATE + " TEXT,"
                + COL_CREATION_TIME + " TEXT,"
                + COL_EVENT_DATE + " TEXT,"
                + COL_EVENT_TIME + " TEXT,"

                + COL_AUDIO_PATH + " TEXT" +
                ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertEvent(String title, String description, String creationDate, String creationTime, String eventDate, String eventTime,String audioPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_CREATION_DATE, creationDate);
        values.put(COL_CREATION_TIME, creationTime);
        values.put(COL_EVENT_DATE, eventDate);
        values.put(COL_EVENT_TIME, eventTime);
        values.put(COL_AUDIO_PATH, audioPath);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    // Retrieve all events
    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Event event = new Event(
                        cursor.getInt(cursor.getColumnIndex(COL_ID)),
                        cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COL_CREATION_DATE)),
                        cursor.getString(cursor.getColumnIndex(COL_CREATION_TIME)),
                        cursor.getString(cursor.getColumnIndex(COL_EVENT_DATE)),
                        cursor.getString(cursor.getColumnIndex(COL_EVENT_TIME)),
                        cursor.getString(cursor.getColumnIndex(COL_AUDIO_PATH))

                );
                eventList.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return eventList;
    }
    public Event getEventById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") Event event = new Event(
                    cursor.getInt(cursor.getColumnIndex(COL_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(COL_CREATION_DATE)),
                    cursor.getString(cursor.getColumnIndex(COL_CREATION_TIME)),
                    cursor.getString(cursor.getColumnIndex(COL_EVENT_DATE)),
                    cursor.getString(cursor.getColumnIndex(COL_EVENT_TIME)),
                    cursor.getString(cursor.getColumnIndex(COL_AUDIO_PATH))
            );
            cursor.close();
            return event;
        }
        return null;
    }
    // Delete an event by ID
    public boolean deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, COL_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsDeleted > 0;
    }
}
