package com.fitz.camera2info;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.fitz.camera2info.utils.Util;
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info
 * @ClassName: FitzApplication
 * @Author: Fitz
 * @CreateDate: 2019/12/15 17:22
 */
public class FitzApplication extends Application {

    public static final String TAG = "Application";
    protected Util mUtil = null;

    public FitzApplication() {
        super();
        CameraLog.v(TAG, "FitzApplication");

        QMUISwipeBackActivityManager.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CameraLog.v(TAG, "onCreate");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        CameraLog.v(TAG, "onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        CameraLog.v(TAG, "onLowMemory");
    }
}
