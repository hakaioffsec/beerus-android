LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := fridaCore
LOCAL_SRC_FILES := frida-core.c

LOCAL_LDLIBS    := -llog -ldl -lm -pthread
LOCAL_STATIC_LIBRARIES := libfrida-core
LOCAL_LDFLAGS   := -Wl,--export-dynamic

include $(BUILD_EXECUTABLE)
include $(CLEAR_VARS)

LOCAL_MODULE := libfrida-core
LOCAL_SRC_FILES := libs/$(TARGET_ARCH_ABI)/libfrida-core.a

include $(PREBUILT_STATIC_LIBRARY)
