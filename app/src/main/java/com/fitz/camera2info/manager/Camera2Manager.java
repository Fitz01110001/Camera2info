package com.fitz.camera2info.manager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.camerainfo.CameraItem;
import com.fitz.camera2info.flash.FlashManager;
import com.fitz.camera2info.mode.BaseMode;
import com.fitz.camera2info.mode.ModeManager;
import com.fitz.camera2info.mode.ModeManagerInterface;
import com.fitz.camera2info.storage.StorageRunnable;
import com.fitz.camera2info.utils.RequestValue;
import com.fitz.camera2info.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.manager
 * @ClassName: Camera2Manager
 * @Author: Fitz
 * @CreateDate: 2019/12/16 0:47
 */
public class Camera2Manager implements CameraManagerInterface {

    private static final String TAG = "Camera2Manager";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final float defaultZoomRatio = 1.0f;
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private volatile static Camera2Manager camera2Manager;


    private int mSensorOrientation = 0;
    private float mMaxZoom = 1.0f;
    private String mCameraId = "-1";
    private String imageName = null;
    private boolean mIsRecordingVideo = false;
    private FlashManager.Flash mFlashState = FlashManager.Flash.OFF;

    private CameraManager mCameraManager = null;
    private CameraCaptureSession mSession = null;
    private CameraCharacteristics mCameraCharacteristics = null;
    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mPreviewBuilder = null;
    private CaptureRequest mPreviewRequest = null;

    private Handler mBackgroundHandler = null;
    private HandlerThread mBackgroundThread = null;
    private Rect mSensorRect = null;
    private Rect mCurrentRect = null;

    private Size mPreviewSize = null;
    private Size mDefaultSize = null;
    private Size mVideoSize = null;
    private Surface mPreviewSurface = null;
    private TextureView mTextureView = null;
    private HashMap<String, BuilderCache<?>> mRequestHashMap = new HashMap<>();


    private Hashtable<String, CameraItem> mCameraItemHashtable = new Hashtable<>();
    private Activity mContext = null;
    private Util mUtil = null;
    private ImageReader mImageReader = null;
    private ImageReader mRearMainImageReader = null;
    private ImageReader mRearSecondImageReader = null;
    private ImageReader mRearThirdImageReader = null;
    private Executor executor = new Executor() {
        @Override
        public void execute(Runnable command) {

        }
    };
    private CameraStateCallback mCameraStateCallback = null;
    private ModeManager.ModeName mModeName = ModeManager.ModeName.PHOTO_MODE;
    private ModeManagerInterface mModeManagerInterface = null;

    private enum Request {
        PREVIEW,
        CAPTURE,
    }

    public void setModeManagerInterface(ModeManagerInterface modeManagerInterface) {

        mModeManagerInterface = modeManagerInterface;
    }

    private Camera2Manager(Activity context) {

        mContext = context;
        mUtil = Util.getUtil(mContext);
        mCameraManager = mUtil.getCameraManager();
    }

    public static Camera2Manager getCamera2Manager(Activity context) {

        if (camera2Manager == null) {
            synchronized (Camera2Manager.class) {
                if (camera2Manager == null) {
                    camera2Manager = new Camera2Manager(context);
                }
            }
        }
        return camera2Manager;
    }

    public void onCreate(String cameraId, CameraStateCallback cameraStateCallback) {

        CameraLog.d(TAG, "onCreate, cameraId:" + cameraId);
        mCameraId = cameraId;
        mCameraStateCallback = cameraStateCallback;

        startBackgroundThread();

    }

    public void onResume(TextureView textureView) {

        CameraLog.d(TAG, "onResume");

        mTextureView = textureView;
    }

