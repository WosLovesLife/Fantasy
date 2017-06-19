package com.wosloveslife.fantasy.dao.bean.xiami;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.wosloveslife.fantasy.baidu.BaiduSearch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by zhangh on 2017/2/14.
 */
@Deprecated
public class XiamiMusic {
    /**
     * alibaba_xiami_api_search_summary_get_response : {"data":{}}
     */

    public interface SearchApi {
        @GET("router/rest")
        Call<BaiduSearch> searchMusic(@Query("method") String method,
                                      @Query("app_key") String app_key,
                                      @Query("sign_method") String sign_method,
                                      @Query("v") String v,
                                      @Query("timestamp") String timestamp,
                                      @Query("partner_id") String partner_id,
                                      @Query("format") String format,
                                      @Query("force_sensitive_param_fuzzy") String force_sensitive_param_fuzzy,
                                      @Query("limit") @Nullable String limit,
                                      @Query("page") @Nullable String page,
                                      @Query("key") @NonNull String key,
                                      @Query("sign") String sign);
    }
}
