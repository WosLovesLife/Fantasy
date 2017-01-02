package com.yesing.blibrary_wos.utils.viewUtils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by YesingBeijing on 2016/9/13.
 */
public class ImageViewUtils {
//    public static HashMap<String, ViewGroup.LayoutParams> sParamsHashMap = new HashMap<>();
//
//    public static void GlideLoadImage(ImageView view, String url, final int targetWidth) {
//        if (sParamsHashMap.get(url) != null) {
//            view.setLayoutParams(sParamsHashMap.get(url));
//        }
//
//        Glide.with(view.getContext())
//                .load(url)
//                .into(new ImageViewTarget<GlideDrawable>(view) {
//                    @Override
//                    protected void setResource(GlideDrawable resource) {
//                        ImageViewUtils.autoFit(view, targetWidth, resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
//                        view.setImageDrawable(resource);
//                    }
//                });
//    }

    public static void autoFit(View view, int targetWidth, int photoWidth, int photoHeight) {
        float ratio = (targetWidth + 0f) / photoWidth;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(targetWidth, (int) (photoHeight * ratio));
        } else {
            params.width = targetWidth;
            params.height = (int) (photoHeight * ratio);
        }
        view.setLayoutParams(params);
    }
}
