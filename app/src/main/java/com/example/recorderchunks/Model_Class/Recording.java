package com.example.recorderchunks.Model_Class;

public class Recording {
    private int recordingId;
    private int eventId;
    private String name;
    private String format;
    private String length;
    private String url;
    private boolean isRecorded;

    private  String date;
    private  String description;
    private  String is_transcribed;



    public Recording(int recordingId, int eventId, String date,String description,String name, String format, String length, String url, boolean isRecorded,String is_transcribed) {
        this.recordingId = recordingId;
        this.date=date;
        this.eventId = eventId;
        this.name = name;
        this.format = format;
        this.length = length;
        this.url = url;
        this.isRecorded = isRecorded;
        this.description=description;
        this.is_transcribed=is_transcribed;
    }

    // Getters
    public String getDescription()
    {
        return  description;
    }
    public String getIs_transcribed()
    {
        return  is_transcribed;
    }
    public int getRecordingId() {
        return recordingId;
    }

    public int getEventId() {
        return eventId;
    }
    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public String getLength() {
        return length;
    }

    public String getUrl() {
        return url;
    }

    public boolean isRecorded() {
        return isRecorded;
    }
}
