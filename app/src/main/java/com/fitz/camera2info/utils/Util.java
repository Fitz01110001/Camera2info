package com.fitz.camera2info.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Size;

import com.fitz.camera2info.CameraLog;
import com.fitz.camera2info.camerainfo.CameraItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @ProjectName: Camera2Info
 * @Package: com.fitz.camera2info.utils
 * @ClassName: Util
 * @Author: Fitz
 * @CreateDate: 2019/12/16 0:57
 */
public class Util {

    public static final String CAMERAID_INVALID = "-1";
    public static final String REAR_MAIN_CAMERA = "0";
    public static final String FRONT_MAIN_CAMERA = "1";
    public static final String REAR_SECOND_CAMERA = "2";
    public static final String REAR_THIRD_CAMERA = "3";
    public static final String REAR_FOURTH_CAMERA = "4";
    public static final String BOARD_PLATFORM_MTK = "mt";
    public static final String BOARD_PLATFORM_QUALCOMM = "msm";
    public static final String BOARD_PLATFORM_HUAWEI = "kirin";
    public static final String BOARD_PLATFORM_SAMSUNG = "SAMSUNG";
    public static final String BOARD_PLATFORM_SPREADTRUM = "SPREADTRUM";
    public static final String BOARD_PLATFORM_UNKNOW = "unknow";

    public static final String CAMERAID = "cameraid";
    public static final int INSET_LEFT = 16;
    public static final int INSET_RIGHT = 0;
    public static final float SIZE_4_3 = 4f / 3f;
    public static final float SIZE_16_9 = 16f / 9f;

    private static final String TAG = "Util";
    private volatile static Util util;
    private CameraManager mCameraManager = null;
    private Context mContext = null;
    private String[] allCameraIds = null;
    private String devicePlatform = null;
    private static ArrayList<String> mCameraList = new ArrayList<>();
    private static ArrayList<String> mRearCameraList = new ArrayList<>();
    private static ArrayList<String> mFrontCameraList = new ArrayList<>();
    private HashMap<String, Boolean> cameraFacingMap = new HashMap<>();
    private Hashtable<String, CameraItem> mCameraItemTable = new Hashtable<>();


    public static Util getUtil(Context context) {

        if (util == null) {
            synchronized (Util.class) {
                if (util == null) {
                    util = new Util(context);
                }
            }
        }
        return util;
    }

    public static int getScreenWidth(Context context) {

        if (context == null) {
            return -1;
        }

        return context.getResources()
                      .getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {

        if (context == null) {
            return -1;
        }

        return context.getResources()
                      .getDisplayMetrics().heightPixels;
    }

    public Util(Context context) {

        mContext = context;
        getCommonInfo();

        if (mCameraManager == null) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }

        try {
            allCameraIds = mCameraManager.getCameraIdList();
            CameraLog.v(TAG, "This device has " + allCameraIds.length + " cameras.");

            for (String camera : allCameraIds) {
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(camera);
                List<CameraCharacteristics.Key<?>> keyList = cameraCharacteristics.getKeys();

                boolean facingFront = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
                cameraFacingMap.put(camera, facingFront);

                mCameraList.add(camera);
                if (facingFront) {
                    mFrontCameraList.add(camera);
                } else {
                    mRearCameraList.add(camera);
                }

                mCameraItemTable.put(camera,
                                     CameraItem.newInstance()
                                               .setCameraId(camera)
                                               .setCameraFront(getCameraFacing(camera))
                                               .setMulitCamera(isMulitLogicalCameraId(camera))
                                               .setPhysicalCameraIds(getPhysicalCameraIds(camera))
                                               .setCameraDirection(getDirection(cameraCharacteristics))
                                               .setCameraCharacteristics(cameraCharacteristics));

                /*if (isMulitLogicalCameraId(camera)) {
                    getMainPhysicalId(camera);
                }*/

            }


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public int getDirection(CameraCharacteristics cameraCharacteristics) {

        return cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
    }

    public boolean isMulitLogicalCameraId(String cameraId) {

        if (Build.VERSION.SDK_INT <= 29) {
            return false;
        }

        int physicalCameraIds = getPhysicalCameraIdNums(cameraId);
        boolean mulitCam = physicalCameraIds > 1;

        if (mulitCam) {
            CameraLog.d(TAG, "isMulitLogicalCameraId, " + cameraId + " true!");
        } else {
            CameraLog.d(TAG, "isMulitLogicalCameraId, " + cameraId + " false!");
        }

        return mulitCam;

    }

    public boolean isMultiCam(String cameraId) {

        return (isDualCam(cameraId) || isTripleCam(cameraId));
    }

    public boolean isDualCam(String cameraId) {

        int physicalCameraIds = getPhysicalCameraIdNums(cameraId);
        return physicalCameraIds == 2;
    }

    public boolean isTripleCam(String cameraId) {

        int physicalCameraIds = getPhysicalCameraIdNums(cameraId);
        return physicalCameraIds == 3;
    }

    private int getPhysicalCameraIdNums(String cameraId) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return mCameraManager.getCameraCharacteristics(cameraId)
                                     .getPhysicalCameraIds()
                                     .size();
            }
        } catch (CameraAccessException e) {
            CameraLog.e(TAG, "getPhysicalCameraIdNums, shit!");
            e.printStackTrace();
        }

