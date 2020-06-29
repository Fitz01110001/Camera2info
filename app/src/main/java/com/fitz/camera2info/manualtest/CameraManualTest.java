package com.fitz.camera2info.manualtest;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.R;
import com.fitz.camera2info.base.BaseCameraActivity;
import com.fitz.camera2info.camerainfo.CameraItem;
import com.fitz.camera2info.flash.FlashManager;
import com.fitz.camera2info.manager.Camera2Manager;
import com.fitz.camera2info.mode.ModeManager;
import com.fitz.camera2info.storage.StorageRunnable;
import com.fitz.camera2info.thumbnail.ThumbNailManager;
import com.fitz.camera2info.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.manualtest
 * @ClassName: CameraManualTest
 * @Author: Fitz
 * @CreateDate: 2019/12/21 19:04
 */
public class CameraManualTest extends BaseCameraActivity {

    @BindView(R.id.top_flash) ImageView topFlash;
    @BindView(R.id.top_2) ImageView top2;
    @BindView(R.id.top_3) ImageView top3;
    @BindView(R.id.top_settings) ImageView topSettings;
    @BindView(R.id.btn_shutter) ImageButton btnShutter;
    @BindView(R.id.btn_switch) ImageButton btnSwitch;
    @BindView(R.id.camera_control) ConstraintLayout cameraControl;
    @BindView(R.id.activcity_manual_root) ConstraintLayout activcityManualRoot;
    @BindView(R.id.preview_root) FrameLayout previewRoot;
    @BindView(R.id.btn_thumbnail) ImageButton btnThumbnail;

    private static final String TAG = "CameraManualTest";
    @BindView(R.id.tv_photo_mode) TextView tvPhotoMode;
    @BindView(R.id.tv_video_mode) TextView tvVideoMode;


    private Context mContext = null;


    private String mCurrentCameraId = "";
    private Size mDefaultSize = null;
    private TextureView mTextureView = null;
    private CameraItem mCameraItem = null;
    private boolean isMulitCam = false;
    private int mwidth = 0;
    private int mheight = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //去掉窗口标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏顶部的状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_manual_test);
        ButterKnife.bind(this);

