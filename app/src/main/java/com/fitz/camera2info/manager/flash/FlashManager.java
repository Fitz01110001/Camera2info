package com.fitz.camera2info.manager.flash;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.view.Surface;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.R;
import com.fitz.camera2info.manager.BaseStateManager;
import com.fitz.camera2info.manager.Camera2Manager;
import com.fitz.camera2info.manager.CameraManagerInterface;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static android.hardware.camera2.CameraMetadata.*;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.manager.flash
 * @ClassName: FlashManager
 * @Author: Fitz
 * @CreateDate: 2020/6/6 19:12
 */
public class FlashManager extends BaseStateManager {
    private static final String TAG = "FlashManager";


    private int mFlashButtonImage = R.drawable.flash_off;
    private Flash mFlashState = Flash.OFF;
    private ImageView mFlashButton = null;
    private Context mContext = null;
    private CameraManagerInterface mCameraManagerInterface = null;
    private boolean mNeedCapture = true;


    public enum Flash {
        ON,
        OFF,
        AUTO,
    }

    public FlashManager(Context context, ImageView view, CameraManagerInterface cameraManagerInterface) {
        mContext = context;
        mFlashButton = view;
        mCameraManagerInterface = cameraManagerInterface;
        updateFlashState(mFlashButtonImage);
    }

    public void onFlashClick() {

        switch (mFlashState) {
        case ON:
            mFlashState = Flash.AUTO;
            mFlashButtonImage = R.drawable.flash_auto;
            break;
        case AUTO:
            mFlashState = Flash.OFF;
            mFlashButtonImage = R.drawable.flash_off;
            break;
        case OFF:
            mFlashState = Flash.ON;
            mFlashButtonImage = R.drawable.flash_on;
            break;
        }

        CameraLog.d(TAG, "onFlashClick, new FlashState: " + mFlashState);

        updateFlashState(mFlashButtonImage);
    }

    private void updateFlashState(int flashStateDrawable) {

        CameraLog.d(TAG, "updateFlashButton, state: " + mFlashState);
        mCameraManagerInterface.setFlashMode(mFlashState);
        mFlashButton.setImageDrawable(ContextCompat.getDrawable(mContext, flashStateDrawable));
    }

}
