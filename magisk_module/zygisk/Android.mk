LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := zygisk_vcam
LOCAL_SRC_FILES := zygisk_vcam.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_LDLIBS := -llog -landroid -lmediandk
LOCAL_CPPFLAGS += -std=c++17

include $(BUILD_SHARED_LIBRARY)