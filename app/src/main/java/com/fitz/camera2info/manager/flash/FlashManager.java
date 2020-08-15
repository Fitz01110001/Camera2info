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

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.manager.flash
 * @ClassName: FlashManager
 * @Author: Fitz
 * @CreateDate: 2020/6/6 19:12
 */
public class FlashManager extends BaseStateManager {
    private static final String TAG = "FlashManager";

    private static TriggerStateMachine mStateMachine = null;

    private int mFlashButtonImage = R.drawable.flash_off;
    private Flash mFlashState = Flash.OFF;
    private ImageView mFlashButton = null;
    private Context mContext = null;
    private CameraManagerInterface mCameraManagerInterface = null;
    private boolean mNeedCapture = true;


    private static final Set<Integer> TRIGGER_DONE_STATES = new HashSet<Integer>() {
        {
            add(CaptureResult.CONTROL_AE_STATE_INACTIVE);
            add(CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED);
            add(CaptureResult.CONTROL_AE_STATE_CONVERGED);
            add(CaptureResult.CONTROL_AE_STATE_LOCKED);
        }
    };

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

        mStateMachine = new TriggerStateMachine(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START, TRIGGER_DONE_STATES);
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

    public Flash getFlashState() {

        return mFlashState;
    }

    public static class AETriggerResult extends CameraCaptureSession.CaptureCallback {
        private long onConvergedFrame = 0;

        public AETriggerResult() {

            super();
        }

        public void converged(boolean converged) {

        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            boolean done = mStateMachine.update(result.getFrameNumber(),
                                                result.getRequest()
                                                      .get(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER),
                                                aeState);

            if (done && onConvergedFrame == 0) {
                onConvergedFrame = result.getFrameNumber();

                CameraLog.d(TAG,
                            "AETriggerResult, onCaptureCompleted, aeState: " + aeState + ", done: " + done + ", onConvergedFrame: " + onConvergedFrame);
                converged(done);
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            converged(false);
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target,
                long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
        }
    }
}
