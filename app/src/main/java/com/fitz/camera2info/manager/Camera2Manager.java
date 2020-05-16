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
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
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
import com.fitz.camera2info.storage.SaveImage;
import com.fitz.camera2info.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.manager
 * @ClassName: Camera2Manager
 * @Author: Fitz
 * @CreateDate: 2019/12/16 0:47
 */
public class Camera2Manager {

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

    private CameraManager mCameraManager = null;
    private CameraCaptureSession mCameraCaptureSession = null;
    private CameraCharacteristics mCameraCharacteristics = null;
    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mPreviewRequestBuilder = null;
    private CaptureRequest mPreviewRequest = null;

    private Handler mBackgroundHandler = null;
    private HandlerThread mBackgroundThread = null;
    private Rect mSensorRect = null;
    private Rect mCurrentRect = null;

    private Size mPreviewSize = null;
    private Size mDefaultSize = null;
    private Surface mPreviewSurface = null;
    private TextureView mTextureView = null;


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

    public void onCreate(String cameraId) {

        CameraLog.d(TAG, "onCreate, cameraId:" + cameraId);
        mCameraId = cameraId;

        startBackgroundThread();

    }


    public void onResume(TextureView textureView) {

        CameraLog.d(TAG, "onResume");

        mTextureView = textureView;
    }