        mContext = this;
        parseIntent();
        initTextureView();
        mCameraStateCallback = new Camera2Manager.CameraStateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession cameraCaptureSession) {

                CameraLog.d(TAG, "onConfigured");
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
        mCamera2Manager.onCreate(mCurrentCameraId, mCameraStateCallback);
        mModeManager.onCreate(mCurrentCameraId, mOnSaveState);
        mFlashManager = new FlashManager(this, topFlash, mCamera2Manager);

        initUI();
    }

    private void initUI() {

        setUICurrentMode(mModeManager.getCurrentModeName());
    }

    private void initThumbnail() {

        CameraLog.d(getTAG(), "initThumbnail");
        mThumbNailManager.setThumbnailCallback(new ThumbNailManager.ThumbnailCallback() {
            @Override
            public void updateThumbnail(Uri uri, Bitmap bitmap) {

                CameraLog.d(TAG, "initThumbnail-updateThumbnail");
                CameraManualTest.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        btnThumbnail.setTag(uri);
                        btnThumbnail.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    private void initTextureView() {

        mTextureView = new TextureView(this);
        previewRoot.addView(mTextureView, getLayoutParams());
    }

    private FrameLayout.LayoutParams getLayoutParams() {

        FrameLayout.LayoutParams layoutParams = null;

        int width = Util.getScreenWidth(this);
        int height = (int) (width * Util.SIZE_4_3);
        layoutParams = new FrameLayout.LayoutParams(width, height);

        CameraLog.d(TAG, "getLayoutParams, W: " + width + ", H: " + height);

        return layoutParams;
    }

    private void parseIntent() {

        mCurrentCameraId = getIntent().getStringExtra(Util.CAMERAID);
        isMulitCam = mUtil.isMulitLogicalCameraId(mCurrentCameraId);
        mCameraItem = mUtil.getCameraItem(mCurrentCameraId);

        CameraLog.d(TAG, "parseIntent, mCurrentCameraId: " + mCurrentCameraId + ", isMulitCam: " + isMulitCam);
    }

    @Override
    protected void onResume() {

        super.onResume();
        initThumbnail();
        mThumbNailManager.onResume();
        mCamera2Manager.onResume(mTextureView);
        if (mTextureView.isAvailable()) {
            mCamera2Manager.openCamera(mCurrentCameraId);
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {

            Log.d(TAG, "onSurfaceTextureAvailable");
            mCamera2Manager.openCamera(mCurrentCameraId);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {

            Log.d(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {

            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            //Log.d(TAG, "onSurfaceTextureUpdated");

        }

    };

    @Override
    protected void onDestroy() {

        super.onDestroy();
        mCamera2Manager.onStop();
    }

    @Override
    public void finish() {

        super.finish();
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @OnClick({R.id.btn_shutter,
              R.id.btn_switch,
              R.id.btn_thumbnail,
              R.id.tv_photo_mode,
              R.id.tv_video_mode,
              R.id.top_flash,
              R.id.top_2,
              R.id.top_3,
              R.id.top_settings})
    public void onViewClicked(View view) {

        switch (view.getId()) {
        case R.id.btn_shutter:
            onShutterClick();
            break;

        case R.id.btn_switch:
            //todo 切换前后摄
            onSwitchClick();
            break;

        case R.id.btn_thumbnail:
            onThumbnailClick(view);
            break;

        case R.id.tv_photo_mode:
            mModeManager.switchCameraMode(ModeManager.ModeName.PHOTO_MODE);
            break;

        case R.id.tv_video_mode:
            //todo 还有点问题
            //mModeManager.switchCameraMode(ModeManager.ModeName.VIDEO_MODE);
            break;
        case R.id.top_flash:
            mFlashManager.onFlashClick();
            break;
        case R.id.top_2:
            break;
        case R.id.top_3:
            break;
        case R.id.top_settings:
            break;
        }
    }

    private void onThumbnailClick(View view) {

        CameraLog.d(TAG, "onThumbnailClick");
        Uri uri = (Uri) view.getTag();

        try {
            goToGallery(uri);
        } catch (ActivityNotFoundException e) {
            CameraLog.e(getTAG(), "onThumbnailClick, no gallery app!?");
        }
    }

    private void goToGallery(Uri uri) {

        if (uri == null) {
            CameraLog.d(TAG, "uri is null, can not go to gallery");
            return;
        }

        String mimeType = getContentResolver().getType(uri);
        CameraLog.d(TAG, "[goToGallery] uri: " + uri + ", mimeType = " + mimeType);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        startActivity(intent);
    }

    private void onSwitchClick() {

        CameraLog.d(TAG, "onSwitchClick");
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void onShutterClick() {

        setUIEnable(false);
        if (ModeManager.ModeName.PHOTO_MODE == mModeManager.getCurrentModeName()) {
            mCamera2Manager.takeCapture();
        } else {
            mCamera2Manager.videoButtonClick();
        }
    }

    public StorageRunnable.onSaveState mOnSaveState = new StorageRunnable.onSaveState() {
        @Override
        public Context getContext() {

            return mContext;
        }

        @Override
        public void onImageSaved(boolean success, Uri uri) {

            CameraLog.d(TAG, "onImageSaved, success: " + success);

            mThumbNailManager.updateThumbnail(uri);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    updateThumbnailView(uri);
                    setUIEnable(true);
                }
            });
        }

        @Override
        public void onVideoSaved(Uri uri) {

            CameraLog.d(TAG, "onVideoSaved");

            mThumbNailManager.updateThumbnail(uri);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    updateThumbnailView(uri);
                    setUIEnable(true);
                }
            });
        }

    };

    private void updateThumbnailView(Uri uri) {

        CameraLog.d(TAG, "updateThumbnailView, uri: " + uri);
        btnThumbnail.setTag(uri);
    }

    @Override
    protected String getTAG() {

        return TAG;
    }

    @Override
    public void setUIEnable(Boolean enable) {

        if (enable) {

            CameraLog.d(TAG, "setUIEnable, enable");
        } else {

            CameraLog.d(TAG, "setUIEnable, disable");
        }


        //btnShutter.setClickable(enable);
        btnSwitch.setClickable(enable);
        btnThumbnail.setClickable(enable);
        activcityManualRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (enable) {
                    CameraLog.d(TAG, "root onTouch: " + event.getRawX() + "-" + event.getRawY());

                } else {
                    return false;
                }
                return false;
            }
        });
    }

    @Override
    public void setUICurrentMode(ModeManager.ModeName modeName) {

        switch (modeName) {
        case PHOTO_MODE:
            tvPhotoMode.setTextColor(ContextCompat.getColor(this, R.color.color_mode_selected));
            tvVideoMode.setTextColor(ContextCompat.getColor(this, R.color.color_mode_unselected));
            btnShutter.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.shutter_selector));
            break;
        case VIDEO_MODE:
            tvPhotoMode.setTextColor(ContextCompat.getColor(this, R.color.color_mode_unselected));
            tvVideoMode.setTextColor(ContextCompat.getColor(this, R.color.color_mode_selected));
            btnShutter.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cam_core_capture_button_video_start_icn));
            break;
        }
    }

}
