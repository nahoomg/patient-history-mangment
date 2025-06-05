package com.hospital.models;

/**
 * Represents a system activity log entry
 */
public class ActivityLog {
    private final String timestamp;
    private final String activityType;
    private final String description;
    
    public ActivityLog(String timestamp, String activityType, String description) {
        this.timestamp = timestamp;
        this.activityType = activityType;
        this.description = description;
    }
    
    public String getTimestamp() { 
        return timestamp; 
    }
    
    public String getActivityType() { 
        return activityType; 
    }
    
    public String getDescription() { 
        return description; 
    }
} 