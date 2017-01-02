package com.yesing.blibrary_wos.utils.systemUtils;

import android.app.Activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zhangh on 2016/9/7.
 */
public class IOUtils {
    public interface OnCompleteListener {
        void onComplete(File target);

        void onError(Throwable e);
    }

    /** 通过流的方式拷贝文件 */
    public static void copyFileTo(final Activity activity, final File resource, final File target, final OnCompleteListener listener) {
        new Thread() {
            @Override
            public void run() {
                BufferedInputStream bufferedInputStream = null;
                BufferedOutputStream bufferedOutputStream = null;
                try {
                    bufferedInputStream = new BufferedInputStream(new FileInputStream(resource));
                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(target));
                    byte[] bytes = new byte[1024 << 3];
                    int read = -1;
                    while ((read = bufferedInputStream.read(bytes)) > -1) {
                        bufferedOutputStream.write(bytes, 0, read);
                    }
                    if (listener != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onComplete(target);
                            }
                        });
                    }
                } catch (final IOException e) {
                    if (listener != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onError(e);
                            }
                        });
                    }
                } finally {
                    closeStream(bufferedInputStream);
                    closeStream(bufferedOutputStream);
                }
            }
        }.start();
    }

    /** 关闭流, 直接在final中调用即可 */
    public static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** 获取文件或文件夹的大小 */
    public static long getFileSize(File file) {
        synchronized (IOUtils.class) {

            if (file == null || !file.exists()) return 0;

            if (file.isDirectory()) {
                return getSize(file);
            }

            return file.length();
        }
    }

    private static long getSize(File file) {
        long size = 0;
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                size += getSize(f);
            } else {
                size += f.length();
            }
        }
        return size;
    }

    /** 删除文件或文件夹 */
    public static boolean deleteFile(File file) {
        synchronized (IOUtils.class) {

            if (file == null || !file.exists()) return false;

            if (file.isDirectory()) {
                try {
                    deleteContents(file);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return file.delete();
        }
    }

    static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + dir);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }
}
