package com.fitz.camera2info;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class CamManager {

    private static String TAG = "Fitz- Log";
    private CameraManager mCameraManager;
    private Context mContext;
    private CameraCharacteristics cameraCharacteristics;

    public CamManager(Context mContext) {
        this.mContext = mContext;
    }

    public void create() throws CameraAccessException, IOException {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        String[] cameraList = mCameraManager.getCameraIdList();

        log("This phone has " + cameraList.length + " camera(s)");
        for (String cameraId : cameraList) {

            cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
            Set<String> phCameraId = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                phCameraId = cameraCharacteristics.getPhysicalCameraIds();
            }
            log("cameraId:" + cameraId + " PhysicalCameraId:" + phCameraId.toString());
            saveCameraInfoForID(cameraId, phCameraId);

        }
    }

    private void saveCameraInfoForID(String cameraId, Set<String> phCameraId) throws IOException {
        //save cameraCharacteristics.keys
        StringBuilder sb = new StringBuilder();
        sb.append("cameraId: ")
          .append(cameraId)
          .append(" PhysicalCameraId: ")
          .append(phCameraId.toString())
          .append(" CameraCharacteristics.keys:")
          .append("\n");
        List<CameraCharacteristics.Key<?>> cameraKays = cameraCharacteristics.getKeys();
        for (CameraCharacteristics.Key<?> key : cameraKays) {
            sb.append(key.getName()).append("\n");
        }
        sb.append("\n");
        saveUtil.write(mContext, "cameraId:" + cameraId + ".txt", sb.toString());
        log("\n" + sb.toString() + "\n");

        //save CaptureRequest.key
        sb.delete(0, sb.length());
        sb.append(" CaptureRequest.keys:");
        List<CaptureRequest.Key<?>> captureRequestKeys = cameraCharacteristics.getAvailableCaptureRequestKeys();
        for (CaptureRequest.Key key : captureRequestKeys) {
            sb.append(key.getName()).append("\n");
        }
        saveUtil.write(mContext, "CaptureRequestKeys.txt", sb.toString());
        log("\n" + sb.toString() + "\n");

        //save CaptureResult.key
        sb.delete(0, sb.length());
        sb.append(" CaptureResult.keys:");
        List<CaptureResult.Key<?>> captureResultKeys = cameraCharacteristics.getAvailableCaptureResultKeys();
        for (CaptureResult.Key<?> key : captureResultKeys) {
            sb.append(key.getName()).append("\n");
        }
        sb.append("\n");
        saveUtil.write(mContext, "CaptureResult.txt", sb.toString());
        log("\n" + sb.toString() + "\n");
    }


    private void log(String s) {
        Log.d(TAG, s);
    }


}
