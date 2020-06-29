package com.fitz.camera2info.mode;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Size;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.manager.CameraManagerInterface;
import com.fitz.camera2info.storage.StorageRunnable;
import com.fitz.camera2info.utils.Util;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.DIRECTORY_DCIM;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.mode
 * @ClassName: VideoMode
 * @Author: Fitz
 * @CreateDate: 2020/5/23 13:42
 */
public class VideoMode extends BaseMode {
    private static final String TAG = "VideoMode";

    private MediaRecorder mMediaRecorder = null;
    private Integer mSensorOrientation = -1;
    private String mTempVideoPath = "";
    private String mVideoName = "";
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;

    public VideoMode(Context context, CameraManagerInterface cameraManagerInterface, Util util) {

        super(context, cameraManagerInterface, util);
    }

    @Override
    public void beforeOpenCamera() {

        mMediaRecorder = new MediaRecorder();
    }

    @Override
    public ModeManager.ModeName getModeName() {

        return ModeManager.ModeName.VIDEO_MODE;
    }

    public void setupMediarecorder(Size videoSize) throws IOException {

        CameraLog.d(TAG, "setupMediarecorder, videoSize: " + videoSize.getWidth() + "*" + videoSize.getHeight());

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mTempVideoPath == null || mTempVideoPath.isEmpty()) {
            mTempVideoPath = getVideoFilePath();
        }

        CameraLog.d(TAG, "setupMediarecorder, mTempVideoPath: " + mTempVideoPath);
        mMediaRecorder.setOutputFile(mTempVideoPath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
 /*     int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        switch (mSensorOrientation) {
        case SENSOR_ORIENTATION_DEFAULT_DEGREES:
            mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
            break;
        case SENSOR_ORIENTATION_INVERSE_DEGREES:
            mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
            break;
        }*/

        mMediaRecorder.prepare();
    }

    public MediaRecorder getMediaRecorder() {

        return mMediaRecorder;
    }

    private String getVideoFilePath() {

        final File dir = mContext.getExternalFilesDir(DIRECTORY_DCIM);
        mVideoName = System.currentTimeMillis() + ".mp4";
        String absolutePath = dir.getAbsolutePath();
        return (dir == null ? "" : (absolutePath + "/")) + mVideoName;
    }

    public void startRecordingVideo() {

        CameraLog.d(TAG, "startRecordingVideo");

        mMediaRecorder.start();
    }

    public void stopRecordingVideo() {

        CameraLog.d(TAG, "stopRecordingVideo");

        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        if(null == mBackgroundHandler){
            mBackgroundHandler = mCameraManagerInterface.getBackgroundThread();
        }
        mBackgroundHandler.post(new StorageRunnable(mOnSaveState, mTempVideoPath, mVideoName));
    }
}
