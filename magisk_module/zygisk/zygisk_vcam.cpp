#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <unistd.h>
#include <sys/system_properties.h>
#include <media/NdkMediaCodec.h>
#include <media/NdkMediaExtractor.h>
#include <media/NdkMediaFormat.h>

#define LOG_TAG "VCAM_ZYGISK"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Adapted from VideoToFrames.java
class VideoDecoder {
public:
    VideoDecoder() {}
    ~VideoDecoder() {}

    bool init(const char* videoPath, ANativeWindow* surface) {
        // Initialize MediaExtractor and MediaCodec
        // This is a simplified version; full implementation needed
        extractor_ = AMediaExtractor_new();
        if (AMediaExtractor_setDataSource(extractor_, videoPath) != AMEDIA_OK) {
            LOGE("Failed to set data source");
            return false;
        }
        // Select video track
        size_t trackCount = AMediaExtractor_getTrackCount(extractor_);
        for (size_t i = 0; i < trackCount; ++i) {
            AMediaFormat* format = AMediaExtractor_getTrackFormat(extractor_, i);
            const char* mime;
            if (AMediaFormat_getString(format, AMEDIAFORMAT_KEY_MIME, &mime) && strncmp(mime, "video/", 6) == 0) {
                AMediaExtractor_selectTrack(extractor_, i);
                codec_ = AMediaCodec_createDecoderByType(mime);
                AMediaCodec_configure(codec_, format, surface, nullptr, 0);
                AMediaCodec_start(codec_);
                AMediaFormat_delete(format);
                return true;
            }
            AMediaFormat_delete(format);
        }
        return false;
    }

    void decodeFrame(uint8_t* buffer) {
        // Decode and fill buffer with NV21 data
        // Simplified; needs full implementation
        AMediaCodecBufferInfo info;
        ssize_t bufIdx = AMediaCodec_dequeueOutputBuffer(codec_, &info, 10000);
        if (bufIdx >= 0) {
            size_t bufSize;
            uint8_t* buf = AMediaCodec_getOutputBuffer(codec_, bufIdx, &bufSize);
            memcpy(buffer, buf, bufSize);
            AMediaCodec_releaseOutputBuffer(codec_, bufIdx, true);
        }
    }

    void release() {
        if (codec_) AMediaCodec_stop(codec_);
        if (codec_) AMediaCodec_delete(codec_);
        if (extractor_) AMediaExtractor_delete(extractor_);
    }

private:
    AMediaExtractor* extractor_ = nullptr;
    AMediaCodec* codec_ = nullptr;
};

// Global variables
static VideoDecoder* g_decoder = nullptr;
static bool g_spoof_enabled = false;
static char g_video_path[256] = "/sdcard/DCIM/Camera1/virtual.mp4";
static char g_image_path[256] = "/sdcard/DCIM/Camera1/1000.bmp";

// Hook functions for Camera APIs
// Simplified hooks; full implementation needed

extern "C" JNIEXPORT void JNICALL
Java_android_hardware_Camera_setPreviewTexture(JNIEnv* env, jobject thiz, jobject surfaceTexture) {
    LOGI("Hooked setPreviewTexture");
    if (g_spoof_enabled) {
        // Replace with fake surface
        // Implementation needed
    }
    // Call original
}

extern "C" JNIEXPORT void JNICALL
Java_android_hardware_Camera_startPreview(JNIEnv* env, jobject thiz) {
    LOGI("Hooked startPreview");
    if (g_spoof_enabled && g_decoder) {
        // Start decoding video
        // Implementation needed
    }
    // Call original
}

// Zygisk module entry point
extern "C" void zygisk_module_entry(zygisk::Api* api) {
    LOGI("VCam Zygisk module loaded");
    // Hook JNI functions
    // api->hookJniNativeMethods(...);
    // Full hooking setup needed
}