#!/system/bin/sh

# Zygisk script for Virtual Camera Spoofer
MODDIR=${0%/*}

# Function to handle zygisk module loading
zygisk_module() {
    # Load the zygisk library
    if [ -f "$MODDIR/zygisk/zygisk_vcam.so" ]; then
        # Zygisk will load this library automatically
        return 0
    else
        return 1
    fi
}

# Check if zygisk is available
if [ -n "$ZYGISK_LOADED" ]; then
    zygisk_module
fi