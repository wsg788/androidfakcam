"# Virtual Camera Spoofer (VCam) - Magisk Module + Controller APK

## Overview
This project creates a Magisk module that spoofs camera input on rooted Android devices, integrated with a user-friendly APK controller. It allows replacing live camera feeds with pre-recorded videos and images, controlled via the APK instead of manual file placement.

## Features
- **Camera Spoofing**: Replaces Camera1 and Camera2 APIs with video playback and static images.
- **User-Friendly Control**: APK-based UI for enabling/disabling, selecting media, and settings.
- **Multi-Media Support**: Choose videos and images for preview and photo modes.
- **Privacy Modes**: Options for effects like blurring or watermarks.
- **Real-Time Controls**: Toggle spoofing per app or globally.
- **Logging and Troubleshooting**: In-app logs and error handling.

## Components
1. **Magisk Module** (`magisk_module.zip`): Systemless Zygisk module for hooking camera APIs.
2. **Controller APK** (`vcam_controller.apk`): Android app for configuration and control.

## Installation
1. **Install Magisk Module**:
   - Flash `magisk_module.zip` via Magisk Manager.
   - Reboot device.
2. **Install APK**:
   - Sideload `vcam_controller.apk` on your device.
   - Grant necessary permissions (Storage, Camera).

## Usage
1. Open the VCam Controller app.
2. Toggle "Enable Camera Spoofing".
3. Select a video (for preview) and image (for photos).
4. Test with a camera app (e.g., default camera or social media apps).

## Development Notes
- **Reverse Engineering**: Based on the Xposed module from https://github.com/Xposed-Modules-Repo/com.example.vcam.
- **Zygisk Hooks**: Uses native C++ for system-level hooking.
- **APK Communication**: Uses SharedPreferences for settings; can be extended to bound services.
- **Limitations**: Currently a framework; full native implementation needed for production.
- **Testing**: Test on Android 8.0+ with Magisk/Zygisk.

## Disclaimer
Use for ethical purposes only (e.g., testing, privacy). Do not use for illegal activities. All consequences are your own.

## Next Steps
- Build and test the APK in Android Studio.
- Compile the Zygisk library with NDK.
- Implement full hooks and video decoding.
- Add advanced features like live overlays.
- Iterate based on testing feedback.
"