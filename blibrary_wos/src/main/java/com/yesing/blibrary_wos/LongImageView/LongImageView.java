package com.yesing.blibrary_wos.LongImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.yesing.blibrary_wos.utils.systemUtils.IOUtils;
import com.yesing.blibrary_wos.utils.photo.BitmapUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 设置长图片,自动缩放
 * Created by WosLovesLIfe on 2016/9/7.
 */
public class LongImageView extends ScrollView {
    private Context mContext;
    private LinearLayout mLinearLayout;

    private InputStream mPictureStream;
    private boolean mDelayedDispose;

    private int mWidth;
    private int mHeight;

    public LongImageView(Context context) {
        this(context, null);
    }

    public LongImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LongImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        mLinearLayout = new LinearLayout(context);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        addView(mLinearLayout);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        if (mDelayedDispose && mPictureStream != null) {
            dispose(mPictureStream);
            mDelayedDispose = false;
        }
    }

    private void dispose(final InputStream pictureStream) {
        /* 设置只解析边框大小 */
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(pictureStream, null, options);
        final int outWidth = options.outWidth;
        int outHeight = options.outHeight;

        /* 计算缩放的比例 根据这个值调整控件的大小 */
        final float ratio = (mWidth + 0f) / outWidth;

        /* 用于图片压缩,当图片尺寸大于屏幕尺寸时记录 */
        int sampleSize = 1;
        if (outWidth > mWidth) {
            sampleSize = Math.round(outWidth / mWidth);
        }

        /* 配置解析模式, 压缩比例, 不只解析边框, 用RGB色彩(节省内存开支) */
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        /* 计算出控件总高度 = 图片的高度 * 缩放的比例 */
        int totalHeight = (int) (outHeight * ratio);

        /* 计算出图片切割的个数, 因为系统的单个控件对尺寸有限制 */
        final int count = totalHeight / mHeight;

        /* 计算出单个控件的高度 */
        final int destHeight = outHeight / count;

        final Handler handler = getHandler();
        /*
         * 通过 BitmapRegionDecoder 将原始的图片分割成小份
         * 然后使用 BitmapUtils.bitmapScale() 工具 对图片进行缩放
         * 最后产生一个新的ImageView盛放图片, 在将其添加到布局控件中
         */
        for (int i = 0; i < count; i++) {
            final ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mLinearLayout.addView(imageView);
            final Rect rect = new Rect(0, destHeight * i, outWidth, destHeight * (i + 1));

            new Thread() {
                @Override
                public void run() {
                    try {
                        Bitmap bitmap = BitmapRegionDecoder.newInstance(pictureStream, false).decodeRegion(rect, options);

                        if (ratio != 1) {
                            bitmap = BitmapUtils.bitmapScale(bitmap, ratio, ratio);
                        }
                        final Bitmap finalNewImage = bitmap;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(finalNewImage);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        checkIOClose(pictureStream,count);
                    }
                }
            }.start();
        }
    }

    int mCompleteStream;

    /**
     * 当所有地方都使用完该流后,将流关闭
     */
    private void checkIOClose(Closeable closeable, int count) {
        mCompleteStream++;
        if (mCompleteStream % count == 0) {
            IOUtils.closeStream(closeable);
        }
    }

    public void setImageAssets(Context context, String assetsName) throws IOException {
        setImageInputStream(context.getAssets().open(assetsName));
    }

    public void setImageInputStream(InputStream pictureStream) {
        if (mWidth > 0) {
            dispose(pictureStream);
        } else {
            mPictureStream = pictureStream;
            mDelayedDispose = true;
        }
    }
}