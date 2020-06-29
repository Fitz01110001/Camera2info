package com.fitz.camera2info.mode;

import android.content.Context;
import android.os.Handler;

import com.fitz.camera2info.manager.CameraManagerInterface;
import com.fitz.camera2info.storage.StorageRunnable;
import com.fitz.camera2info.utils.Util;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.mode
 * @ClassName: BaseMode
 * @Author: Fitz
 * @CreateDate: 2020/5/23 14:59
 */
public abstract class BaseMode {
    protected CameraManagerInterface mCameraManagerInterface = null;
    protected Util mUtil = null;
    protected Context mContext = null;
    protected StorageRunnable.onSaveState mOnSaveState = null;
    protected String mCurrentCameraId = "";
    protected Handler mBackgroundHandler = null;

    public BaseMode(Context context, CameraManagerInterface cameraManagerInterface, Util util) {

        mContext = context;
        mCameraManagerInterface = cameraManagerInterface;
        mUtil = util;
        mBackgroundHandler = mCameraManagerInterface.getBackgroundThread();
    }

    public void onCreate(String cameraId, StorageRunnable.onSaveState state){
        mCurrentCameraId = cameraId;
        mOnSaveState = state;
    }

    public abstract void beforeOpenCamera();

    public abstract ModeManager.ModeName getModeName();

}
