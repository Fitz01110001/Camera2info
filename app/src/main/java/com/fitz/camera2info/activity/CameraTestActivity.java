package com.fitz.camera2info.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.R;
import com.fitz.camera2info.base.BaseActivity;
import com.fitz.camera2info.camerainfo.CameraItem;
import com.fitz.camera2info.manualtest.CameraManualTest;
import com.fitz.camera2info.utils.Util;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.fragment
 * @ClassName: CameraTestActivity
 * @Author: Fitz
 * @CreateDate: 2019/12/15 23:31
 */
public class CameraTestActivity extends BaseActivity {

    @BindView(R.id.topbar_test) QMUITopBarLayout topbar;
    @BindView(R.id.camera_test_list) QMUIGroupListView cameraTestList;

    private static final String TAG = "CameraTestActivity";
    private List<CameraItem> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        ButterKnife.bind(this);

        initTopBar();
        initListView();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initListView() {
        int size = QMUIDisplayHelper.dp2px(this, 20);
        QMUIGroupListView.Section section = QMUIGroupListView.newSection(this);

        for (String cameraId : mUtil.getAllCameraIds()) {
            CameraItem cameraItem = mUtil.getCameraItem(cameraId);
            mCamera2Manager.addCameraItem(cameraId, cameraItem);

            QMUICommonListItemView itemCamera = cameraTestList.createItemView(setCameraNameText(cameraItem));
            itemCamera.setOrientation(QMUICommonListItemView.VERTICAL);
            itemCamera.setAccessoryType(QMUICommonListItemView.ACCESSORY_TYPE_CHEVRON);

            if(cameraItem.isMulitCamera()){
                itemCamera.setDetailText(cameraItem.getPhysicalCameraIds());
            }

            section.addItemView(itemCamera, onClickListener);
        }

        section.setLeftIconSize(size, ViewGroup.LayoutParams.WRAP_CONTENT)
               .setMiddleSeparatorInset(QMUIDisplayHelper.dp2px(this, Util.INSET_LEFT), Util.INSET_RIGHT)
               .addTo(cameraTestList);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String text = ((QMUICommonListItemView) v).getText()
                                                      .toString();

            int cameraid = Integer.valueOf(text.split(":")[1].trim());

            CameraLog.d(TAG, "onClick, cameraid: " + cameraid);

            Intent intent = new Intent(getContext(), CameraManualTest.class);
            intent.putExtra(Util.CAMERAID, String.valueOf(cameraid));
            startActivity(intent);
        }
    };

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    protected void setUIEnable(Boolean enable) {

    }

    protected void initTopBar() {
        topbar.setTitle(R.string.camera_check);
        topbar.addLeftBackImageButton()
              .setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      onBackPressed();
                  }
              });
    }

    private Context getContext() {
        return (Context) this;
    }

    private String setCameraNameText(CameraItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.getCameraFront()) {
            sb.append(getResources().getString(R.string.front_camera));
            sb.append(" id: ");
            sb.append(item.getCameraId());

            return sb.toString();
        } else {
            sb.append(getResources().getString(R.string.rear_camera));
            sb.append(" id: ");
            sb.append(item.getCameraId());

            return sb.toString();
        }

    }

    private String setCameraDetailText(CameraItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.isMulitCamera()) {
            sb.append(getResources().getString(R.string.mulit_logic_camera));
            sb.append(": ");
            sb.append(item.getPhysicalCameraIds());

            return sb.toString();
        } else {
            sb.append(getResources().getString(R.string.single_physical_id));
            sb.append(": ");
            sb.append(item.getCameraId());

            return sb.toString();
        }
    }

}
