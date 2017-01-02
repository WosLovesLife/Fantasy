package com.yesing.blibrary_wos.fragment;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.yesing.blibrary_wos.R;
import com.yesing.blibrary_wos.utils.AlertUtils;
import com.yesing.blibrary_wos.utils.assist.Toaster;
import com.yesing.blibrary_wos.utils.assist.WLogger;
import com.yesing.blibrary_wos.utils.systemUtils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 查看大图的类
 * Created by zhangh on 2016/10/3.
 */

public class PhotoViewActivity extends AppCompatActivity {
    private static final String ARG_PHOTO_BASE_URL = "arg_photo_base_urls";
    private static final String ARG_PHOTO_URLS = "arg_photo_urls";
    private static final String ARG_PHOTO_POSITION = "arg_photo_position";

    //=========Views
    ViewPager mViewPager;
    TextView mTvSize;

    //=========变量
    int mCurrentPosition;

    //=========数据
    String mBaseUrl;
    private ObjectAnimator mAlphaAnim;

    public static void startActivity(Fragment fragment, String baseUrl, String photoUrl) {
        ArrayList<String> photoUrls = new ArrayList<>();
        photoUrls.add(photoUrl);
        startActivity(fragment, baseUrl, photoUrls, 0);
    }

    public static void startActivity(Fragment fragment, String baseUrl, ArrayList<String> photoUrls, int position) {
        fragment.startActivity(getStartIntent(fragment.getActivity(), baseUrl, photoUrls, position));
    }

    public static void startActivity(Context context, String baseUrl, String photoUrl) {
        ArrayList<String> photoUrls = new ArrayList<>();
        photoUrls.add(photoUrl);
        startActivity(context, baseUrl, photoUrls, 0);
    }

    public static void startActivity(Context context, String baseUrl, ArrayList<String> photoUrls, int position) {
        context.startActivity(getStartIntent(context, baseUrl, photoUrls, position));
    }

    public static Intent getStartIntent(Context context, String baseUrl, ArrayList<String> photoUrls, int position) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(ARG_PHOTO_BASE_URL, baseUrl);
        intent.putExtra(ARG_PHOTO_URLS, photoUrls);
        intent.putExtra(ARG_PHOTO_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_photo_view);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTvSize = (TextView) findViewById(R.id.tvSize);