        return -1;
    }

    public String[] getPhysicalCameraIdsArray(String cameraId) {

        try {
            Set<String> physicalCameraIdList = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                physicalCameraIdList = mCameraManager.getCameraCharacteristics(cameraId)
                                                                 .getPhysicalCameraIds();
            }
            if (physicalCameraIdList.isEmpty()) {
                CameraLog.w(TAG, "cameraId :" + cameraId + " is not a MulitLogicalCameraId!");
            } else {
                CameraLog.d(TAG, "getPhysicalCameraIdsArray, cameraId :" + cameraId + " has " + toArrayString(physicalCameraIdList));
            }

            return physicalCameraIdList.toArray(new String[physicalCameraIdList.size()]);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPhysicalCameraIds(String cameraId) {
        if (Build.VERSION.SDK_INT <= 29) {
            return null;
        }

        try {
            Set<String> physicalCameraIdList = mCameraManager.getCameraCharacteristics(cameraId)
                                                             .getPhysicalCameraIds();
            if (physicalCameraIdList.isEmpty()) {
                CameraLog.w(TAG, "cameraId :" + cameraId + " is not a MulitLogicalCameraId!");
            } else {
                CameraLog.d(TAG, "getPhysicalCameraIdsArray, cameraId :" + cameraId + " has " + toArrayString(physicalCameraIdList));
            }

            return toArrayString(physicalCameraIdList);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Size getDefaultSizeByCameraId(String cameraId) {

        try {
            return Arrays.asList(mCameraManager.getCameraCharacteristics(cameraId)
                                               .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                               .getOutputSizes(ImageFormat.JPEG))
                         .get(0);
        } catch (CameraAccessException e) {
            CameraLog.e(TAG, "getDefaultSizeByCameraId, shit!");
            e.printStackTrace();
        }

        return null;
    }

    public String getMainPhysicalId(String cameraId) {

        String id = CAMERAID_INVALID;
        String[] physicalCameraIdList = getPhysicalCameraIdsArray(cameraId);

        if (physicalCameraIdList.length > 1) {
            id = physicalCameraIdList[0];
        }
        CameraLog.d(TAG, "getMainPhysicalId, Logical: " + cameraId + " MainPhysical: " + id);

        return id;
    }

    private void getCommonInfo() {

        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("getprop ro.board.platform", false);
        if (commandResult.result == 0) {
            String platform = commandResult.successMsg;
            CameraLog.d(TAG, "getCommonInfo, result:" + platform);

            if (platform.contains(BOARD_PLATFORM_HUAWEI)) {
                devicePlatform = BOARD_PLATFORM_HUAWEI;
                CameraLog.d(TAG, "getCommonInfo, This is a HUAWEI device");
            } else if (platform.contains(BOARD_PLATFORM_MTK)) {
                devicePlatform = BOARD_PLATFORM_MTK;
                CameraLog.d(TAG, "getCommonInfo, This is a MTK device");
            } else if (platform.contains(BOARD_PLATFORM_QUALCOMM)) {
                devicePlatform = BOARD_PLATFORM_QUALCOMM;
                CameraLog.d(TAG, "getCommonInfo, This is a QC device");
            } else if (platform.contains(BOARD_PLATFORM_SAMSUNG)) {
                devicePlatform = BOARD_PLATFORM_SAMSUNG;
                CameraLog.d(TAG, "getCommonInfo, This is a SAMSUNG device");
            } else if (platform.contains(BOARD_PLATFORM_SPREADTRUM)) {
                devicePlatform = BOARD_PLATFORM_SPREADTRUM;
                CameraLog.d(TAG, "getCommonInfo, This is a SPREADTRUM device");
            } else {
                devicePlatform = BOARD_PLATFORM_UNKNOW;
                CameraLog.e(TAG, "getCommonInfo, This is a UNKNOW device!!! platform is :" + platform);
            }
        }
    }

    public boolean isHWPlatform() {

        return devicePlatform == BOARD_PLATFORM_HUAWEI;
    }

    public boolean isMTKPlatform() {

        return devicePlatform == BOARD_PLATFORM_MTK;
    }

    public boolean isQCPlatform() {

        return devicePlatform == BOARD_PLATFORM_QUALCOMM;
    }

    public boolean isSAMSUNGPlatform() {

        return devicePlatform == BOARD_PLATFORM_SAMSUNG;
    }

    public boolean isSPREADTRUMPlatform() {

        return devicePlatform == BOARD_PLATFORM_SPREADTRUM;
    }

    public int getCameraIdNum() {

        return allCameraIds.length;
    }

    public String[] getAllCameraIds() {

        return allCameraIds;
    }

    public static ArrayList<String> getRearCameraList() {

        return mRearCameraList;
    }

    public static ArrayList<String> getFrontCameraList() {

        return mFrontCameraList;
    }

    public int getRearCameraIdNum() {

        return mRearCameraList.size();
    }

    public int getFrontCameraIdNum() {

        return mFrontCameraList.size();
    }

    public boolean getCameraFacing(String camera) {

        return cameraFacingMap.get(camera);
    }

    private String toArrayString(Set<String> list) {

        Iterator it = list.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        while (it.hasNext()) {
            sb.append(it.next());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    public CameraManager getCameraManager() {

        return mCameraManager;
    }

    public CameraItem getCameraItem(String cameraId) {

        return mCameraItemTable.get(cameraId);
    }

    public void playSound() {

        AudioManager meng = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        if (volume != 0) {
            MediaPlayer shootMP = MediaPlayer.create(mContext, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            shootMP.start();
        }
    }

    public String getKeyValue(CameraCharacteristics cameraCharacteristics, CameraCharacteristics.Key<?> key) {

        if (null == key) {
            return null;
        }

        Object config = null;
        String result = null;

        config = cameraCharacteristics.get(key);

        if (config instanceof Byte) {
            result = String.valueOf((Byte) config);
        } else if (config instanceof Integer) {
            result = String.valueOf((Integer) config);
        } else if (config instanceof Float) {
            result = String.valueOf((Float) config);
        } else if (config instanceof Long) {
            result = String.valueOf((Long) config);
        } else if (config instanceof Double) {
            result = String.valueOf((Double) config);
        } else if (config instanceof int[]) {
            int[] intArray = (int[]) config;

            if (null != intArray) {
                for (int i = 0; i < intArray.length; i++) {
                    if (0 == i) {
                        result = String.valueOf(intArray[0]);
                    } else {
                        result += ("x" + intArray[i]);
                    }
                }
            }
        }

        return result;
    }
}
