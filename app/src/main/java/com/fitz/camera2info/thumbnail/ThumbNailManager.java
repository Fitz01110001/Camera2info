package com.fitz.camera2info.thumbnail;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.RequiresApi;

import com.fitz.camera2info.CameraLog;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.thumbnail
 * @ClassName: ThumbNailManager
 * @Author: Fitz
 * @CreateDate: 2020/5/16 21:01
 */
public class ThumbNailManager {

    public static final String TAG = "ThumbNailManager";
    private static final String SAVE_THUMBNAIL_PATH = Environment.getExternalStorageDirectory() + "/.tmp";
    public ThumbnailCallback mThumbnailCallback = null;
    private ContentResolver mContentResolver = null;
    private CancellationSignal mCancellationSignal = null;

    public ThumbNailManager(ContentResolver contentResolver) {

        mContentResolver = contentResolver;
        mCancellationSignal = new CancellationSignal();
    }

    public void setThumbnailCallback(ThumbnailCallback thumbnailCallback) {

        mThumbnailCallback = thumbnailCallback;
    }

    public void onResume() {

        Uri uri = getLastestPicThumbnailFromDataBase();
        if (null != uri) {
            updateThumbnail(uri);
        }
    }

    public interface ThumbnailCallback {
        void updateThumbnail(Uri uri, Bitmap bitmap);
    }

    public Uri getLastestPicThumbnailFromDataBase() {

        CameraLog.d(TAG, "getLastestPicThumbnail");

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projImage = {MediaStore.Images.Media._ID,
                              MediaStore.Images.Media.DATA,
                              MediaStore.Images.Media.SIZE,
                              MediaStore.Images.Media.MIME_TYPE,
                              MediaStore.Images.Media.DISPLAY_NAME,
                              MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String selection =
                "(" + MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?) AND (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=? OR " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?)";
        String[] seslecionArgs = {"image/jpeg", "image/png", "Camera", "FitzCamera"};
        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";
        Cursor mCursor = mContentResolver.query(uri, projImage, selection, seslecionArgs, sortOrder);
        mCursor.moveToFirst();
        String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));  // 图片的路径
        String id = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
        mCursor.close();

        Uri imageUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + File.separator + id);

        CameraLog.e(TAG, "getLastestPicThumbnail, path: " + path + ", id: " + id);
        return imageUri;
    }

    public void updateThumbnail(Uri uri) {

        CameraLog.d(TAG, "updateThumbnail, uri: " + uri.toString());

        Bitmap bitmap = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                bitmap = mContentResolver.loadThumbnail(uri, new Size(100, 100), mCancellationSignal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mThumbnailCallback.updateThumbnail(uri, bitmap);
    }

}
