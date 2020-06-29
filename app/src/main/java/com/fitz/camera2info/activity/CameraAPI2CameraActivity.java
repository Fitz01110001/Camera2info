package com.fitz.camera2info.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.fitz.camera2info.R;
import com.fitz.camera2info.base.BaseCameraActivity;
import com.fitz.camera2info.mode.ModeManager;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.activity
 * @ClassName: CameraAPI2Activity
 * @Author: Fitz
 * @CreateDate: 2020/5/17 22:37
 */
public class CameraAPI2CameraActivity extends BaseCameraActivity {

    public static final String TAG = "CameraAPI2Activity";
    @BindView(R.id.topbar_dump) QMUITopBarLayout topbarDump;
    @BindView(R.id.tv_dump_info) TextView tvDumpInfo;
    @BindView(R.id.layout_camera_dump) ConstraintLayout layoutCameraDump;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_dump);
        ButterKnife.bind(this);

        initTopBar();
    }

    protected void initTopBar() {

        topbarDump.setTitle(R.string.camera_dump);
        topbarDump.addLeftBackImageButton()
                  .setOnClickListener(v -> onBackPressed());
    }


    @Override
    protected String getTAG() {

        return TAG;
    }

    @Override
    public void setUIEnable(Boolean enable) {

    }

    @Override
    public void setUICurrentMode(ModeManager.ModeName modeName) {

    }
}
