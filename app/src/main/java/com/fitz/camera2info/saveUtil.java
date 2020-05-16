package com.fitz.camera2info;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

public class saveUtil {

    private static String TAG = "Fitz- saveUtil";
    private static boolean isFirstWrite = true;
    private static File file = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
    private static FileOutputStream fileOutputStream;

    /**
     * 功能：已知字符串内容，输出到文件
     *
     * @param fileName 要写文件的文件名，如：abc.txt
     * @param string   要写文件的文件内容
     */
    public static void write(Context context, String fileName, String string) throws IOException {
        Log.d(TAG, "isFirstWrite:" + isFirstWrite);
        // 首先判断文件夹是否存在
        if (!file.exists()) {
            if (!file.mkdirs()) {   // 文件夹不存在则创建文件
                Toast.makeText(context, "文件夹创建失败", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "文件夹创建失败");
            }
        } else {
            File fileWrite = new File(file + "/" + fileName);
            if (isFirstWrite) {
                // 实例化对象：文件输出流
                fileOutputStream = new FileOutputStream(fileWrite);
                isFirstWrite = false;
            } else {
                fileOutputStream = new FileOutputStream(fileWrite, true);
            }

            // 写入文件
            fileOutputStream.write(string.getBytes());

            // 清空输出流缓存
            fileOutputStream.flush();

            // 关闭输出流
            fileOutputStream.close();
        }
    }


}
