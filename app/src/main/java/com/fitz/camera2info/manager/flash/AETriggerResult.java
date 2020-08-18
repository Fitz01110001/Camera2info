package com.fitz.camera2info.manager.flash;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import androidx.annotation.NonNull;

import com.fitz.camera2info.CameraLog;

import java.util.HashSet;
import java.util.Set;

import static android.hardware.camera2.CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.manager.flash
 * @ClassName: AETriggerResult
 * @Author: Fitz
 * @CreateDate: 2020/8/18 23:37
 */
public class AETriggerResult extends CameraCaptureSession.CaptureCallback {
    private String TAG = "AETriggerResult";
    private long onConvergedFrame = -1;
    private int mCurrentAETriggerState = CONTROL_AE_PRECAPTURE_TRIGGER_IDLE;
    private static TriggerStateMachine mStateMachine = null;

    private static final Set<Integer> TRIGGER_DONE_STATES = new HashSet<Integer>() {
        {
            add(CaptureResult.CONTROL_AE_STATE_INACTIVE);
            add(CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED);
            add(CaptureResult.CONTROL_AE_STATE_CONVERGED);
            add(CaptureResult.CONTROL_AE_STATE_LOCKED);
        }
    };

    public AETriggerResult() {
        super();

        mStateMachine = new TriggerStateMachine(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START, TRIGGER_DONE_STATES);
    }

    public void converged(boolean converged) {

    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);

        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        Integer aeTriggerState = result.get(CaptureResult.CONTROL_AE_PRECAPTURE_TRIGGER);

        CameraLog.v(TAG, "AETriggerResult, onCaptureCompleted, mCurrentAEState, " + mCurrentAETriggerState + ", aeState: " + aeState + ", " +
                         "aeTriggerState: " + aeTriggerState);

        if (CONTROL_AE_PRECAPTURE_TRIGGER_START == aeTriggerState) {
            mCurrentAETriggerState = aeTriggerState;
        }

        if (CONTROL_AE_PRECAPTURE_TRIGGER_START == mCurrentAETriggerState) {
            boolean done = mStateMachine.update(result.getFrameNumber(),
                                                result.getRequest()
                                                      .get(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER),
                                                aeState);

            if (done && onConvergedFrame == -1) {
                onConvergedFrame = result.getFrameNumber();

                CameraLog.d(TAG,
                            "AETriggerResult, onCaptureCompleted, aeState: " + aeState + ", done: " + done + ", onConvergedFrame: " + onConvergedFrame);
                converged(done);

                mCurrentAETriggerState = CONTROL_AE_PRECAPTURE_TRIGGER_IDLE;
            }
        }

    }

    public void reset(){
        onConvergedFrame = -1;
        mCurrentAETriggerState = CONTROL_AE_PRECAPTURE_TRIGGER_IDLE;
    }
}
