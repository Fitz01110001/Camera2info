package com.fitz.camera2info.manager;

import android.os.Handler;

import java.util.concurrent.Executor;

/**
 * @ProjectName: Camera2info
 * @Package: com.fitz.camera2info.manager
 * @ClassName: HandlerExecutor
 * @Author: Fitz
 * @CreateDate: 2020/4/18 10:51
 */
public class HandlerExecutor implements Executor {
    private final Handler mHandler;

    public HandlerExecutor(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void execute(Runnable runnable) {
        mHandler.post(runnable);
    }
}
