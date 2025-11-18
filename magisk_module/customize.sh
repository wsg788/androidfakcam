#!/system/bin/sh
# This script will be executed during installation
# Add your custom commands here
MODDIR=${0%/*}

# Grant necessary permissions or perform setup
# Example: set permissions for zygisk library
chmod 755 $MODDIR/zygisk/zygisk_vcam.so

# Create necessary directories if needed
mkdir -p /data/local/tmp/vcam_data

# Note: Actual setup will be done in the APK for user-friendliness
ui_print "- Virtual Camera Spoofer installed"
ui_print "- Pair with the VCam Controller APK for setup"