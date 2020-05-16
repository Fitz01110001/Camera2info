package com.fitz.camera2info.camerainfo;

import android.hardware.camera2.CameraCharacteristics;

import com.fitz.camera2info.CameraLog;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.Adapter
 * @ClassName: CameraItem
 * @Author: Fitz
 * @CreateDate: 2019/12/16 2:01
 */
public class CameraItem {

    private String cameraId = "";
    private String cameraName = "";
    private boolean cameraFront = false;

    private String mPhysicalCameraIds = "";
    private boolean isMulitCamera = false;
    private int cameraDirection = 0;

    public CameraCharacteristics getCameraCharacteristics() {
        return mCameraCharacteristics;
    }

    public CameraItem setCameraCharacteristics(CameraCharacteristics cameraCharacteristics) {
        mCameraCharacteristics = cameraCharacteristics;
        return this;
    }

    private CameraCharacteristics mCameraCharacteristics = null;

    public static CameraItem newInstance() {
        return new CameraItem();
    }

    public String getCameraId() {
        return cameraId;
    }

    public CameraItem setCameraId(String cameraId) {
        this.cameraId = cameraId;
        return this;
    }

    public String getCameraName() {
        return cameraName;
    }

    public CameraItem setCameraName(String cameraName) {
        this.cameraName = cameraName;
        return this;
    }

    public int getCameraDirection() {
        return cameraDirection;
    }

    public CameraItem setCameraDirection(int cameraDirection) {
        this.cameraDirection = cameraDirection;
        return this;
    }

    public boolean getCameraFront() {
        return cameraFront;
    }

    public CameraItem setCameraFront(boolean cameraFront) {
        this.cameraFront = cameraFront;
        return this;
    }

    public boolean isMulitCamera() {
        return isMulitCamera;
    }

    public CameraItem setMulitCamera(boolean mulitCamera) {
        isMulitCamera = mulitCamera;
        return this;
    }

    public String getPhysicalCameraIds() {
        return mPhysicalCameraIds;
    }

    public CameraItem setPhysicalCameraIds(String physicalCameraIds) {
        mPhysicalCameraIds = physicalCameraIds;
        return this;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("cameraId: ").append(cameraId).append("\n\t")
                     .append("cameraFront: ").append(cameraFront).append("\n\t")
                     .append("isMulitCamera: ").append(isMulitCamera).append("\n\t")
                     .append("mPhysicalCameraIds: ").append(mPhysicalCameraIds).append("\n\t")
                     .append("cameradirection: ").append(cameraDirection);

        return stringBuilder.toString();
    }
}