    private void setupPreview() {

        CameraLog.d(TAG, "setupPreview");

        mCameraCharacteristics = mUtil.getCameraItem(mCameraId)
                                      .getCameraCharacteristics();
        StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        mPreviewSize = choosePreview4_3Size(map.getOutputSizes(ImageFormat.JPEG));
        mDefaultSize = mUtil.getDefaultSizeByCameraId(mCameraId);
        if (ModeManager.ModeName.VIDEO_MODE == mModeName) {
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

            CameraLog.d(TAG, "setupPreview, mVideoSize: " + mVideoSize.getHeight() + "*" + mVideoSize.getWidth());
        }

        int rotatedPreviewWidth = mDefaultSize.getWidth();
        int rotatedPreviewHeight = mDefaultSize.getHeight();

        // Find out if we need to swap dimension to get the preview size relative to sensor
        // coordinate.
        int displayRotation = mContext.getWindowManager()
                                      .getDefaultDisplay()
                                      .getRotation();
        //noinspection ConstantConditions
        mSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        CameraLog.d(TAG, "setupTextureView, mSensorOrientation: " + mSensorOrientation);

        boolean swappedDimensions = false;
        switch (displayRotation) {
        case Surface.ROTATION_0:
            //go next
        case Surface.ROTATION_180:
            if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                swappedDimensions = true;
            }
            break;
        case Surface.ROTATION_90:
            //go next
        case Surface.ROTATION_270:
            if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                swappedDimensions = true;
            }
            break;
        default:
            CameraLog.e(TAG, "setupTextureView, Display rotation is invalid: " + displayRotation);
            break;
        }

        if (swappedDimensions) {
            rotatedPreviewWidth = mDefaultSize.getHeight();
            rotatedPreviewHeight = mDefaultSize.getWidth();
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(rotatedPreviewWidth, rotatedPreviewHeight);
        mPreviewSurface = new Surface(texture);

        CameraLog.d(TAG, "setupTextureView X, W: " + rotatedPreviewWidth + ", H:" + rotatedPreviewHeight);
    }

    private Size choosePreview4_3Size(Size[] outputSizes) {

        for (Size size : outputSizes) {
            float ratio = (float) size.getWidth() / (float) size.getHeight();
            CameraLog.d(TAG, "choosePreviewSize, size: " + size.toString() + ", ratio: " + ratio);

            if (Math.abs(ratio - Util.SIZE_4_3) == 0) {

                CameraLog.d(TAG, "choosePreviewSize, find 4_3 size : " + size.toString());
                return size;
            }
        }
        return null;
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private static Size chooseVideoSize(Size[] choices) {

        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }

        CameraLog.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    public void onPause() {

    }

    public void onStop() {

        closeCamera();
    }


    private void setUpCameraOutputs() {

        mSensorRect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        mMaxZoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        mCurrentRect = cropRegionForZoom(defaultZoomRatio);

    }

    private Rect cropRegionForZoom(float ratio) {

        CameraLog.d(TAG, "ratio:" + ratio);
        int xCenter = mSensorRect.width() / 2;
        int yCenter = mSensorRect.height() / 2;
        int xDelta = (int) (0.5f * mSensorRect.width() / ratio);
        int yDelta = (int) (0.5f * mSensorRect.height() / ratio);
        /*CameraLog.d(TAG, "xCenter:" + xCenter);
        CameraLog.d(TAG, "yCenter:" + yCenter);
        CameraLog.d(TAG, "xDelta:" + xDelta);
        CameraLog.d(TAG, "yDelta:" + yDelta);*/
        return new Rect(xCenter - xDelta, yCenter - yDelta, xCenter + xDelta, yCenter + yDelta);
    }

    public void closeCamera() {

        if (mCameraDevice != null) {
            synchronized (mCameraDevice) {
                if (null != mSession) {
                    try {
                        mSession.stopRepeating();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    mSession.close();
                    mSession = null;
                }
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
                if (null != mImageReader) {
                    mImageReader.close();
                    mImageReader = null;
                }
            }
        }
    }

    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private void startBackgroundThread() {

        mBackgroundThread = new HandlerThread("CameraManualTestBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * preview Step 1, open camera
     */
    public void openCamera(String cameraId) {

        CameraLog.d(TAG, "openCamera");

        mCameraId = cameraId;
        setupPreview();
        mModeManagerInterface.getCameraMode()
                             .beforeOpenCamera();

        try {
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            setUpCameraOutputs();
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO 权限检查
                //requestCameraPermission();
            } else {
                CameraLog.d(TAG, "open cameraId:" + mCameraId);
                mCameraManager.openCamera(mCameraId, mDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * preview step 2, camera opened, get cameradevice
     */
    private CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {

        String StateCallbackTAG = "StateCallback";

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            mCameraStateCallback.onOpened(camera);
            CameraLog.d(StateCallbackTAG, "onOpened " + camera.getId());
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

            mCameraStateCallback.onDisconnected(camera);
            CameraLog.d(StateCallbackTAG, "onDisconnected " + camera.getId());
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {

            super.onClosed(camera);
            mCameraStateCallback.onClosed(camera);
            CameraLog.d(StateCallbackTAG, "onClosed," + camera.getId());
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

            mCameraStateCallback.onError(camera, error);
            CameraLog.d(StateCallbackTAG, "onError" + camera.getId());
        }
    };

    /**
     * preview step 3, use cameradevice createCaptureSession
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void startPreview() {

        List<OutputConfiguration> currentOutputs = new ArrayList<>();
        OutputConfiguration outputConfiguration = null;


        CameraLog.d(TAG, "startPreview");
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            if (mPreviewSurface != null) {
                mPreviewBuilder.addTarget(mPreviewSurface);
                outputConfiguration = new OutputConfiguration(mPreviewSurface);
                currentOutputs.add(outputConfiguration);
            }

            if (ModeManager.ModeName.PHOTO_MODE == mModeName) {
                /*if (mUtil.isMultiCam(mCameraId)) {
                    CameraLog.d(TAG, "startPreview, multi mode");

                    outputConfiguration = new OutputConfiguration(mRearMainImageReader.getSurface());
                    CameraLog.d(TAG, "startPreview, id: " + mUtil.getPhysicalCameraIdsArray(mCameraId)[0]);
                    outputConfiguration.setPhysicalCameraId(mUtil.getPhysicalCameraIdsArray(mCameraId)[0]);
                    currentOutputs.add(outputConfiguration);

                    outputConfiguration = new OutputConfiguration(mRearSecondImageReader.getSurface());
                    CameraLog.d(TAG, "startPreview, id: " + mUtil.getPhysicalCameraIdsArray(mCameraId)[1]);
                    outputConfiguration.setPhysicalCameraId(mUtil.getPhysicalCameraIdsArray(mCameraId)[1]);
                    currentOutputs.add(outputConfiguration);

                    if (mUtil.isTripleCam(mCameraId)) {
                        outputConfiguration = new OutputConfiguration(mRearThirdImageReader.getSurface());
                        CameraLog.d(TAG, "startPreview, id: " + mUtil.getPhysicalCameraIdsArray(mCameraId)[2]);
                        outputConfiguration.setPhysicalCameraId(mUtil.getPhysicalCameraIdsArray(mCameraId)[2]);
                        currentOutputs.add(outputConfiguration);
                    }

                    CaptureRequest request = mPreviewBuilder.build();
                    HandlerExecutor executor = new HandlerExecutor(mBackgroundHandler);
                    SessionConfiguration config = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR,
                                                                           currentOutputs,
                                                                           executor,
                                                                           stateCallback);
                    config.setSessionParameters(request);
                    mCameraDevice.createCaptureSession(config);
                    //mCameraDevice.createCaptureSessionByOutputConfigurations(currentOutputs, stateCallback, mBackgroundHandler);
                } else */
                {
                    CameraLog.d(TAG, "startPreview, single photo mode");

                    mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()), stateCallback, mBackgroundHandler);
                }
            } else if (ModeManager.ModeName.VIDEO_MODE == mModeName) {
                mCameraDevice.createCaptureSession(Collections.singletonList(mPreviewSurface), stateCallback, mBackgroundHandler);
            }


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * preview step 4, Session is configured
     */
    CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

            synchronized (mCameraDevice) {
                CameraLog.d(TAG, "onConfigured");
                // The camera is already closed
                if (null == mCameraDevice) {
                    return;
                }

                // When the session is ready, we start displaying the preview.
                mSession = cameraCaptureSession;

                // Auto focus should be continuous for camera preview.
                if (mPreviewBuilder != null) {
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                }

                mCameraStateCallback.onConfigured(cameraCaptureSession);
                setRepeatingPreview(null);
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

            CameraLog.d(TAG, "onConfigureFailed");

            mCameraStateCallback.onConfigureFailed(cameraCaptureSession);
        }
    };

    /**
     * preview setp 5, use session setRequset displaying the camera preview.
     */
    private void setRepeatingPreview(CameraCaptureSession.CaptureCallback captureCallback) {

        if (null == mPreviewBuilder) {
            return;
        }

        setBuilderCache(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        addCacheToBuilder(mPreviewBuilder);
        mPreviewRequest = mPreviewBuilder.build();
        try {
            if (mSession != null) {
                mSession.setRepeatingRequest(mPreviewRequest, captureCallback == null ? mCaptureCallback : captureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * preview finally, check request callback here
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        String CaptureTAG = "CaptureCallback";

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {

            super.onCaptureStarted(session, request, timestamp, frameNumber);
            mCameraStateCallback.onCaptureStarted(session, request, timestamp, frameNumber);
            //CameraLog.d(CaptureTAG, "onCaptureStarted");
        }

        @Override
        public void onCaptureProgressed(
                @NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {

            super.onCaptureProgressed(session, request, partialResult);
            mCameraStateCallback.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {

            super.onCaptureCompleted(session, request, result);
            mCameraStateCallback.onCaptureCompleted(session, request, result);

        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {

            super.onCaptureSequenceAborted(session, sequenceId);
            mCameraStateCallback.onCaptureSequenceAborted(session, sequenceId);
            CameraLog.e(CaptureTAG, "onCaptureSequenceAborted");
        }

        @Override
        public void onCaptureBufferLost(
                @NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {

            super.onCaptureBufferLost(session, request, target, frameNumber);
            mCameraStateCallback.onCaptureBufferLost(session, request, target, frameNumber);
            CameraLog.e(CaptureTAG, "onCaptureBufferLost");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {

            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            mCameraStateCallback.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            CameraLog.d(CaptureTAG, "onCaptureSequenceCompleted");
            if(mFlashState == FlashManager.Flash.ON){
                resetFlashRequest();
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {

            super.onCaptureFailed(session, request, failure);
            mCameraStateCallback.onCaptureFailed(session, request, failure);
            CameraLog.e(CaptureTAG, "onCaptureFailed");
        }
    };

    private void resetFlashRequest() {

    }

    /**
     * capture step 1, 生成照片名称，发出声音，发出拍照请求
     */
    public String takeCapture() {

        imageName = StorageRunnable.getImageName();

        CameraLog.d(TAG, "takeShot, imageName" + imageName);

        mUtil.playSound();
        sendShotRequest();
        return imageName;
    }

    /**
     * capture step 2, 发出拍照请求
     */
    private void sendShotRequest() {

        CameraLog.d(TAG, "sendShotRequest");

        try {
            CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            // Orientation
            int rotation = ((Activity) mContext).getWindowManager()
                                                .getDefaultDisplay()
                                                .getRotation();
            CameraLog.d(TAG, "sendShotRequest, rotation: " + rotation);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));


            if (mFlashState == FlashManager.Flash.ON) {
                triggerFlash(captureBuilder);
            } else {
                addCacheToBuilder(captureBuilder);
                mSession.capture(captureBuilder.build(), null, mBackgroundHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void triggerFlash(CaptureRequest.Builder captureBuilder) {

        CameraLog.d(TAG, "triggerFlash");

        setBuilderCache(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
        setBuilderCache(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        setBuilderCache(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        addCacheToBuilder(mPreviewBuilder);
        submitRequest(Request.PREVIEW, new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(
                    @NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {

                super.onCaptureCompleted(session, request, result);

                CameraLog.d(TAG, "triggerFlash, onCaptureCompleted");
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                boolean converged = ((null != aeState) && (aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED));

                CameraLog.d(TAG, "triggerFlash, onCaptureCompleted aeState: " + aeState);

                if (converged) {
                    setBuilderCache(CaptureRequest.CONTROL_AE_LOCK, Boolean.TRUE);
                    addCacheToBuilder(captureBuilder);
                    try {
                        CameraLog.d(TAG, "triggerFlash, onCaptureCompleted converged: " + converged);
                        mSession.capture(captureBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 将设置的 request 添加到 builder
     */
    private void addCacheToBuilder(CaptureRequest.Builder captureBuilder) {

        if (null == captureBuilder) {
            CameraLog.d(TAG, "addCacheToBuilder, captureBuilder is null");
            return;
        }

        Iterator iterator = mRequestHashMap.entrySet()
                                           .iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            BuilderCache cache = (BuilderCache) entry.getValue();

            CameraLog.d(TAG,
                        "addCacheToBuilder, key: " + entry.getKey() + ", value: " + ((BuilderCache) entry.getValue()).getValue()
                                                                                                                     .toString());
            cache.addBuilder(captureBuilder);
        }
    }

    @Override
    public void setImageReader(ImageReader imageReader,
                               @Nullable ImageReader mRearMainImageReader,
                               @Nullable ImageReader mRearSecondImageReader, @Nullable ImageReader mRearThirdImageReader) {

        this.mImageReader = imageReader;
        if (mRearMainImageReader != null) {
            this.mRearMainImageReader = mRearMainImageReader;
        }
        if (mRearSecondImageReader != null) {
            this.mRearSecondImageReader = mRearSecondImageReader;
        }
        if (mRearThirdImageReader != null) {
            this.mRearThirdImageReader = mRearThirdImageReader;
        }
    }

    /**
     * 设置闪光灯模式
     */
    @Override
    public void setFlashMode(FlashManager.Flash flashMode) {

        CameraLog.d(TAG, "setFlashMode, flashMode: " + flashMode);

        switch (flashMode) {
        case ON:
            mFlashState = FlashManager.Flash.ON;
            setBuilderCache(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_SINGLE);
            setBuilderCache(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
            break;
        case AUTO:
            setBuilderCache(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            break;
        case OFF:
            mFlashState = FlashManager.Flash.OFF;
            setBuilderCache(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            setBuilderCache(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            break;
        }

        submitRequest(Request.PREVIEW, null);
    }

    private void submitRequest(Request request, CameraCaptureSession.CaptureCallback callback) {

        CameraLog.d(TAG, "submitRequest");

        switch (request) {
        case CAPTURE:
            sendShotRequest();
            break;
        case PREVIEW:
            setRepeatingPreview(callback);
            break;
        }
    }

    private <T> void setBuilderCache(CaptureRequest.Key key, final T value) {

        setBuilderCache(key.getName(), value);
    }

    /**
     * 保存 request 到 cachemap 中
     */
    private <T> void setBuilderCache(String name, final T value) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            CaptureRequest.Key key = new CaptureRequest.Key(name, value.getClass());
            mRequestHashMap.put(name, new BuilderCache<T>(key, new RequestValue<T>() {
                @Override
                public T get() {

                    return value;
                }

                @Override
                public String toString() {

                    if (null == value) {

                        return null;

                    }

                    if ((value instanceof int[]) && (((int[]) value).length > 0)) {

                        return String.valueOf(((int[]) value)[0]);

                    } else if ((value instanceof MeteringRectangle[]) && (((MeteringRectangle[]) value).length > 0)) {

                        return ((MeteringRectangle[]) value)[0].toString();

                    }

                    return value.toString();
                }
            }));

            CameraLog.d(TAG, "setBuilderCache, name: " + name);
        } else {
            CameraLog.e(TAG, "shit! os.Build.VERSION is less Q");
        }

    }

    private class BuilderCache<T> {

        private final CaptureRequest.Key<T> key;
        private final RequestValue<T> value;

        private BuilderCache(CaptureRequest.Key<T> key, RequestValue<T> value) {

            this.key = key;
            this.value = value;
        }

        public CaptureRequest.Key<T> getKey() {

            return key;
        }


        public RequestValue<T> getValue() {

            return value;
        }

        public void addBuilder(CaptureRequest.Builder builder) {

            builder.set(getKey(), getValue().get());
        }


    }

    public void addCameraItem(String cameraid, CameraItem cameraItem) {

        mCameraItemHashtable.put(cameraid, cameraItem);
    }

    public Handler getBackgroundThread() {

        return mBackgroundHandler;
    }

    @Override
    public String getCurrentCameraId() {

        return mCameraId;
    }

    public void setCameraId(String cameraId) {

        mCameraId = cameraId;
    }

    @Override
    public void swicthCamera(int cameraId) {

    }

    @Override
    public void switchMode(BaseMode modeName) {

        mModeName = modeName.getModeName();
        CameraLog.d(TAG, "switchMode: " + mModeName);

        reOpenCamera(mCameraId);
    }

    private void reOpenCamera(String cameraId) {

        closeCamera();
        openCamera(cameraId);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void videoButtonClick() {

        closePreviewSession();

        if (mIsRecordingVideo) {
            stopRecordingVideo();
        } else {
            try {
                startRecordingVideo();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void startRecordingVideo() throws CameraAccessException, IOException {

        CameraLog.d(TAG, "startRecordingVideo");
        mModeManagerInterface.getVideoMode()
                             .setupMediarecorder(mVideoSize);

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        assert texture != null;
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        List<Surface> surfaces = new ArrayList<>();

        // Set up Surface for the camera preview
        Surface previewSurface = new Surface(texture);
        surfaces.add(previewSurface);
        mPreviewBuilder.addTarget(previewSurface);

        // Set up Surface for the MediaRecorder
        Surface recorderSurface = mModeManagerInterface.getVideoMode()
                                                       .getMediaRecorder()
                                                       .getSurface();
        surfaces.add(recorderSurface);
        mPreviewBuilder.addTarget(recorderSurface);

        // Start a capture session
        // Once the session starts, we can update the UI and start recording
        mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                CameraLog.d(TAG, "startRecordingVideo, onConfigured");
                mSession = cameraCaptureSession;
                setRepeatingPreview(null);
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // UI
                        mIsRecordingVideo = true;

                        // Start recording
                        mModeManagerInterface.getVideoMode()
                                             .startRecordingVideo();
                    }
                });
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                mIsRecordingVideo = false;
                CameraLog.e(TAG, "holy shit!!! onConfigureFailed");
            }
        }, mBackgroundHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void stopRecordingVideo() {

        CameraLog.d(TAG, "stopRecordingVideo");
        // UI
        mIsRecordingVideo = false;

        // Stop recording
        mModeManagerInterface.getVideoMode()
                             .stopRecordingVideo();

        startPreview();
    }

    private void closePreviewSession() {

        if (mSession != null) {
            mSession.close();
            mSession = null;
        }
    }

    public interface CameraStateCallback {
        void onConfigured(CameraCaptureSession cameraCaptureSession);

        void onConfigureFailed(CameraCaptureSession cameraCaptureSession);

        void onOpened(CameraDevice camera);

        void onDisconnected(CameraDevice camera);

        void onClosed(CameraDevice camera);

        void onError(CameraDevice camera, int error);

        void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber);

        void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult);

        void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result);

        void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId);

        void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber);

        void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber);

        void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure);
    }

}
