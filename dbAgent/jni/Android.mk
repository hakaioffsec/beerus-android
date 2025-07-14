LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := dbAgent
LOCAL_SRC_FILES := agent.cpp
LOCAL_LDLIBS    := -llog -ldl
include $(BUILD_EXECUTABLE)