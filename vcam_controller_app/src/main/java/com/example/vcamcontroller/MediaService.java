package com.example.vcamcontroller;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.File;

public class MediaService extends Service {

    private final IBinder binder = new LocalBinder();
    private SharedPreferences prefs;

    public class LocalBinder extends Binder {
        MediaService getService() {
            return MediaService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("vcam_prefs", MODE_PRIVATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean isSpoofEnabled() {
        return prefs.getBoolean("spoof_enabled", false);
    }

    public void setSpoofEnabled(boolean enabled) {
        prefs.edit().putBoolean("spoof_enabled", enabled).apply();
        // TODO: Notify Magisk module
    }

    public String getVideoPath() {
        return prefs.getString("video_path", null);
    }

    public String getImagePath() {
        return prefs.getString("image_path", null);
    }

    public void setVideoPath(String path) {
        prefs.edit().putString("video_path", path).apply();
        // Copy to module directory
    }

    public void setImagePath(String path) {
        prefs.edit().putString("image_path", path).apply();
        // Copy to module directory
    }
}