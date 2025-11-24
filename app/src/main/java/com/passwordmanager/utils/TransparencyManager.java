package com.passwordmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;

public class TransparencyManager {
    private static final String PREFS_NAME = "TransparencyPrefs";
    private static final String KEY_SEARCH_BAR_ALPHA = "search_bar_alpha";
    private static final String KEY_PASSWORD_CARD_ALPHA = "password_card_alpha";
    private static final String KEY_DELETE_BUTTON_ALPHA = "delete_button_alpha";
    
    private static final float DEFAULT_SEARCH_BAR_ALPHA = 1.0f;
    private static final float DEFAULT_PASSWORD_CARD_ALPHA = 1.0f;
    private static final float DEFAULT_DELETE_BUTTON_ALPHA = 1.0f;
    
    public static void saveSearchBarAlpha(Context context, float alpha) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_SEARCH_BAR_ALPHA, alpha).apply();
    }
    
    public static void savePasswordCardAlpha(Context context, float alpha) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_PASSWORD_CARD_ALPHA, alpha).apply();
    }
    
    public static void saveDeleteButtonAlpha(Context context, float alpha) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putFloat(KEY_DELETE_BUTTON_ALPHA, alpha).apply();
    }
    
    public static float getDeleteButtonAlpha(Context context) {
        if (context == null) return DEFAULT_DELETE_BUTTON_ALPHA;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_DELETE_BUTTON_ALPHA, DEFAULT_DELETE_BUTTON_ALPHA);
    }
    
    public static void applyDeleteButtonAlpha(Context context, View deleteButton) {
        if (context == null || deleteButton == null) return;
        float alpha = getDeleteButtonAlpha(context);
        deleteButton.setAlpha(alpha);
    }
    
    public static float getSearchBarAlpha(Context context) {
        if (context == null) return DEFAULT_SEARCH_BAR_ALPHA;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_SEARCH_BAR_ALPHA, DEFAULT_SEARCH_BAR_ALPHA);
    }
    
    public static float getPasswordCardAlpha(Context context) {
        if (context == null) return DEFAULT_PASSWORD_CARD_ALPHA;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_PASSWORD_CARD_ALPHA, DEFAULT_PASSWORD_CARD_ALPHA);
    }
    
    public static void applySearchBarAlpha(Context context, View searchBar) {
        if (context == null || searchBar == null) return;
        float alpha = getSearchBarAlpha(context);
        searchBar.setAlpha(alpha);
    }
    
    public static void applyPasswordCardAlpha(Context context, MaterialCardView cardView) {
        if (context == null || cardView == null) return;
        float alpha = getPasswordCardAlpha(context);
        cardView.setAlpha(alpha);
    }
}