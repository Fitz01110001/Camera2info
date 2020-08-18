package com.fitz.camera2info.manager.flash;

import android.hardware.camera2.CaptureResult;

import androidx.annotation.Nullable;

import com.fitz.camera2info.CameraLog;

import java.util.Set;


/**
 * Tracks the finite state machines used by the camera2 api for AF and AE
 * triggers. That is, the state machine waits for a TRIGGER_START followed by
 * one of the done states, at which point a callback is invoked and the state
 * machine resets.
 * <p>
 * In other words, this implements the state machine defined by the following
 * regex, such that a callback is invoked each time the state machine reaches
 * the end.
 *
 * <pre>
 * (.* TRIGGER_START .* [DONE_STATES])+
 * </pre>
 * <p>
 * See the android documentation for {@link CaptureResult#CONTROL_AF_STATE} and
 * {@link CaptureResult#CONTROL_AE_STATE} for the transition tables which this
 * is based on.
 */

public final class TriggerStateMachine {
    private String TAG = "TriggerStateMachine";

    private enum State {
        WAITING_FOR_TRIGGER,
        TRIGGERED
    }

    private final int mTriggerStart;
    private final Set<Integer> mDoneStates;
    private State mCurrentState;
    @Nullable private Long mLastTriggerFrameNumber;
    @Nullable private Long mLastFinishFrameNumber;

    public TriggerStateMachine(int triggerStart, Set<Integer> doneStates) {
        mTriggerStart = triggerStart;
        mDoneStates = doneStates;
        mCurrentState = State.WAITING_FOR_TRIGGER;
        mLastTriggerFrameNumber = null;
        mLastFinishFrameNumber = null;
    }

    /**
     * @return True upon completion of a cycle of the state machine.
     */
    public boolean update(long frameNumber, @Nullable Integer triggerState, @Nullable Integer state) {
        boolean triggeredNow = triggerState != null && triggerState == mTriggerStart;
        boolean doneNow = mDoneStates.contains(state);

        CameraLog.v(TAG,"update, frameNumber: " + frameNumber + ", triggerState: " + triggerState + ", state: " + state);

        if (mCurrentState == State.WAITING_FOR_TRIGGER) {
            if (mLastTriggerFrameNumber == null || frameNumber > mLastTriggerFrameNumber) {
                if (triggeredNow) {
                    mCurrentState = State.TRIGGERED;
                    mLastTriggerFrameNumber = frameNumber;
                }
            }
        }

        if (mCurrentState == State.TRIGGERED) {
            if (mLastFinishFrameNumber == null || frameNumber > mLastFinishFrameNumber) {
                if (doneNow) {
                    mCurrentState = State.WAITING_FOR_TRIGGER;
                    mLastFinishFrameNumber = frameNumber;
                    return true;
                }
            }
        }

        return false;
    }
}
