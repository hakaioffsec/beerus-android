LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := beerusd
LOCAL_SRC_FILES := beerusd.c

LOCAL_LDLIBS    := -llog -ldl -lm -pthread
LOCAL_LDFLAGS   := -Wl,--export-dynamic

include $(BUILD_EXECUTABLE)
include $(CLEAR_VARS)