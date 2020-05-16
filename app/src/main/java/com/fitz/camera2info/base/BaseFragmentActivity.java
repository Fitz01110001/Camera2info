package com.fitz.camera2info.base;

import android.os.Bundle;

import com.fitz.camera2info.CameraLog;
import com.qmuiteam.qmui.arch.QMUIFragmentActivity;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.base
 * @ClassName: BaseFragmentActivity
 * @Author: Fitz
 * @CreateDate: 2019/12/15 19:30
 */
public abstract class BaseFragmentActivity extends QMUIFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraLog.v(getTAG(),"onCreate");
    }

    public abstract String getTAG();
}
