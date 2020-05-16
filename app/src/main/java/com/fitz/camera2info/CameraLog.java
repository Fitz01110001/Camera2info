package com.fitz.camera2info;

import android.util.Log;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info
 * @ClassName: CameraLog
 * @Author: Fitz
 * @CreateDate: 2019/12/15 17:24
 */
public class CameraLog {

    public static final String TAG = "Fitz_";
    public static final int LOG_LEVEL = Log.VERBOSE;

    public static void v(String tag, String info) {
        if (LOG_LEVEL <= Log.VERBOSE) {
            Log.v(TAG + tag, info);
        }
    }

    public static void d(String tag, String info) {
        if (LOG_LEVEL <= Log.DEBUG) {
            Log.d(TAG + tag, info);
        }
    }

    public static void i(String tag, String info) {
        if (LOG_LEVEL <= Log.INFO) {
            Log.i(TAG + tag, info);
        }
    }

    public static void w(String tag, String info) {
        if (LOG_LEVEL <= Log.WARN) {
            Log.w(TAG + tag, info);
        }
    }

    public static void e(String tag, String info) {
        if (LOG_LEVEL <= Log.ERROR) {
            Log.e(TAG + tag, info);
        }
    }

}
