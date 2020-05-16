package com.fitz.camera2info.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.fitz.camera2info.CameraLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class SaveImage implements Runnable {
    private static String TAG = "SaveImage";

    private Image mImage = null;
    private String mImageName = "";
    private Bitmap mBitmap = null;
    private Context mContext = null;
    private static File imageDir = null;
    private File finalImage = null;
    private onSaveImageState mOnSaveImageState = null;


    public SaveImage(onSaveImageState onSaveImageState, Image image, String imageName) {

        CameraLog.d(TAG, "SaveImage: " + imageName);
        mOnSaveImageState = onSaveImageState;
        mImage = image;
        mImageName = imageName;
        mContext = mOnSaveImageState.getContext();
        imageDir = mContext.getExternalFilesDir(DIRECTORY_DCIM);
    }

    public SaveImage(onSaveImageState onSaveImageState, Bitmap bitmap, String imageName) {

        CameraLog.d(TAG, "SaveBitmap: " + imageName);
        mOnSaveImageState = onSaveImageState;
        mBitmap = bitmap;
        mImageName = imageName;
        mContext = mOnSaveImageState.getContext();
        imageDir = mContext.getExternalFilesDir(DIRECTORY_DCIM);
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

        if (mImage != null) {
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

                    mOnSaveImageState.onImageSaved(false, null);
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

                mOnSaveImageState.onImageSaved(false, null);
            } finally {
                mImage.close();
            }
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
        values.put(MediaStore.Images.Media.TITLE, "Image.jpg");
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

            mOnSaveImageState.onImageSaved(true, insertUri);

        } catch (IOException e) {
            CameraLog.e(TAG, "savePicIntoAlbum IOException: " + e.toString());

            mOnSaveImageState.onImageSaved(false, null);
        }

        CameraLog.d(TAG, "savePicIntoAlbum X");
    }

    public interface onSaveImageState {
        Context getContext();

        void onImageSaved(boolean success, Uri uri);
    }
}
