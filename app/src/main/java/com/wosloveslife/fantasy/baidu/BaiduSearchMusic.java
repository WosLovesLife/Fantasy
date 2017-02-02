package com.wosloveslife.fantasy.baidu;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by zhangh on 2017/2/3.
 */

public class BaiduSearchMusic {
    public interface BaiduSearchMusicApi {
        @GET("v1/restserver/ting")
        Observable<BaiduSearchMusic> searchMusic(@Query("method") String method, @Query("query") String query);
    }

    /**
     * song : [{"bitrate_fee":"{\"0\":\"0|0\",\"1\":\"0|0\"}","weight":"80","songname":"海阔天空","songid":"73896409","has_mv":"0","yyr_artist":"1","artistname":"幼稚园杀手","resource_type_ext":"0","resource_provider":"1","control":"0000000000","encrypted_songid":""}]
     * error_code : 22000
     * order : song
     */
    private int error_code;
    private String order;
    private List<BaiduMusic> song;

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public List<BaiduMusic> getSong() {
        return song;
    }

    public void setSong(List<BaiduMusic> song) {
        this.song = song;
    }
}
