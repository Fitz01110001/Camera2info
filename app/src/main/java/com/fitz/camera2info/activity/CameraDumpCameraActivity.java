package com.fitz.camera2info.activity;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.R;
import com.fitz.camera2info.base.BaseCameraActivity;
import com.fitz.camera2info.camerainfo.CameraItem;
import com.fitz.camera2info.manager.Camera2Manager;
import com.fitz.camera2info.mode.ModeManager;
import com.fitz.camera2info.utils.Util;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.fragment
 * @ClassName: CameraDumpActivity
 * @Author: Fitz
 * @CreateDate: 2019/12/15 23:32
 */
public class CameraDumpCameraActivity extends BaseCameraActivity {

    private static final String TAG = "CameraDumpActivity";
    private String[] mCameraIds = null;
    private String mDumpInfo = null;
    private String mCameraId = Util.REAR_SECOND_CAMERA;

    @BindView(R.id.tv_dump_info) TextView tvDumpInfo;

    @BindView(R.id.topbar_dump) QMUITopBarLayout topbarDump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_dump);
        ButterKnife.bind(this);
        initTopBar();

        mCameraStateCallback = new Camera2Manager.CameraStateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                CameraLog.d(TAG,"onConfigured");
                onCameraConfigured();
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

            }

            @Override
            public void onOpened(CameraDevice camera) {

            }

            @Override
            public void onDisconnected(CameraDevice camera) {

            }

            @Override
            public void onClosed(CameraDevice camera) {

            }

            @Override
            public void onError(CameraDevice camera, int error) {

                CameraLog.e(TAG, "onError shit!!!");
            }

            @Override
            public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {

            }

            @Override
            public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {

            }

            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {

            }

            @Override
            public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {

            }

            @Override
            public void onCaptureBufferLost(CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber) {

            }

            @Override
            public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {

            }

            @Override
            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {

            }
        };
        mCamera2Manager.onCreate(mCameraId,mCameraStateCallback);

        Size captureSize = mUtil.getCaptureSizeByCameraId(mCameraId, Util.SIZE_4_3, ImageFormat.JPEG);
        ImageReader mImageReader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(), ImageFormat.JPEG, 5);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

            }
        }, null);
        mCamera2Manager.setImageReader(mImageReader, null, null, null);

    }

    protected void initTopBar() {

        topbarDump.setTitle(R.string.camera_dump);
        topbarDump.addLeftBackImageButton()
                  .setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {

                          onBackPressed();
                      }
                  });
    }

    @Override
    protected void onResume() {

        super.onResume();

        fakeOpenCamera();
    }

    private void fakeOpenCamera() {

        TextureView textureView = new TextureView(this);
        textureView.setSurfaceTexture(new SurfaceTexture(true));
        mCamera2Manager.onResume(textureView);
        mCamera2Manager.openCamera(mCameraId);
    }

    private void onCameraConfigured() {

        mDumpInfo = getCameraDumpInfo();
        tvDumpInfo.setText(mDumpInfo);
    }

    private String getCameraDumpInfo() {
        CameraLog.d(TAG,"getCameraDumpInfo");

        StringBuilder sb = new StringBuilder();

        //mCameraIds = mUtil.getAllCameraIds();
        //for (String cameraId : mCameraIds) {
        sb.append("cameraId: " + mCameraId)
          .append("\n")
          .append("CameraCharacteristics.Key: ")
          .append("\n\t");

        CameraItem cameraItem = mUtil.getCameraItem(mCameraId);
        CameraLog.d(TAG, cameraItem.toString());

        //CameraCharacteristics.Key
        CameraCharacteristics characteristics = cameraItem.getCameraCharacteristics();
        List<CameraCharacteristics.Key<?>> keys = characteristics.getKeys();
        for (CameraCharacteristics.Key<?> key : keys) {
            sb.append(getKeyName(key.toString()))
              .append(":")
              .append("\n\t")
              .append("[")
              .append(mUtil.getKeyValue(characteristics, key))
              .append("]")
              .append("\n\t");

            CameraLog.d(TAG, "key: " + key.toString());
        }

        //
        List<CaptureRequest.Key<?>> captureRequestKeys = characteristics.getAvailableCaptureRequestKeys();
        for (CaptureRequest.Key<?> key : captureRequestKeys) {
            sb.append(getKeyName(key.toString()))
              .append("\n\t");
        }

        sb.append("\n");
        //}

        String result = sb.toString();
        sb = null;
        return result;
    }

    public String getKeyName(String s) {

        String keyName = s.substring(s.indexOf("(") + 1, s.indexOf(")"));

        return keyName;
    }

    @Override
    public String getTAG() {

        return TAG;
    }

    @Override
    public void setUIEnable(Boolean enable) {

    }

    @Override
    public void setUICurrentMode(ModeManager.ModeName modeName) {

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (null != mCameraStateCallback) {
            mCameraStateCallback = null;
        }
    }

}
