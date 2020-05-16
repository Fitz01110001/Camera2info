package com.fitz.camera2info.manualtest;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.R;
import com.fitz.camera2info.base.BaseActivity;
import com.fitz.camera2info.camerainfo.CameraItem;
import com.fitz.camera2info.storage.SaveImage;
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
public class CameraManualTest extends BaseActivity {

    @BindView(R.id.top_btn_1) Button topBtn1;
    @BindView(R.id.top_btn_2) Button topBtn2;
    @BindView(R.id.top_btn_3) Button topBtn3;
    @BindView(R.id.top_btn_4) Button topBtn4;
    @BindView(R.id.btn_shutter) ImageButton btnShutter;
    @BindView(R.id.btn_switch) ImageButton btnSwitch;
    @BindView(R.id.camera_control) ConstraintLayout cameraControl;
    @BindView(R.id.activcity_manual_root) ConstraintLayout activcityManualRoot;
    @BindView(R.id.preview_root) FrameLayout previewRoot;
    @BindView(R.id.btn_thumbnail) ImageButton btnThumbnail;

    private static final String TAG = "CameraManualTest";

    private Context mContext = null;
    private ImageReader mImageReader = null;
    private ImageReader mImageReader1 = null;
    private ImageReader mImageReader2 = null;
    private ImageReader mImageReader3 = null;
    private Handler mBackgroundHandler = null;
    private String mCurrentCameraId = "";
    private Size mDefaultSize = null;
    private TextureView mTextureView = null;
    private CameraItem mCameraItem = null;
    private boolean isMulitCam = false;
    private int mwidth = 0;
    private int mheight = 0;
    private int mMaxImages = 5;


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
        mCamera2Manager.onCreate(mCurrentCameraId);
        initImageReader();
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

    private void initImageReader() {

        CameraLog.d(TAG, "initImageReader, mCurrentCameraId：" + mCurrentCameraId);

        mBackgroundHandler = mCamera2Manager.getBackgroundThread();
        Size mDefaultSize = mUtil.getDefaultSizeByCameraId(mCurrentCameraId);
        mImageReader = ImageReader.newInstance(mDefaultSize.getWidth(), mDefaultSize.getHeight(), ImageFormat.JPEG, mMaxImages);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

        if (mUtil.isMultiCam(mCurrentCameraId)) {
            Size mRearMainSize = mUtil.getDefaultSizeByCameraId(mUtil.getPhysicalCameraIdsArray(mCurrentCameraId)[0]);

            CameraLog.d(TAG, "initImageReader, mRearMainSize: " + mRearMainSize.getWidth() + ", " + mRearMainSize.getHeight());

            mImageReader1 = ImageReader.newInstance(mRearMainSize.getWidth(), mRearMainSize.getHeight(), ImageFormat.JPEG, mMaxImages);
            mImageReader1.setOnImageAvailableListener(mOnMulitImageAvailableListener1, mBackgroundHandler);

            Size mRearSecondSize = mUtil.getDefaultSizeByCameraId(mUtil.getPhysicalCameraIdsArray(mCurrentCameraId)[1]);

            CameraLog.d(TAG, "initImageReader, mRearSecondSize: " + mRearSecondSize.getWidth() + ", " + mRearSecondSize.getHeight());

            mImageReader2 = ImageReader.newInstance(mRearSecondSize.getWidth(), mRearSecondSize.getHeight(), ImageFormat.JPEG, mMaxImages);
            mImageReader2.setOnImageAvailableListener(mOnMulitImageAvailableListener2, mBackgroundHandler);

            if (mUtil.isTripleCam(mCurrentCameraId)) {
                Size mRearThirdSize = mUtil.getDefaultSizeByCameraId(mUtil.getPhysicalCameraIdsArray(mCurrentCameraId)[2]);

                CameraLog.d(TAG, "initImageReader, mRearThirdSize: " + mRearThirdSize.getWidth() + ", " + mRearThirdSize.getHeight());

                mImageReader3 = ImageReader.newInstance(mRearThirdSize.getWidth(), mRearThirdSize.getHeight(), ImageFormat.JPEG, mMaxImages);
                mImageReader3.setOnImageAvailableListener(mOnMulitImageAvailableListener3, mBackgroundHandler);
            }
        }

        mCamera2Manager.setImageReader(mImageReader, mImageReader1, mImageReader2, mImageReader3);

        CameraLog.d(TAG, "initImageReader X");
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


    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            CameraLog.d(TAG, "mOnImageAvailableListener");

            Image image = reader.acquireNextImage();

            if (image == null) {
                CameraLog.e(TAG, "[onImageAvailable] acquireNextImage null, return");
                return;
            }

            mBackgroundHandler.post(new SaveImage(mOnSaveImageState, image, SaveImage.getImageName()));

            CameraLog.d(TAG, "mOnImageAvailableListener X");
        }
    };

    private ImageReader.OnImageAvailableListener mOnMulitImageAvailableListener1 = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            CameraLog.d(TAG, "mOnMulitImageAvailableListener1");

            Image image = reader.acquireNextImage();

            if (image == null) {
                CameraLog.e(TAG, "[onMulitImageAvailable] acquireNextImage null, return");
                return;
            }

            mBackgroundHandler.post(new SaveImage(mOnSaveImageState, image, "Mulit_1_" + SaveImage.getImageName()));

            CameraLog.d(TAG, "mOnMulitImageAvailableListener1 X");
        }
    };

    private ImageReader.OnImageAvailableListener mOnMulitImageAvailableListener2 = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            CameraLog.d(TAG, "mOnMulitImageAvailableListener2");

            Image image = reader.acquireNextImage();

            if (image == null) {
                CameraLog.e(TAG, "[onMulitImageAvailable] acquireNextImage null, return");
                return;
            }

            mBackgroundHandler.post(new SaveImage(mOnSaveImageState, image, "Mulit_2_" + SaveImage.getImageName()));

            CameraLog.d(TAG, "mOnMulitImageAvailableListener2 X");
        }
    };

    private ImageReader.OnImageAvailableListener mOnMulitImageAvailableListener3 = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            CameraLog.d(TAG, "mOnMulitImageAvailableListener3");

            Image image = reader.acquireNextImage();

            if (image == null) {
                CameraLog.e(TAG, "[onMulitImageAvailable] acquireNextImage null, return");
                return;
            }

            mBackgroundHandler.post(new SaveImage(mOnSaveImageState, image, "Mulit_3_" + SaveImage.getImageName()));

            CameraLog.d(TAG, "mOnMulitImageAvailableListener3 X");
        }
    };


    @OnClick({R.id.btn_shutter, R.id.btn_switch, R.id.btn_thumbnail})
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

    private void onShutterClick() {

        setUIEnable(false);
        mCamera2Manager.takeShot();
    }

    public SaveImage.onSaveImageState mOnSaveImageState = new SaveImage.onSaveImageState() {
        @Override
        public Context getContext() {

            return mContext;
        }

        @Override
        public void onImageSaved(boolean success, Uri uri) {

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
    protected void setUIEnable(Boolean enable) {

        if (enable) {

            CameraLog.d(TAG, "setUIEnable, enable");
        } else {

            CameraLog.d(TAG, "setUIEnable, disable");
        }

        topBtn1.setClickable(enable);
        topBtn2.setClickable(enable);
        topBtn3.setClickable(enable);
        topBtn4.setClickable(enable);
        btnShutter.setClickable(enable);
        btnSwitch.setClickable(enable);
        btnThumbnail.setClickable(enable);
        activcityManualRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (enable) {

                } else {
                    return false;
                }
                return false;
            }
        });
    }

}
