package com.fitz.camera2info.activity;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.R;
import com.fitz.camera2info.base.BaseActivity;
import com.fitz.camera2info.camerainfo.CameraItem;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.fragment
 * @ClassName: CameraDumpActivity
 * @Author: Fitz
 * @CreateDate: 2019/12/15 23:32
 */
public class CameraDumpActivity extends BaseActivity {

    private static final String TAG = "CameraDumpActivity";
    private String[] mCameraIds = null;
    private String mDumpInfo = null;

    @BindView(R.id.tv_dump_info) TextView tvDumpInfo;

    @BindView(R.id.topbar_dump) QMUITopBarLayout topbarDump;


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
                  .setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          onBackPressed();
                      }
                  });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDumpInfo = getCameraDumpInfo();
        tvDumpInfo.setText(mDumpInfo);
    }

    private String getCameraDumpInfo() {
        StringBuilder sb = new StringBuilder();

        mCameraIds = mUtil.getAllCameraIds();
        for (String cameraId : mCameraIds) {
            sb.append("cameraId: " + cameraId)
              .append("\n")
              .append("CameraCharacteristics.Key: ")
              .append("\n\t");

            CameraItem cameraItem = mUtil.getCameraItem(cameraId);
            CameraLog.d(TAG, cameraItem.toString());

            //CameraCharacteristics.Key
            CameraCharacteristics characteristics = cameraItem.getCameraCharacteristics();
            List<CameraCharacteristics.Key<?>> keys = characteristics.getKeys();
            for (CameraCharacteristics.Key<?> key : keys) {
                sb.append(getKeyName(key.toString()))
                  .append(":")
                  .append("\n\t")
                  .append("[")
                  .append(mUtil.getKeyValue(characteristics, key))
                  .append("]")
                  .append("\n\t");

                CameraLog.d(TAG, "key: " + key.toString());
            }

            //
            List<CaptureRequest.Key<?>> captureRequestKeys = characteristics.getAvailableCaptureRequestKeys();
            for(CaptureRequest.Key<?> key : captureRequestKeys){
                sb.append(getKeyName(key.toString()))
                  .append("\n\t");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public String getKeyName(String s) {
        String keyName = s.substring(s.indexOf("(") + 1, s.indexOf(")"));

        return keyName;
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    protected void setUIEnable(Boolean enable) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
