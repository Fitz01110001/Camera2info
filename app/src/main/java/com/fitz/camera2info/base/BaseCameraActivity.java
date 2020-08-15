package com.fitz.camera2info.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.manager.flash.FlashManager;
import com.fitz.camera2info.manager.Camera2Manager;
import com.fitz.camera2info.mode.ModeManager;
import com.fitz.camera2info.thumbnail.ThumbNailManager;
import com.fitz.camera2info.ui.CameraUIInterface;
import com.fitz.camera2info.utils.Util;


/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.base
 * @ClassName: BaseActivity
 * @Author: Fitz
 * @CreateDate: 2019/12/15 19:30
 */
public abstract class BaseCameraActivity extends AppCompatActivity implements CameraUIInterface {

    protected Camera2Manager mCamera2Manager = null;
    protected ModeManager mModeManager = null;
    protected FlashManager mFlashManager = null;
    protected ThumbNailManager mThumbNailManager = null;
    protected Util mUtil = null;
    private static final int PERMISSIONS_REQUEST_CODE = 10;
    private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.CAMERA,
                                                          Manifest.permission.RECORD_AUDIO,
                                                          Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                          Manifest.permission.READ_EXTERNAL_STORAGE};

    protected Camera2Manager.CameraStateCallback mCameraStateCallback = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        CameraLog.v(getTAG(), "onCreate");

        if (allPermissionsGranted()) {
            doAfterPermissionsGranted();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE);
        }

        mCamera2Manager = Camera2Manager.getCamera2Manager(this);
        mUtil = Util.getUtil(this);
        mModeManager = new ModeManager(this, this, mCamera2Manager, mUtil);
        mCamera2Manager.setModeManagerInterface(mModeManager);
        mThumbNailManager = new ThumbNailManager(getContentResolver());

    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public void setContentView(View view) {

        super.setContentView(view);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void finish() {

        super.finish();
    }

    protected abstract String getTAG();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        CameraLog.d(getTAG(), "requestCode: " + requestCode);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                doAfterPermissionsGranted();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                     .show();
                finish();
            }
        }
    }

    private void doAfterPermissionsGranted() {

        CameraLog.v(getTAG(), "doAfterPermissionsGranted");
    }

    private boolean allPermissionsGranted() {

        for (String permission : PERMISSIONS_REQUIRED) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

                CameraLog.e(getTAG(), "permission <" + permission + "> not granted!");
                return false;
            }
        }
        return true;
    }

}
