package com.sambusgeospatial.featurelayerupdater.handlers;

import android.content.Context;
import android.content.SharedPreferences;

import com.sambusgeospatial.featurelayerupdater.models.SambusUser;

import java.util.Date;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_DESCRIPTION = "user_description";
    private static final String KEY_USER_THUMBNAIL = "user_thumbnail";
    private static final String KEY_EXPIRES = "expires";
    private static final String KEY_EMPTY = "";
    private Context mContext;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mPreferences;

    public SessionManager(Context mContext) {
        this.mContext = mContext;
        mPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.mEditor = mPreferences.edit();
    }

    /**
     * Logs in the user by saving user details and setting session
     *
     * @param fullName
     * @param email
     * @param username
     * @param userDescription
     * @param userThumbnail
     */
    public void loginUser(String fullName, String email, String username, String userDescription, String userThumbnail) {
        mEditor.putString(KEY_FULL_NAME, fullName);
        mEditor.putString(KEY_EMAIL, email);
        mEditor.putString(KEY_USERNAME, username);
        mEditor.putString(KEY_USER_DESCRIPTION, userDescription);
        mEditor.putString(KEY_USER_THUMBNAIL, userThumbnail);
        Date date = new Date();

        //Set user session for next 7 days
        long millis = date.getTime() + (7 * 24 * 60 * 60 * 1000);
        mEditor.putLong(KEY_EXPIRES, millis);
        mEditor.commit();
    }

    /**
     * Checks whether user is logged in
     *
     * @return
     */
    public boolean isLoggedIn() {
        Date currentDate = new Date();

        long millis = mPreferences.getLong(KEY_EXPIRES, 0);

        /* If shared preferences does not have a value
         then user is not logged in
         */
        if (millis == 0) {
            return false;
        }
        Date expiryDate = new Date(millis);

        /* Check if session is expired by comparing
        current date and Session expiry date
        */
        return currentDate.before(expiryDate);
    }

    /**
     * Fetches and returns user details
     *
     * @return user details
     */
    public SambusUser getUserDetails() {
        //Check if user is logged in first
        if (!isLoggedIn()) {
            return null;
        }
        SambusUser user = new SambusUser();
        user.setUserFullName(mPreferences.getString(KEY_FULL_NAME, KEY_EMPTY));
        user.setUserEmail(mPreferences.getString(KEY_EMAIL, KEY_EMPTY));
        user.setUsername(mPreferences.getString(KEY_USERNAME, KEY_EMPTY));
        user.setUserDescription(mPreferences.getString(KEY_USER_DESCRIPTION, KEY_EMPTY));
        user.setUserThumbnail(mPreferences.getString(KEY_USER_THUMBNAIL, KEY_EMPTY));
        user.setSessionExpiry(new Date(mPreferences.getLong(KEY_EXPIRES, 0)));

        return user;
    }

    /**
     * Logs out user by clearing the session
     */
    public void logoutUser(){
        mEditor.clear();
        mEditor.commit();
    }

}
