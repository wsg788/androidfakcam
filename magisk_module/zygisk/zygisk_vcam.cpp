#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <unistd.h>
#include <sys/system_properties.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaExtractor.h>
#include <media/NdkMediaFormat.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#define LOG_TAG "VCAM_ZYGISK"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Adapted from VideoToFrames.java
class VideoDecoder {
public:
    VideoDecoder() : extractor_(nullptr), codec_(nullptr), surface_(nullptr) {}
    ~VideoDecoder() { release(); }

    bool init(const char* videoPath, ANativeWindow* surface) {
        surface_ = surface;
        extractor_ = AMediaExtractor_new();
        if (AMediaExtractor_setDataSource(extractor_, videoPath) != AMEDIA_OK) {
            LOGE("Failed to set data source: %s", videoPath);
            return false;
        }
        size_t trackCount = AMediaExtractor_getTrackCount(extractor_);
        for (size_t i = 0; i < trackCount; ++i) {
            AMediaFormat* format = AMediaExtractor_getTrackFormat(extractor_, i);
            const char* mime;
            if (AMediaFormat_getString(format, AMEDIAFORMAT_KEY_MIME, &mime) && strncmp(mime, "video/", 6) == 0) {
                AMediaExtractor_selectTrack(extractor_, i);
                codec_ = AMediaCodec_createDecoderByType(mime);
                AMediaCodec_configure(codec_, format, surface_, nullptr, 0);
                AMediaCodec_start(codec_);
                LOGI("Decoder initialized for %s", videoPath);
                AMediaFormat_delete(format);
                return true;
            }
            AMediaFormat_delete(format);
        }
        return false;
    }

    void decodeFrame(uint8_t* buffer, size_t bufferSize) {
        if (!codec_) return;
        AMediaCodecBufferInfo info;
        ssize_t bufIdx = AMediaCodec_dequeueOutputBuffer(codec_, &info, 10000);
        if (bufIdx >= 0) {
            size_t bufSize;
            uint8_t* buf = AMediaCodec_getOutputBuffer(codec_, bufIdx, &bufSize);
            if (bufSize <= bufferSize) {
                memcpy(buffer, buf, bufSize);
            }
            AMediaCodec_releaseOutputBuffer(codec_, bufIdx, info.size != 0);
        }
    }

    void release() {
        if (codec_) {
            AMediaCodec_stop(codec_);
            AMediaCodec_delete(codec_);
            codec_ = nullptr;
        }
        if (extractor_) {
            AMediaExtractor_delete(extractor_);
            extractor_ = nullptr;
        }
    }

private:
    AMediaExtractor* extractor_;
    AMediaCodec* codec_;
    ANativeWindow* surface_;
};

// Global variables
static VideoDecoder* g_decoder = nullptr;
static bool g_spoof_enabled = false;
static char g_video_path[256] = "/sdcard/DCIM/Camera1/virtual.mp4";
static char g_image_path[256] = "/sdcard/DCIM/Camera1/1000.bmp";
static uint8_t* g_frame_buffer = nullptr;
static size_t g_frame_size = 0;

// Hook Camera.setPreviewTexture (Camera1)
static void* (*original_setPreviewTexture)(JNIEnv*, jobject, jobject);
static void* hooked_setPreviewTexture(JNIEnv* env, jobject thiz, jobject surfaceTexture) {
    LOGI("Hooked setPreviewTexture");
    if (g_spoof_enabled) {
        // Create fake SurfaceTexture
        // TODO: Implement fake texture creation
        LOGI("Spoofing preview texture");
        return nullptr;  // Replace with fake
    }
    return original_setPreviewTexture(env, thiz, surfaceTexture);
}

// Hook Camera.startPreview
static void* (*original_startPreview)(JNIEnv*, jobject);
static void* hooked_startPreview(JNIEnv* env, jobject thiz) {
    LOGI("Hooked startPreview");
    if (g_spoof_enabled && g_decoder) {
        // Start decoding loop
        // TODO: Run decode in thread
        LOGI("Starting spoofed preview");
    }
    return original_startPreview(env, thiz);
}

// Hook Camera.takePicture (simplified)
static void* (*original_takePicture)(JNIEnv*, jobject, jobject, jobject, jobject, jobject);
static void* hooked_takePicture(JNIEnv* env, jobject thiz, jobject shutter, jobject raw, jobject postview, jobject jpeg) {
    LOGI("Hooked takePicture");
    if (g_spoof_enabled) {
        // Replace JPEG callback with our image
        // TODO: Load BMP and inject
        LOGI("Spoofing photo");
    }
    return original_takePicture(env, thiz, shutter, raw, postview, jpeg);
}

// Zygisk entry point
extern "C" void zygisk_module_entry(zygisk::Api* api) {
    LOGI("VCam Zygisk module loaded");

    // Hook JNI methods
    // Note: This is pseudo-code; use api->hookJniNativeMethods for real hooks
    // api->hookJniNativeMethods("android/hardware/Camera", "setPreviewTexture", "(Landroid/graphics/SurfaceTexture;)V", hooked_setPreviewTexture, &original_setPreviewTexture);
    // Similar for other methods

    // Load settings from shared prefs or file
    // TODO: Read from /data/local/tmp/vcam_settings.json

    LOGI("Hooks installed");
}