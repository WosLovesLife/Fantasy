package com.yesing.blibrary_wos.utils.systemUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by zhangh on 2016/9/7.
 */
public class SystemDaoUtils {
    /**
     * 向系统相册 Pictures 中插入图片
     *
     * @throws FileNotFoundException 文件路径错误时抛出
     */
    public static void updateSystemAlbum(Context context, String filePath, String fileName) throws FileNotFoundException {
        MediaStore.Images.Media.insertImage(context.getContentResolver(), filePath, fileName, null);
    }

    /**
     * 向系统相册 Pictures 中插入图片
     */
    public static void updateSystemAlbum(Context context, Bitmap bitmap, String title, String description) {
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, title, description);
    }

    /**
     * 通过Uri查找图片所在的路径, 该方法可以在系统相册选择图片后调用以取得图片的路径
     * @param context 上下文, 用来访问系统数据库
     * @param selectedImage 选中的图片uri
     * @return 图片路径
     */
    public static String getPicByUri(Context context, Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            if (picturePath == null || picturePath.equals("null")) {
                return null;
            }
            return picturePath;
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                return null;
            }
            return file.getAbsolutePath();
        }
    }
}
