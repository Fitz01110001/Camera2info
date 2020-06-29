package com.fitz.camera2info.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;

import com.fitz.camera2info.CameraLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;

import static android.os.Environment.DIRECTORY_DCIM;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.storage
 * @ClassName: SaveImage
 * @Author: Fitz
 * @CreateDate: 2019/12/22 15:34
 */
public class StorageRunnable implements Runnable {
    private static String TAG = "SaveImage";

    private Image mImage = null;
    private String mImageName = "";
    private String mVideoName = "";
    private String mVideoPath = "";
    private Bitmap mBitmap = null;
    private Context mContext = null;
    private static File imageDir = null;
    private File finalImage = null;
    private onSaveState mOnSaveState = null;
    private saveType defaultSaveType = saveType.picture;

    private enum saveType {
        picture,
        video
    }


    public StorageRunnable(onSaveState onSaveState, Image image, String imageName) {

        CameraLog.d(TAG, "SaveImage: " + imageName);
        defaultSaveType = saveType.picture;
        mOnSaveState = onSaveState;
        mImage = image;
        mImageName = imageName;
        mContext = mOnSaveState.getContext();
        imageDir = mContext.getExternalFilesDir(DIRECTORY_DCIM);
    }

    public StorageRunnable(onSaveState onSaveState, Bitmap bitmap, String imageName) {

        CameraLog.d(TAG, "SaveBitmap: " + imageName);
        mOnSaveState = onSaveState;
        mBitmap = bitmap;
        mImageName = imageName;
        mContext = mOnSaveState.getContext();
        imageDir = mContext.getExternalFilesDir(DIRECTORY_DCIM);
    }

    public StorageRunnable(onSaveState onSaveState, String path, String videoName) {

        CameraLog.d(TAG, "mVideoPath: " + path);
        defaultSaveType = saveType.video;
        mOnSaveState = onSaveState;
        mVideoPath = path;
        mVideoName = videoName;
        mContext = mOnSaveState.getContext();
    }

    /**
     * get pic name by time :
     * FIMG_20190101_170259
     */
    public static String getImageName() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FIMG_")
                     .append(year)
                     .append(month)
                     .append(day)
                     .append("_")
                     .append(hour)
                     .append(min)
                     .append(sec)
                     .append(".jpg");
        return stringBuilder.toString();
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     */
    @Override
    public void run() {

        if (defaultSaveType == saveType.picture) {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            CameraLog.d(TAG, "imageDir: " + imageDir.toString());

            // 首先判断文件夹是否存在
            if (!imageDir.exists()) {
                CameraLog.e(TAG, "文件夹不存在!!?");

                return;
            } else {
                finalImage = new File(imageDir, mImageName);

                try {
                    savePicIntoAppData(bytes);
                    savePicIntoAlbum();

                } catch (IOException e) {
                    CameraLog.e(TAG, "save IOException: " + e.toString());

                    mOnSaveState.onImageSaved(false, null);
                    return;
                } finally {
                    buffer.rewind();
                    mImage.close();
                    finalImage.delete();
                }
            }

        } else if (mBitmap != null) {
            File finalImage = new File(imageDir, mImageName);
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(finalImage));
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                CameraLog.e(TAG, "save IOException: " + e.toString());

                mOnSaveState.onImageSaved(false, null);
            } finally {
                mImage.close();
            }
        } else if (defaultSaveType == saveType.video) {
            saveVideoIntoAlbum(mVideoPath);
        }
    }

    private void savePicIntoAppData(byte[] bytes) throws IOException {

        CameraLog.d(TAG, "savePicIntoAppData");

        // 实例化对象：文件输出流
        FileOutputStream mFileOutputStream = new FileOutputStream(finalImage);
        // 写入文件
        mFileOutputStream.write(bytes);
        // 清空输出流缓存
        mFileOutputStream.flush();
        // 关闭输出流
        mFileOutputStream.close();

        CameraLog.d(TAG, "savePicIntoAppData X");
    }

    /**
     * for Android Q
     */
    private void savePicIntoAlbum() {

        CameraLog.d(TAG, "savePicIntoAlbum");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, "This is an FitzCamera image");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, mImageName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.TITLE, mImageName);
        values.put(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, "FitzCamera");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera");

        Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = mContext.getContentResolver();
        Uri insertUri = resolver.insert(external, values);
        BufferedInputStream inputStream = null;
        OutputStream os = null;

        CameraLog.d(TAG, "external: " + external.toString() + "\n" + "insertUri: " + insertUri.toString());

        try {
            inputStream = new BufferedInputStream(new FileInputStream(finalImage));
            if (insertUri != null) {
                os = resolver.openOutputStream(insertUri);
            }

            if (os != null) {
                byte[] buffer = new byte[1024 * 4];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }

            mOnSaveState.onImageSaved(true, insertUri);

        } catch (IOException e) {
            CameraLog.e(TAG, "savePicIntoAlbum IOException: " + e.toString());

            mOnSaveState.onImageSaved(false, null);
        }

        CameraLog.d(TAG, "savePicIntoAlbum X");
    }

    private void saveVideoIntoAlbum(String path) {

        CameraLog.d(TAG, "saveVideoIntoAlbum, path: " + path);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DESCRIPTION, "This is an FitzCamera video");
        values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Video.Media.DISPLAY_NAME, mVideoName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.TITLE, mVideoName);
        values.put(MediaStore.Video.Media.BUCKET_DISPLAY_NAME, "FitzCamera");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Camera");

        ContentResolver resolver = mContext.getContentResolver();
        Uri insertUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        CameraLog.d(TAG, "saveVideoIntoAlbum, path: " + insertUri);
        mOnSaveState.onVideoSaved(insertUri);
    }

    public interface onSaveState {
        Context getContext();

        void onImageSaved(boolean success, Uri uri);

        void onVideoSaved(Uri uri);
    }
}
