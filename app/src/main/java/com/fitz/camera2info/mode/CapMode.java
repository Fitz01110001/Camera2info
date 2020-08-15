package com.fitz.camera2info.mode;

import android.content.Context;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.util.Size;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.manager.CameraManagerInterface;
import com.fitz.camera2info.storage.StorageRunnable;
import com.fitz.camera2info.utils.Util;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.mode
 * @ClassName: CapMode
 * @Author: Fitz
 * @CreateDate: 2020/5/23 14:59
 */
public class CapMode extends BaseMode {
    private static final String TAG = "CapMode";

    private static final int MAXIMAGES = 5;

    private ImageReader mImageReader = null;
    private ImageReader mImageReader1 = null;
    private ImageReader mImageReader2 = null;
    private ImageReader mImageReader3 = null;


    public CapMode(Context context, CameraManagerInterface cameraManagerInterface, Util util) {

        super(context, cameraManagerInterface, util);

    }

    @Override
    public void beforeOpenCamera() {

        CameraLog.d(TAG, "initImageReader, mCurrentCameraId：" + mCurrentCameraId);


        Size captureSize = mUtil.getCaptureSizeByCameraId(mCurrentCameraId, Util.SIZE_4_3, ImageFormat.JPEG);
        mImageReader = ImageReader.newInstance(captureSize.getWidth(), captureSize.getHeight(), ImageFormat.JPEG, MAXIMAGES);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

        if (mUtil.isMultiCam(mCurrentCameraId)) {
            Size mRearMainSize = mUtil.getCaptureSizeByCameraId(mUtil.getPhysicalCameraIdsArray(mCurrentCameraId)[0], Util.SIZE_4_3, ImageFormat.JPEG);

            CameraLog.d(TAG, "initImageReader, mRearMainSize: " + mRearMainSize.getWidth() + ", " + mRearMainSize.getHeight());

            mImageReader1 = ImageReader.newInstance(mRearMainSize.getWidth(), mRearMainSize.getHeight(), ImageFormat.JPEG, MAXIMAGES);
            mImageReader1.setOnImageAvailableListener(mOnMulitImageAvailableListener1, mBackgroundHandler);

            Size mRearSecondSize = mUtil.getCaptureSizeByCameraId(mUtil.getPhysicalCameraIdsArray(mCurrentCameraId)[1], Util.SIZE_4_3, ImageFormat.JPEG);

            CameraLog.d(TAG, "initImageReader, mRearSecondSize: " + mRearSecondSize.getWidth() + ", " + mRearSecondSize.getHeight());

            mImageReader2 = ImageReader.newInstance(mRearSecondSize.getWidth(), mRearSecondSize.getHeight(), ImageFormat.JPEG, MAXIMAGES);
            mImageReader2.setOnImageAvailableListener(mOnMulitImageAvailableListener2, mBackgroundHandler);

            if (mUtil.isTripleCam(mCurrentCameraId)) {
                Size mRearThirdSize = mUtil.getCaptureSizeByCameraId(mUtil.getPhysicalCameraIdsArray(mCurrentCameraId)[2], Util.SIZE_4_3, ImageFormat.JPEG);

                CameraLog.d(TAG, "initImageReader, mRearThirdSize: " + mRearThirdSize.getWidth() + ", " + mRearThirdSize.getHeight());

                mImageReader3 = ImageReader.newInstance(mRearThirdSize.getWidth(), mRearThirdSize.getHeight(), ImageFormat.JPEG, MAXIMAGES);
                mImageReader3.setOnImageAvailableListener(mOnMulitImageAvailableListener3, mBackgroundHandler);
            }
        }

        mCameraManagerInterface.setImageReader(mImageReader, mImageReader1, mImageReader2, mImageReader3);

        CameraLog.d(TAG, "initImageReader X");
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

            if (null == mBackgroundHandler) {
                mBackgroundHandler = mCameraManagerInterface.getBackgroundThread();
            }
            mBackgroundHandler.post(new StorageRunnable(mOnSaveState, image, StorageRunnable.getImageName()));

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

            if (null == mBackgroundHandler) {
                mBackgroundHandler = mCameraManagerInterface.getBackgroundThread();
            }
            mBackgroundHandler.post(new StorageRunnable(mOnSaveState, image, "Mulit_1_" + StorageRunnable.getImageName()));

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

            if (null == mBackgroundHandler) {
                mBackgroundHandler = mCameraManagerInterface.getBackgroundThread();
            }
            mBackgroundHandler.post(new StorageRunnable(mOnSaveState, image, "Mulit_2_" + StorageRunnable.getImageName()));

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

            if (null == mBackgroundHandler) {
                mBackgroundHandler = mCameraManagerInterface.getBackgroundThread();
            }
            mBackgroundHandler.post(new StorageRunnable(mOnSaveState, image, "Mulit_3_" + StorageRunnable.getImageName()));

            CameraLog.d(TAG, "mOnMulitImageAvailableListener3 X");
        }
    };

    @Override
    public ModeManager.ModeName getModeName() {

        return ModeManager.ModeName.PHOTO_MODE;
    }

}
