package com.eduar2tc.calculator.utils;

//save data and configurations in shared preferences
import android.content.Context;
import android.content.SharedPreferences;

public class SharePrefs {
    private static SharePrefs instance;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "calculator_prefs";
    private static final String KEY_HIBERNATION = "hibernation";
    private static final String KEY_ANIMATION = "animation";
    private static final String KEY_THEME = "theme";

    private SharePrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharePrefs getInstance(Context context) {
        if (instance == null) {
            instance = new SharePrefs(context);
        }
        return instance;
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, "system");
    }

    public void setAnimationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ANIMATION, enabled).apply();
    }

    public boolean isAnimationEnabled() {
        return prefs.getBoolean(KEY_ANIMATION, true);
    }

    public void setHibernation(boolean enabled) {
        prefs.edit().putBoolean(KEY_HIBERNATION, enabled).apply();
    }

    public boolean isHibernationEnabled() {
        return prefs.getBoolean(KEY_HIBERNATION, false);
    }
}
