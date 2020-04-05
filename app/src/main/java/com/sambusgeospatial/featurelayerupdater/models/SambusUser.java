package com.sambusgeospatial.featurelayerupdater.models;

import java.util.Date;

public class SambusUser {
    private String userFullName;
    private String userEmail;
    private String username;
    private String userDescription;
    private String userThumbnail;
    private Date sessionExpiry;

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String userDescription) {
        this.userDescription = userDescription;
    }

    public String getUserThumbnail() {
        return userThumbnail;
    }

    public void setUserThumbnail(String userThumbnail) {
        this.userThumbnail = userThumbnail;
    }

    public Date getSessionExpiry() {
        return sessionExpiry;
    }

    public void setSessionExpiry(Date sessionExpiry) {
        this.sessionExpiry = sessionExpiry;
    }
}
