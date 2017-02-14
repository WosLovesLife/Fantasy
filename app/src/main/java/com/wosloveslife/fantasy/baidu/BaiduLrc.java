package com.wosloveslife.fantasy.baidu;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by zhangh on 2017/2/3.
 */

public class BaiduLrc {
    public interface BaiduLrcApi {
        @GET("v1/restserver/ting")
        Observable<BaiduLrc> searchLrc(@Query("method") String method, @Query("songid") String query);
        @GET("v1/restserver/ting")
        Call<BaiduLrc> callSearchLrc(@Query("method") String method, @Query("songid") String query);
    }

    private String title;
    private String lrcContent;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLrcContent() {
        return lrcContent;
    }

    public void setLrcContent(String lrcContent) {
        this.lrcContent = lrcContent;
    }
}
