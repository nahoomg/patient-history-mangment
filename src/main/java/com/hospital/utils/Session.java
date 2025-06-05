package com.hospital.utils;

/**
 * Singleton class to manage user session data
 */
public class Session {
    private static Session instance;
    private Object currentUser;
    private String userType;

    private Session() {
        // Private constructor for singleton
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public Object getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Object currentUser) {
        this.currentUser = currentUser;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void clearSession() {
        currentUser = null;
        userType = null;
    }
} 