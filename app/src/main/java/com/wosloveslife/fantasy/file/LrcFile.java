package com.wosloveslife.fantasy.file;

import android.content.Context;
import android.text.TextUtils;

import com.yesing.blibrary_wos.utils.systemUtils.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by zhangh on 2017/2/15.
 */

public class LrcFile {
    private static File getLrcDir(Context context) {
        File albumsDir = new File(context.getExternalFilesDir(null), "lyrics");
        if (!albumsDir.exists()) {
            if (!albumsDir.mkdir()) {
                albumsDir = new File(context.getFilesDir(), "lyrics");
            }
        }
        return albumsDir;
    }

    public static String getLrc(Context context, String title) {
        File lrcFile = new File(getLrcDir(context), title);
        if (lrcFile.exists()) {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(lrcFile));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeStream(bufferedReader);
            }
        }
        return null;
    }

    public static boolean hasLrc(Context context, String title) {
        File albumFile = new File(getLrcDir(context), title);
        return albumFile.exists() && albumFile.length() > 0;
    }

    public static boolean saveLrc(Context context, String title, String lrc) {
        if (context == null || TextUtils.isEmpty(title) || TextUtils.isEmpty(lrc)) return false;

        if (hasLrc(context, title)) return true;

        File lrcFile = new File(getLrcDir(context), title);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(lrcFile));
            bufferedWriter.write(lrc);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(bufferedWriter);
        }
        return false;
    }
}
