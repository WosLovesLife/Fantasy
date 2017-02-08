package com.wosloveslife.fantasy.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.wosloveslife.fantasy.manager.CustomConfiguration;

import java.util.Set;

/**
 * Created by YesingBeijing on 2016/11/30.
 */
public class SPHelper {
    private static final SPHelper sSPHelper = new SPHelper();
    private Context mContext;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEdit;

    private SPHelper() {
    }

    public static SPHelper getInstance() {
        return sSPHelper;
    }

    public void init(@NonNull Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        mEdit = mPreferences.edit();

        CustomConfiguration.init();
    }

    public void save(@NonNull String key, @NonNull String value) {
        mEdit.putString(key, value).apply();
    }

    public void save(@NonNull String key, @NonNull Set<String> value) {
        mEdit.putStringSet(key, value).apply();
    }

    public void save(@NonNull String key, boolean value) {
        mEdit.putBoolean(key, value).apply();
    }

    public void save(@NonNull String key, int value) {
        mEdit.putInt(key, value).apply();
    }

    public void save(@NonNull String key, long value) {
        mEdit.putLong(key, value).apply();
    }

    public void save(@NonNull String key, float value) {
        mEdit.putFloat(key, value).apply();
    }


    public String get(@NonNull String key, @NonNull String def) {
        return mPreferences.getString(key, def);
    }

    public Set<String> get(@NonNull String key, @NonNull Set<String> def) {
        return mPreferences.getStringSet(key, def);
    }

    public boolean get(@NonNull String key, boolean def) {
        return mPreferences.getBoolean(key, def);
    }

    public int get(@NonNull String key, int def) {
        return mPreferences.getInt(key, def);
    }

    public long get(@NonNull String key, long def) {
        return mPreferences.getLong(key, def);
    }

    public float get(@NonNull String key, float def) {
        return mPreferences.getFloat(key, def);
    }
}
