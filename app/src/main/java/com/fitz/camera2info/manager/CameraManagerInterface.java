package com.fitz.camera2info.manager;

import android.media.ImageReader;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.fitz.camera2info.flash.FlashManager;
import com.fitz.camera2info.mode.BaseMode;
import com.fitz.camera2info.mode.ModeManager;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.manager
 * @ClassName: CameraManagerInterface
 * @Author: Fitz
 * @CreateDate: 2020/5/23 13:32
 */
public interface CameraManagerInterface {

    void swicthCamera(int cameraId);
    void switchMode(BaseMode mode);
    Handler getBackgroundThread();
    String getCurrentCameraId();
    void setImageReader(ImageReader imageReader,
                        @Nullable ImageReader mRearMainImageReader,
                        @Nullable ImageReader mRearSecondImageReader, @Nullable ImageReader mRearThirdImageReader);
    void setFlashMode(FlashManager.Flash flashMode);
}
