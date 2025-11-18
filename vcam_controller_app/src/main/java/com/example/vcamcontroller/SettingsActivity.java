package com.example.vcamcontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox checkBoxNoToast;
    private CheckBox checkBoxNoSilent;
    private CheckBox checkBoxPrivateDir;
    private EditText editTextVideoPath;
    private EditText editTextImagePath;
    private Button buttonSave;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("vcam_prefs", MODE_PRIVATE);

        checkBoxNoToast = findViewById(R.id.checkbox_no_toast);
        checkBoxNoSilent = findViewById(R.id.checkbox_no_silent);
        checkBoxPrivateDir = findViewById(R.id.checkbox_private_dir);
        editTextVideoPath = findViewById(R.id.edit_video_path);
        editTextImagePath = findViewById(R.id.edit_image_path);
        buttonSave = findViewById(R.id.button_save);

        // Load settings
        checkBoxNoToast.setChecked(prefs.getBoolean("no_toast", false));
        checkBoxNoSilent.setChecked(prefs.getBoolean("no_silent", false));
        checkBoxPrivateDir.setChecked(prefs.getBoolean("private_dir", false));
        editTextVideoPath.setText(prefs.getString("video_path", "/sdcard/DCIM/Camera1/virtual.mp4"));
        editTextImagePath.setText(prefs.getString("image_path", "/sdcard/DCIM/Camera1/1000.bmp"));

        buttonSave.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean("no_toast", checkBoxNoToast.isChecked())
                    .putBoolean("no_silent", checkBoxNoSilent.isChecked())
                    .putBoolean("private_dir", checkBoxPrivateDir.isChecked())
                    .putString("video_path", editTextVideoPath.getText().toString())
                    .putString("image_path", editTextImagePath.getText().toString())
                    .apply();
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}