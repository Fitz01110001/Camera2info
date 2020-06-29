package com.fitz.camera2info.ui;

import com.fitz.camera2info.mode.ModeManager;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.ui
 * @ClassName: CameraUIManager
 * @Author: Fitz
 * @CreateDate: 2020/5/23 12:47
 */
public interface CameraUIInterface {

    void setUIEnable(Boolean enable);

    void setUICurrentMode(ModeManager.ModeName modeName);


}