        initView();
        updateData();
    }

    protected void initView() {
        initAnim();

        /* 切换效果 */
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mAlphaAnim.cancel();
                mTvSize.clearAnimation();
                mTvSize.setAlpha(1f);
            }

            @Override
            public void onPageSelected(int position) {
                mTvSize.setText((position + 1) + " / " + mViewPager.getAdapter().getCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    mAlphaAnim.start();
                }
            }
        });
    }

    protected void updateData() {
        Intent intent = getIntent();
        if (intent == null) {
            Toaster.showShort(this, "打开异常");
            finish();
            return;
        }

        ArrayList<String> arrayList = intent.getStringArrayListExtra(ARG_PHOTO_URLS);
        if (arrayList == null || arrayList.size() < 1) {
            Toaster.showShort(this, "打开异常");
            finish();
            return;
        }

        mBaseUrl = intent.getStringExtra(ARG_PHOTO_BASE_URL);
        mCurrentPosition = intent.getIntExtra(ARG_PHOTO_POSITION, 0);

        Adapter adapter = new Adapter(arrayList);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mCurrentPosition);

        int size = arrayList.size();
        if (size < 2) {
            mTvSize.setVisibility(View.GONE);
        } else {
            mTvSize.setVisibility(View.VISIBLE);
            mAlphaAnim.start();
        }

        mTvSize.setText((mCurrentPosition + 1) + " / " + size);
    }

    class Adapter extends PagerAdapter {
        ArrayList<String> mPhotoUrls = new ArrayList<>();

        public Adapter(ArrayList<String> photoUrls) {
            mPhotoUrls = photoUrls;
        }

        @Override
        public int getCount() {
            return mPhotoUrls.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(PhotoViewActivity.this).inflate(R.layout.item_photo_view, null);

            PhotoView photoView = (PhotoView) view.findViewById(R.id.photo_view);
            final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.pb_loading);
            final TextView tvHint = (TextView) view.findViewById(R.id.tv_hint);

            container.addView(view);

            String photoPath = mPhotoUrls.get(position);
            if (!TextUtils.isEmpty(photoPath)) {
                tvHint.setVisibility(View.GONE);
                try {
                    Glide.with(container.getContext())
                            .load(mBaseUrl + photoPath)
                            .crossFade()
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    // 隐藏进度框
                                    progressBar.setVisibility(View.GONE);
                                    String errorMsg = AlertUtils.getErrorMsg(e);
                                    tvHint.setText(TextUtils.isEmpty(errorMsg) ? "读取图片失败(图片地址为空)" : errorMsg);
                                    tvHint.setVisibility(View.VISIBLE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    // 隐藏进度框
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(photoView);
                } catch (Throwable e) {
                    WLogger.logE("Glide", e);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                tvHint.setText("读取图片失败(图片地址为空)");
                tvHint.setVisibility(View.VISIBLE);
            }
            /* 轻击退出 */
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    finish();
                }
            });
            /* 长按提示保存 */
            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mCurrentPosition = position;
                    new AlertDialog.Builder(PhotoViewActivity.this)
                            .setTitle(null)
                            .setMessage("要保存这张图片吗?")
                            .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    save(mBaseUrl + mPhotoUrls.get(mCurrentPosition));
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                    return true;
                }
            });

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private void initAnim() {
        mAlphaAnim = ObjectAnimator.ofFloat(mTvSize, "alpha", mTvSize.getAlpha(), 0f);
        mAlphaAnim.setDuration(1000);
        mAlphaAnim.setStartDelay(3000);
    }

    /**
     * 保存到相册
     */
    private void save(final String url) {
        String displayUrl = url;
        if (!url.startsWith("http")) {
            displayUrl = Uri.fromFile(new File(url)).toString();
        }

        try {
            // 保存到相册
            Glide.with(this)
                    .load(displayUrl)
                    .downloadOnly(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
//                            try {
//                                SystemDaoUtils.updateSystemAlbum(PhotoViewActivity.this, resource.getAbsolutePath(), resource.getName());
//                                Toaster.showShort(PhotoViewActivity.this, "成功保存到相册");
//                            } catch (FileNotFoundException e) {
//                                WLogger.logE("保存失败", e);
//                                if (!SDCardUtils.isSDCardEnable()) {
//                                    Toaster.showShort(PhotoViewActivity.this, "保存失败,存储设备不可用");
//                                }
//                                Toaster.showShort(PhotoViewActivity.this, "保存失败");
//                            }
                            save2Local(resource);
                        }
                    });
        } catch (Throwable e) {
            WLogger.logE("Glide发生错误", e);
        }
    }

    private void save2Local(final File resource) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                String msg = "保存失败";
                if (resource != null && resource.exists()) {
                    File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (filesDir == null || !filesDir.exists()) {
                        filesDir = getFilesDir();
                    }

                    if (!filesDir.exists()) {
                        filesDir.mkdirs();
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(resource.getAbsolutePath(), options);
                    String type = options.outMimeType;
                    String extension = type;
                    if (extension.contains("/")) {
                        extension = extension.substring(extension.indexOf("/") + 1);
                    }
                    String resourceName = resource.getName();
                    if (resourceName.length() > 16) {
                        resourceName = resourceName.substring(0, 16);
                    }
                    File picturePath = new File(filesDir, resourceName + "." + extension);

                    BufferedInputStream inputStream = null;
                    BufferedOutputStream outputStream = null;
                    try {
                        inputStream = new BufferedInputStream(new FileInputStream(resource));
                        outputStream = new BufferedOutputStream(new FileOutputStream(picturePath));
                        int len;
                        byte[] bytes = new byte[512 * 1024];
                        while ((len = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, len);
                            outputStream.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeStream(inputStream);
                        IOUtils.closeStream(outputStream);
                    }

                    if (picturePath.exists() && picturePath.length() > 0) {
//                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                        Uri uri = Uri.fromFile(picturePath);
//                        intent.setData(uri);
//                        sendBroadcast(intent);

                        // 其次把文件插入到系统图库
//                        try {
//                            MediaStore.Images.Media.insertImage(getContentResolver(), picturePath.getAbsolutePath(), picturePath.getName(), null);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }

                        // 最后通知图库更新
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(filesDir)));

                        msg = "成功保存到相册";
                    } else {
                        msg = "保存失败";
                    }
                }

                final String finalMsg = msg;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.showShort(PhotoViewActivity.this, finalMsg);
                    }
                });
            }
        }.start();
    }

    public static void addImageToGallery(final String filePath, String type, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, type);
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
