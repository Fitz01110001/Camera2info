package com.fitz.camera2info.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.R;
import com.fitz.camera2info.base.BaseCameraActivity;
import com.fitz.camera2info.mode.ModeManager;
import com.fitz.camera2info.utils.Util;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info
 * @ClassName: MainActivity
 * @Author: Fitz
 * @CreateDate: 2019/12/15 20:54
 */
public class MainCameraActivity extends BaseCameraActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.topbar) QMUITopBarLayout topbar;
    @BindView(R.id.camera_test_list) QMUIGroupListView mGroupListView;
    @BindView(R.id.scrollView) ScrollView scrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initTopBar();
        initGroupListView();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    protected void initTopBar() {

        topbar.setTitle(R.string.app_name);
    }

    private void initGroupListView() {

        int height = QMUIResHelper.getAttrDimen(this, com.qmuiteam.qmui.R.attr.qmui_list_item_height);

        // item 1 camera check
        QMUICommonListItemView itemCameraCheck = mGroupListView.createItemView(getString(R.string.camera_check));
        itemCameraCheck.setOrientation(QMUICommonListItemView.VERTICAL);
        itemCameraCheck.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        // item 2 camera dump
        QMUICommonListItemView itemCameraDump = mGroupListView.createItemView(getString(R.string.camera_dump));
        itemCameraDump.setOrientation(QMUICommonListItemView.VERTICAL);
        itemCameraDump.setDetailText(getString(R.string.camera_dump_about));
        itemCameraDump.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        // item 3 camera api2 check
        QMUICommonListItemView itemCamera2API = mGroupListView.createItemView(getString(R.string.camera_api2_check));
        itemCameraDump.setOrientation(QMUICommonListItemView.VERTICAL);
        itemCameraDump.setDetailText(getString(R.string.camera_api2_about));
        itemCameraDump.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        // item 4 cameraX check
        QMUICommonListItemView itemCameraXAPI = mGroupListView.createItemView(getString(R.string.camera_X_check));
        itemCameraDump.setOrientation(QMUICommonListItemView.VERTICAL);
        itemCameraDump.setDetailText(getString(R.string.camera_X_about));
        itemCameraDump.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String text = ((QMUICommonListItemView) v).getText()
                                                          .toString();
                if (v instanceof QMUICommonListItemView) {
                    if (((QMUICommonListItemView) v).getAccessoryType() == QMUICommonListItemView.ACCESSORY_TYPE_SWITCH) {
                        ((QMUICommonListItemView) v).getSwitch()
                                                    .toggle();
                    }

                    if (((QMUICommonListItemView) v).getAccessoryType() == QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON) {
                        if (text.equals(getString(R.string.camera_check))) {
                            CameraLog.d(getTAG(), "check");

                            Intent intent = new Intent();
                            intent.setClass(getContext(), CameraTestCameraActivity.class);
                            startActivity(intent);

                            return;
                        } else if (text.equals(getString(R.string.camera_dump))) {
                            CameraLog.d(getTAG(), "dump");

                            Intent intent = new Intent();
                            intent.setClass(getContext(), CameraDumpCameraActivity.class);
                            startActivity(intent);

                            return;
                        } else if (text.equals(getString(R.string.camera_api2_check))) {
                            CameraLog.d(getTAG(), "check api2");

                            Intent intent = new Intent();
                            intent.setClass(getContext(), CameraAPI2CameraActivity.class);
                            startActivity(intent);

                            return;
                        } else if (text.equals(getString(R.string.camera_X_check))) {
                            CameraLog.d(getTAG(), "check apiX");


                        }

                    }

                }
            }
        };

        int size = QMUIDisplayHelper.dp2px(this, 20);
        QMUIGroupListView.newSection(this)
                         //.setTitle("Section 1: 默认提供的样式")
                         //.setDescription("Section 1 的描述")
                         .setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
                         .addItemView(itemCameraCheck, onClickListener)
                         .addItemView(itemCameraDump, onClickListener)
                         .addItemView(itemCamera2API, onClickListener)
                         .addItemView(itemCameraXAPI, onClickListener)
                         .setMiddleSeparatorInset(QMUIDisplayHelper.dp2px(this, Util.INSET_LEFT), Util.INSET_RIGHT)
                         .addTo(mGroupListView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        CameraLog.d(getTAG(), "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        CameraLog.d(getTAG(), "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraLog.d(getTAG(), "onDestroy");
    }

    @Override
    public String getTAG() {

        return TAG;
    }

    private Context getContext() {

        return (Context) this;
    }

    private Activity getActivity() {

        return (Activity) this;
    }

    @Override
    public void setUIEnable(Boolean enable) {

    }

    @Override
    public void setUICurrentMode(ModeManager.ModeName modeName) {

    }
}
