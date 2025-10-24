package com.m3.veilchat.models;

public class RecentActivity {
    public static final int TYPE_ROOM_JOIN = 1;
    public static final int TYPE_ROOM_CREATE = 2;
    public static final int TYPE_MESSAGE_SENT = 3;

    private String activityId;
    private String roomId;
    private String roomName;
    private String description;
    private long timestamp;
    private int type;

    public RecentActivity() {}

    public RecentActivity(String roomId, String roomName, String description, long timestamp, int type) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.description = description;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters and Setters
    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
}