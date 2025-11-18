package com.example.vcamcontroller;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final int REQUEST_CODE_VIDEO_PICK = 101;
    private static final int REQUEST_CODE_IMAGE_PICK = 102;

    private Switch switchEnableSpoof;
    private Button buttonSelectVideo;
    private Button buttonSelectImage;
    private TextView textViewStatus;
    private SharedPreferences prefs;
    private MediaService mediaService;
    private boolean bound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaService.LocalBinder binder = (MediaService.LocalBinder) service;
            mediaService = binder.getService();
            bound = true;
            updateStatus();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("vcam_prefs", MODE_PRIVATE);

        switchEnableSpoof = findViewById(R.id.switch_enable_spoof);
        buttonSelectVideo = findViewById(R.id.button_select_video);
        buttonSelectImage = findViewById(R.id.button_select_image);
        textViewStatus = findViewById(R.id.text_view_status);

        // Bind to service
        Intent intent = new Intent(this, MediaService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        // Check permissions
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
        }

        // Load saved state
        switchEnableSpoof.setChecked(prefs.getBoolean("spoof_enabled", false));
        updateStatus();

        switchEnableSpoof.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (bound) {
                mediaService.setSpoofEnabled(isChecked);
            }
            updateStatus();
            Toast.makeText(this, "Spoofing " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        buttonSelectVideo.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickIntent, REQUEST_CODE_VIDEO_PICK);
        });

        buttonSelectImage.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickIntent, REQUEST_CODE_IMAGE_PICK);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

    private void updateStatus() {
        boolean enabled = prefs.getBoolean("spoof_enabled", false);
        String videoPath = prefs.getString("video_path", null);
        String imagePath = prefs.getString("image_path", null);
        String status = "Status: " + (enabled ? "Enabled" : "Disabled") +
                "\nVideo: " + (videoPath != null ? new File(videoPath).getName() : "None") +
                "\nImage: " + (imagePath != null ? new File(imagePath).getName() : "None");
        textViewStatus.setText(status);
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permissions required for VCam to work", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    File destDir = new File(getExternalFilesDir(null), "vcam_media");
                    if (!destDir.exists()) destDir.mkdirs();
                    String fileName = requestCode == REQUEST_CODE_VIDEO_PICK ? "virtual.mp4" : "1000.bmp";
                    File destFile = new File(destDir, fileName);
                    FileOutputStream outputStream = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                    inputStream.close();
                    if (bound) {
                        if (requestCode == REQUEST_CODE_VIDEO_PICK) {
                            mediaService.setVideoPath(destFile.getAbsolutePath());
                        } else {
                            mediaService.setImagePath(destFile.getAbsolutePath());
                        }
                    }
                    updateStatus();
                    Toast.makeText(this, fileName + " selected and copied", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error copying file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}