    private void setupTextureView() {

        mCameraCharacteristics = mUtil.getCameraItem(mCameraId)
                                      .getCameraCharacteristics();
        StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        for (Size size : Arrays.asList(map.getOutputSizes(ImageFormat.JPEG))) {
            float ratio = (float) size.getWidth() / (float) size.getHeight();
            CameraLog.d(TAG, "setupTextureView, size: " + size.toString() + ", ratio: " + ratio);

            if (Math.abs(ratio - Util.SIZE_4_3) == 0) {
                mDefaultSize = size;

                CameraLog.d(TAG, "setupTextureView, mDefaultSize: " + mDefaultSize.toString());
                break;
            }
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

        /*if (swappedDimensions) {
            rotatedPreviewWidth = mDefaultSize.getHeight();
            rotatedPreviewHeight = mDefaultSize.getWidth();
        }*/

        SurfaceTexture texture = mTextureView.getSurfaceTexture();

        texture.setDefaultBufferSize(rotatedPreviewWidth, rotatedPreviewHeight);
        mPreviewSurface = new Surface(texture);

        CameraLog.d(TAG, "setupTextureView X, W: " + rotatedPreviewWidth + ", H:" + rotatedPreviewHeight);
    }

    public void onPause() {

    }

    public void onStop() {

        closeCamera();
    }

    public void openCamera(String cameraId) {

        CameraLog.d(TAG, "openCamera");

        mCameraId = cameraId;
        setupTextureView();

        try {
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            setUpCameraOutputs();
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO 权限检查
                //requestCameraPermission();
            } else {
                CameraLog.d(TAG, "open cameraid:" + mCameraId);
                mCameraManager.openCamera(mCameraId, mDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
                if (null != mCameraCaptureSession) {
                    try {
                        mCameraCaptureSession.stopRepeating();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    mCameraCaptureSession.close();
                    mCameraCaptureSession = null;
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

    public String takeShot() {
        imageName = SaveImage.getImageName();

        CameraLog.d(TAG, "takeShot, imageName" + imageName);

        mUtil.playSound();
        sendShotRequest();
        return imageName;
    }

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

            mCameraCaptureSession.capture(captureBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setRepeatingPreview() {
        // Finally, we start displaying the camera preview.
        mPreviewRequest = mPreviewRequestBuilder.build();
        try {
            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        String CaptureTAG = "CaptureCallback";

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {

            super.onCaptureStarted(session, request, timestamp, frameNumber);
            //CameraLog.d(CaptureTAG, "onCaptureStarted");
        }

        @Override
        public void onCaptureProgressed(
                @NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {

            super.onCaptureProgressed(session, request, partialResult);

        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {

            super.onCaptureCompleted(session, request, result);


        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {

            super.onCaptureSequenceAborted(session, sequenceId);
            CameraLog.e(CaptureTAG, "onCaptureSequenceAborted");
        }

        @Override
        public void onCaptureBufferLost(
                @NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {

            super.onCaptureBufferLost(session, request, target, frameNumber);
            CameraLog.e(CaptureTAG, "onCaptureBufferLost");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {

            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            CameraLog.e(CaptureTAG, "onCaptureSequenceCompleted");
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {

            super.onCaptureFailed(session, request, failure);
            CameraLog.e(CaptureTAG, "onCaptureFailed");
        }
    };

    private CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {

        String StateCallbackTAG = "StateCallback";

        @Override
        public void onOpened(@NonNull CameraDevice camera) {

            CameraLog.d(StateCallbackTAG, "onOpened " + camera.getId());
            mCameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

            CameraLog.d(StateCallbackTAG, "onDisconnected " + camera.getId());
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {

            super.onClosed(camera);
            CameraLog.d(StateCallbackTAG, "onClosed," + camera.getId());
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

            CameraLog.d(StateCallbackTAG, "onError" + camera.getId());
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void createCameraPreviewSession() {

        List<OutputConfiguration> currentOutputs = new ArrayList<>();
        OutputConfiguration outputConfiguration = null;


        CameraLog.d(TAG, "createCameraPreviewSession");
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            if (mPreviewSurface != null) {
                mPreviewRequestBuilder.addTarget(mPreviewSurface);
                outputConfiguration = new OutputConfiguration(mPreviewSurface);
                currentOutputs.add(outputConfiguration);
            }

            if (mUtil.isMultiCam(mCameraId)) {
                CameraLog.d(TAG, "createCameraPreviewSession, multi mode");

                outputConfiguration = new OutputConfiguration(mRearMainImageReader.getSurface());
                CameraLog.d(TAG, "createCameraPreviewSession, id: " + mUtil.getPhysicalCameraIdsArray(mCameraId)[0]);
                outputConfiguration.setPhysicalCameraId(mUtil.getPhysicalCameraIdsArray(mCameraId)[0]);
                currentOutputs.add(outputConfiguration);

                outputConfiguration = new OutputConfiguration(mRearSecondImageReader.getSurface());
                CameraLog.d(TAG, "createCameraPreviewSession, id: " + mUtil.getPhysicalCameraIdsArray(mCameraId)[1]);
                outputConfiguration.setPhysicalCameraId(mUtil.getPhysicalCameraIdsArray(mCameraId)[1]);
                currentOutputs.add(outputConfiguration);

                if (mUtil.isTripleCam(mCameraId)) {
                    outputConfiguration = new OutputConfiguration(mRearThirdImageReader.getSurface());
                    CameraLog.d(TAG, "createCameraPreviewSession, id: " + mUtil.getPhysicalCameraIdsArray(mCameraId)[2]);
                    outputConfiguration.setPhysicalCameraId(mUtil.getPhysicalCameraIdsArray(mCameraId)[2]);
                    currentOutputs.add(outputConfiguration);
                }

                CaptureRequest request = mPreviewRequestBuilder.build();
                HandlerExecutor executor = new HandlerExecutor(mBackgroundHandler);
                SessionConfiguration config = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR, currentOutputs, executor, stateCallback);
                config.setSessionParameters(request);
                mCameraDevice.createCaptureSession(config);
                //mCameraDevice.createCaptureSessionByOutputConfigurations(currentOutputs, stateCallback, mBackgroundHandler);
            } else {
                CameraLog.d(TAG, "createCameraPreviewSession, single mode");

                mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()), stateCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

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
                mCameraCaptureSession = cameraCaptureSession;

                // Auto focus should be continuous for camera preview.
                if (mPreviewRequestBuilder != null) {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                }

                setRepeatingPreview();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

        }
    };

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

    public void addCameraItem(String cameraid, CameraItem cameraItem) {

        mCameraItemHashtable.put(cameraid, cameraItem);
    }

    public Handler getBackgroundThread() {

        return mBackgroundHandler;
    }

    public void setCameraId(String cameraId) {

        mCameraId = cameraId;
    }

    public interface cameraState {
        void onCameraOpened();
    }
}
