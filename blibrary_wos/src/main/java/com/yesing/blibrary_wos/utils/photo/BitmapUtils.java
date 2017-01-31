package com.yesing.blibrary_wos.utils.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.support.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

/**
 * Created by YesingBeijing on 2016/9/1.
 */
public class BitmapUtils {
    public static Bitmap bitmapScale(Bitmap bitmap, float widRatio, float heiRatio) {
        Matrix matrix = new Matrix();
        matrix.postScale(widRatio, heiRatio); //长和宽放大缩小的比例
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap getScaledDrawable(String path, int maxWidth, int minWidth) {
        /* 创建一个位图工厂的配置器对象 */
        BitmapFactory.Options options = new BitmapFactory.Options();
        /* 配置器设置为只解析图片的边框大小 */
        options.inJustDecodeBounds = true;
        /* 用位图工厂析出一个只有边框大小的数据的Option对象 */
        BitmapFactory.decodeFile(path, options);
        /* 通过配置器获取到图片的宽高 */
        float srcWidth = options.outWidth;  //图片宽
        float srcHeight = options.outHeight;//图片高
        int inSampleSize = 1;   //默认的缩放比例
        /* 如果资源位图的高或者宽大于给定的宽度 */
        if (srcWidth > maxWidth) {
            /* 根据判断宽高值哪个更大决定将用哪个作为缩放比计算的参照 */
            inSampleSize = Math.round(srcWidth / maxWidth);
        }
        /* 重新给option赋值, 这次是为了创建一个真实的位图对象 */
        options = new BitmapFactory.Options();
        /* 根据之前的计算结果,设置图片的缩放比 */
        options.inSampleSize = inSampleSize;
        /* 按照改变了缩放比的option获取位图对象 */
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        /* 如果高度是宽度的三倍以上,则裁剪图片,使高度在宽度的三倍以内 */
        if (srcHeight > srcWidth * 3f) {
            bitmap = ImageCrop(bitmap, 3);
        }

        /* 如果当前图片的宽度小于给定的最小宽度,则放大图片 */
        if (srcWidth < minWidth) {
            float v = minWidth / srcWidth;
            bitmap = bitmapScale(bitmap, v, v);
        }
        /* 如果图片宽度大于给定的最大宽度,则缩小 */
        else if (bitmap.getWidth() > maxWidth) {
            float v = (maxWidth + 0f) / bitmap.getWidth();
            bitmap = bitmapScale(bitmap, v, v);
        }

        return bitmap;
    }

    public static Bitmap getScaledDrawable(byte[] bytes, float destWidth, float destHeight, Bitmap.Config mode) {
        if (destWidth < 1) destWidth = 1;
        if (destHeight < 1) destHeight = 1;

        /* 获取只截取边缘的Option */
        BitmapFactory.Options options = getBoundOption();
        /* 用位图工厂析出一个只有边框大小的数据的Option对象 */
        decodeImg(bytes, options);

        /*获取压缩比例 */
        int inSampleSize = getSampleSize(destWidth, destHeight, options);

        options = getScaledOptions(inSampleSize);
        options.inPreferredConfig = mode;

        return decodeImg(bytes, options);
    }

    public static Bitmap decodeImg(byte[] imgByte, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        InputStream input = null;
        try {
            input = new ByteArrayInputStream(imgByte);
            SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(input, null, options));
            bitmap = (Bitmap) softRef.get();
            ;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    @NonNull
    private static BitmapFactory.Options getBoundOption() {
        /* 创建一个位图工厂的配置器对象 */
        BitmapFactory.Options options = new BitmapFactory.Options();
        /* 配置器设置为只解析图片的边框大小 */
        options.inJustDecodeBounds = true;
        return options;
    }

    private static int getSampleSize(float destWidth, float destHeight, BitmapFactory.Options options) {
    /* 通过配置器获取到图片的宽高 */
        float srcWidth = options.outWidth;  //图片宽
        float srcHeight = options.outHeight;//图片高

        int inSampleSize = 1;   //默认的缩放比例
        /* 如果资源位图的高或者宽大于屏幕 */
        if (srcWidth > destWidth || srcHeight > destHeight) {
        /* 根据判断宽高值哪个更大决定将用哪个作为缩放比计算的参照 */
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }
        return inSampleSize;
    }

    @NonNull
    private static BitmapFactory.Options getScaledOptions(int inSampleSize) {
        /* 重新给option赋值, 这次是为了创建一个真实的位图对象 */
        BitmapFactory.Options options = new BitmapFactory.Options();
        /* 根据之前的计算结果,设置图片的缩放比 */
        options.inSampleSize = inSampleSize;
        /* 按照改变了缩放比的option获取位图对象 */
        return options;
    }

    /**
     * 按照图片的宽度裁剪高度(将长图裁剪成短图)
     *
     * @param bitmap 位图对象
     * @param ratio  高对应宽的比例, 例如 ratio=2 则 如果宽是10 高就是20
     * @return 裁剪后的位图对象
     */
    public static Bitmap ImageCrop(Bitmap bitmap, float ratio) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = (int) (w * ratio);// 裁切后所取的正方形区域边长

//        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
//        int retY = w > h ? 0 : (h - w) / 2;

        //下面这句是关键
        return Bitmap.createBitmap(bitmap, 0, 0, w, wh, new Matrix(), false);
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        System.out.println("w" + bitmap.getWidth());
        System.out.println("h" + bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 将位图保存到文件中
     *
     * @param bm   位图对象
     * @param file 文件存放路径
     * @throws IOException
     */
    public static void saveBitmapAsFile(Bitmap bm, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
        bos.flush();
        bos.close();
    }

    /**
     * 将位图保存到文件中
     *
     * @param bm   位图对象
     * @param file 文件存放路径
     * @throws IOException
     */
    public static void saveBitmapAsFile(Bitmap bm, File file, int quality) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        bm.compress(Bitmap.CompressFormat.PNG, quality, bos);
        bos.flush();
        bos.close();
    }
}
