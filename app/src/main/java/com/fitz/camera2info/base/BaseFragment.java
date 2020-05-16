package com.fitz.camera2info.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fitz.camera2info.CameraLog;
import com.qmuiteam.qmui.arch.QMUIFragment;
import com.qmuiteam.qmui.arch.SwipeBackLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.base
 * @ClassName: BaseFragment
 * @Author: Fitz
 * @CreateDate: 2019/12/15 19:31
 */
public abstract class BaseFragment extends QMUIFragment {
    private int mBindId = -1;

    public BaseFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CameraLog.v(getTAG(),"onViewCreated");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    protected int backViewInitOffset(Context context, int dragDirection, int moveEdge) {
        if (moveEdge == SwipeBackLayout.EDGE_TOP || moveEdge == SwipeBackLayout.EDGE_BOTTOM) {
            return 0;
        }
        return QMUIDisplayHelper.dp2px(context, 100);
    }

    public abstract String getTAG();

}
