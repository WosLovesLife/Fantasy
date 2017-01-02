package com.yesing.blibrary_wos.utils.systemUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by YesingBeijing on 2016/9/5.
 */
public class IntentUtils {
    public static Intent getInstallAppIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 设置目标应用安装包路径
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        return intent;
    }

    @Deprecated
    public static Intent getSelectPhotoIntent() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        return intent;
    }

    @Deprecated
    public static Intent getSelectPhotoIntent(int maxNum) {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

        return intent;
    }

    @Deprecated
    public static Intent getSelectCropPhoto(int size) {
        return getSelectCropPhoto(size, size, 1, 1);
    }

    /**
     * 从系统相册获取图片并剪裁. 详见blog: https://my.oschina.net/ryanhoo/blog/86843
     *
     * @param outputX   // 裁剪区的宽 int
     * @param outputY   // 裁剪区的高 int
     * @param aspectX   // X方向上的比例 int
     * @param aspectY   // Y方向上的比例 int
     * @return 从相册获取并剪裁的Intent
     */
    @Deprecated
    public static Intent getSelectCropPhoto(int outputX, int outputY, int aspectX, int aspectY) {
        Intent intent = getSelectPhotoIntent();
        //开启裁剪功能
        intent.putExtra("crop", "true");
        //设定宽高的比例
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        //设定裁剪图片宽高
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        //要求返回数据
        intent.putExtra("return-data", true);
        return intent;
    }

    /**
     * 从系统相机拍摄视频
     *
     * @param file 视频存储路径
     * @return Intent
     */
    @Deprecated
    public static Intent getPickVideoIntent(File file) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);//create a intent to record video
        Uri parse = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, parse);    //存储路径
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // 拍摄质量 只有0和1 部分手机不支持0
        return intent;
    }
}
