package com.example.heal;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "HealUserSession";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String KEY_ROLE = "UserRole";
    private static final String KEY_NAME = "UserName";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    public void setRole(String role) {
        editor.putString(KEY_ROLE, role);
        editor.commit();
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "Patient");
    }

    public void setName(String name) {
        editor.putString(KEY_NAME, name);
        editor.commit();
    }

    public String getName() {
        return pref.getString(KEY_NAME, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}
