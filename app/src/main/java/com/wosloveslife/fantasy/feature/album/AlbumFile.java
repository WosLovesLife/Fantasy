package com.wosloveslife.fantasy.feature.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import com.yesing.blibrary_wos.utils.systemUtils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by zhangh on 2017/2/15.
 */

public class AlbumFile {

    private static File getAlbumDir(Context context) {
        File albumsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "albums");
        if (!albumsDir.exists()) {
            if (!albumsDir.mkdir()) {
                albumsDir = new File(context.getFilesDir(), "albums");
            }
        }
        return albumsDir;
    }

    private static boolean hasAlbum(Context context, String albumName) {
        File albumFile = new File(getAlbumDir(context), albumName);
        return albumFile.exists() && albumFile.length() > 0;
    }

    public static File getAlbumFile(Context context, String albumName) {
        if (context == null || TextUtils.isEmpty(albumName)) return null;

        File albumFile = new File(getAlbumDir(context), albumName);
        if (albumFile.exists()) {
            return albumFile;
        }
        return null;
    }

    public static boolean saveAlbum(Context context, String albumName, Bitmap bitmap) {
        if (context == null || TextUtils.isEmpty(albumName) || bitmap == null) return false;

        if (hasAlbum(context, albumName)) return true;

        File albumFile = new File(getAlbumDir(context), albumName);
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(albumFile));
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bufferedOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeStream(bufferedOutputStream);
        }
        return false;
    }
}
