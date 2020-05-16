package com.fitz.camera2info.base;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.activity.MainActivity;
import com.fitz.camera2info.manager.Camera2Manager;
import com.fitz.camera2info.thumbnail.ThumbNailManager;
import com.fitz.camera2info.utils.Util;


/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.base
 * @ClassName: BaseActivity
 * @Author: Fitz
 * @CreateDate: 2019/12/15 19:30
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Camera2Manager mCamera2Manager = null;
    protected ThumbNailManager mThumbNailManager = null;
    protected Util mUtil = null;
    private static final int PERMISSIONS_REQUEST_CODE = 10;
    private static final String[] PERMISSIONS_REQUIRED = {Manifest.permission.CAMERA,
                                                          Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                          Manifest.permission.READ_EXTERNAL_STORAGE};

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
        mThumbNailManager = new ThumbNailManager(getContentResolver());
        mUtil = Util.getUtil(this);
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

    protected abstract void setUIEnable(Boolean enable);

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
