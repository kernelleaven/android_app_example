LOCAL_PATH:= $(call my-dir)

#include $(CLEAR_VARS)
#LOCAL_MODULE := libPlatinumJNI
#LOCAL_MODULE_SUFFIX=.so
#LOCAL_MODULE_TAGS :=  user
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES 
#LOCAL_MODULE_PATH :=$(TARGET_OUT_SHARED_LIBRARIES) 
#LOCAL_SRC_FILES :=libPlatinumJNI.so
#include $(BUILD_PREBUILT)

#LOCAL_MODULE :=libPlatinumJNI
#LOCAL_SRC_FILES :=libPlatinumJNI.so
#include $(PREBUILT_SHARED_LIBRARY)
#include $(BUILD_PREBUILT)

#skyworth+copy lib libHA.AUDIO.DTS.decode.so to system/lib
#include $(CLEAR_VARS)
#LOCAL_MODULE := libPlatinumJNI.so
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_SRC_FILES := libPlatinumJNI.so
#include $(BUILD_PREBUILT)
#

#skyworth+copy lib libHA.AUDIO.DTS.decode.so to system/lib
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := libPlatinumJNI.so
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_SRC_FILES := libPlatinumJNI.so
include $(BUILD_PREBUILT)
