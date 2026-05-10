package com.example.heal;

public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type; // "appointment" or "room"
    private String referenceId; // bookingId or appointmentId
    private int milestone; // days left (3, 2, 1, 0)
    private long timestamp;
    private boolean read;

    public Notification() {
        // Required for Firebase
    }

    public Notification(String id, String userId, String title, String message, String type, String referenceId, int milestone, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.milestone = milestone;
        this.timestamp = timestamp;
        this.read = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public int getMilestone() { return milestone; }
    public void setMilestone(int milestone) { this.milestone = milestone; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
