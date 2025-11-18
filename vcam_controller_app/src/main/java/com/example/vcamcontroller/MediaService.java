package com.example.vcamcontroller;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
        notifyModule(enabled);
    }

    public String getVideoPath() {
        return prefs.getString("video_path", null);
    }

    public String getImagePath() {
        return prefs.getString("image_path", null);
    }

    public void setVideoPath(String path) {
        prefs.edit().putString("video_path", path).apply();
        notifyModulePath("video", path);
    }

    public void setImagePath(String path) {
        prefs.edit().putString("image_path", path).apply();
        notifyModulePath("image", path);
    }

    private void notifyModule(boolean enabled) {
        try {
            File statusFile = new File("/data/local/tmp/vcam_status.txt");
            FileWriter writer = new FileWriter(statusFile);
            writer.write("enabled=" + enabled + "\n");
            writer.close();
        } catch (IOException e) {
            // Handle error
        }
    }

    private void notifyModulePath(String type, String path) {
        try {
            File pathFile = new File("/data/local/tmp/vcam_paths.txt");
            FileWriter writer = new FileWriter(pathFile, true);  // Append
            writer.write(type + "=" + path + "\n");
            writer.close();
        } catch (IOException e) {
            // Handle error
        }
    }
}