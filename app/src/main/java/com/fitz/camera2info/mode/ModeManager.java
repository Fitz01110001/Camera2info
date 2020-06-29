package com.fitz.camera2info.mode;

import android.content.Context;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.manager.CameraManagerInterface;
import com.fitz.camera2info.storage.StorageRunnable;
import com.fitz.camera2info.ui.CameraUIInterface;
import com.fitz.camera2info.utils.Util;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.mode
 * @ClassName: ModeManager
 * @Author: Fitz
 * @CreateDate: 2020/5/23 12:14
 */
public class ModeManager implements ModeManagerInterface {
    private static final String TAG = "ModeManager";

    public ModeName mCurrentModeName = ModeName.PHOTO_MODE;
    private Context mContext = null;
    private CameraUIInterface mCameraUIInterface = null;
    private CameraManagerInterface mCameraManagerInterface = null;
    private BaseMode mCameraMode = null;
    private Util mUtil = null;
    private HashMap<ModeName, BaseMode> mBaseModeMap = new LinkedHashMap<>();

    public enum ModeName {
        PHOTO_MODE,
        VIDEO_MODE,
    }

    public ModeManager(Context context, CameraUIInterface cameraUIInterface, CameraManagerInterface cameraManagerInterface, Util util) {

        mContext = context;
        mCameraUIInterface = cameraUIInterface;
        mCameraManagerInterface = cameraManagerInterface;
        mUtil = util;

        initBaseModeMap();
        mCameraMode = getCameraMode(ModeName.PHOTO_MODE);
    }

    private void initBaseModeMap() {

        mBaseModeMap.put(ModeName.PHOTO_MODE, new CapMode(mContext, mCameraManagerInterface, mUtil));
        mBaseModeMap.put(ModeName.VIDEO_MODE, new VideoMode(mContext, mCameraManagerInterface, mUtil));
    }

    public void onCreate(String cameraId, StorageRunnable.onSaveState state) {

        for (BaseMode mode : mBaseModeMap.values()) {
            mode.onCreate(cameraId, state);
        }
    }

    public BaseMode getCameraMode(ModeName modeName) {

        return mBaseModeMap.get(modeName);
    }

    public ModeName getCurrentModeName() {

        return mCurrentModeName;
    }

    public BaseMode getCameraMode() {

        if (null != mCameraMode) {
            CameraLog.d(TAG, "getCameraMode, mCameraMode: " + mCameraMode.getModeName());

            return mCameraMode;
        } else {
            CameraLog.d(TAG, "getCameraMode, mCameraMode: new CapMode");

            return getCameraMode(ModeName.PHOTO_MODE);
        }
    }

    public VideoMode getVideoMode() {

        return (VideoMode) getCameraMode(ModeName.VIDEO_MODE);
    }

    public void switchCameraMode(ModeName modeName) {

        if (mCurrentModeName == modeName) {
            CameraLog.d(TAG, "setCurrentMode, same mode, do nothing ");
        } else {
            mCurrentModeName = modeName;
            mCameraUIInterface.setUICurrentMode(modeName);

            switch (modeName) {
            case PHOTO_MODE:
                mCameraMode = getCameraMode(ModeName.PHOTO_MODE);

                break;
            case VIDEO_MODE:
                mCameraMode = getCameraMode(ModeName.VIDEO_MODE);

                break;
            }

            mCameraManagerInterface.switchMode(mCameraMode);
        }
    }

}
