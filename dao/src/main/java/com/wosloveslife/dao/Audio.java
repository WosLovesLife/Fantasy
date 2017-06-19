package com.wosloveslife.dao;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by zhangh on 2017/6/14.
 */

public class Audio extends RealmObject implements AudioProperties {
    /** 用来判断一首歌的唯一值 */
    @NonNull
    public String id;

    /** 资源名(歌曲名) */
    @Nullable
    public String title;
    /** 艺术家(歌手) */
    @Nullable
    public String artist;
    /** 专辑名 */
    @Nullable
    public String album;
    /** 资源名的拼音形式, 用于排序 */
    @Nullable
    public String titlePinyin;
    /** 艺术家的拼音形式, 用于排序 */
    @Nullable
    public String artistPinyin;
    /** 专辑的拼音形式, 用于排序 */
    @Nullable
    public String albumPinyin;

    /** url路径,一般是本地文件路径, 如果是在线资源则对应网络url */
    public String path;
    /** 持续时间 */
    public long duration;
    /** 文件大小,如果是在线资源,该值需要根据Api返回的数据调整 */
    public long size;

    /** 年份 */
    public int year;
    /** 音轨 */
    public int track;
    /** 光盘编号, 系统数据库貌似没有这个字段 */
    public int discId;

    /** 是否是乐曲,对应系统的数据0为false */
    public boolean isMusic;
    /** 是否是铃声,对应系统的数据0为false */
    public boolean isRingtone;
    /** 是否是提示音,对应系统的数据0为false */
    public boolean isAlarm;
    /** 是否是通知音,对应系统的数据0为false */
    public boolean isNotification;
    /** 是否是播客电台,对应系统的数据0为false */
    public boolean isPodcast;
    /** 所属歌单 */
    @Nullable
    public RealmList<Sheet> songList;

    /**
     * 这首歌所在的音乐列表, 这个字段很关键, 它需要和歌单列表数据表所对应上.<br/>
     * 它使用一个字符串来存储一个数组,使用 "-" 短横杠来分割,通过数字来表示其所属组(这样能够节省开销)<br/>
     * 至于为什么不每个歌单存储自己的歌曲列表呢? 一个是这样存一个总表节省空间<br/>
     * 更重要的是,这样可以快捷的查询到一首歌曲所属的每一个歌单而不用遍历每一个歌单来确定某个歌单是否包含某首歌<br/>
     * 例如: 在删除歌曲时应该询问用户删除的范围, 是只从本地删除还是删除该歌在所有歌单的记录, 亦或是只删除其在某一个歌单的记录<br/>
     * 再例如: 在用户将一首歌添加到某一个歌单时弹出歌单列表,并标记出已包含该首歌曲的歌单<br/>
     * !!!最关键的!!!: 如何使歌单和歌曲对应上:<br/>
     * 每一个歌单都应该对应一个唯一的ID(以数字的16进制表示可以节省空间)(可以按照用户创建的顺序)<br/>
     * 预占用0-9的范围, 其中0是本地列表,1是收藏列表, 2是最近播放, 3是下载管理
     */
//    private String belongTo;
    public long joinTimestamp;

    public Audio() {
    }

    public Audio(Audio audio) {
        id = audio.id;
        title = audio.title;
        artist = audio.artist;
        album = audio.album;
        titlePinyin = audio.titlePinyin;
        artistPinyin = audio.artistPinyin;
        albumPinyin = audio.albumPinyin;
        path = audio.path;
        duration = audio.duration;
        size = audio.size;
        year = audio.year;
        track = audio.track;
        discId = audio.discId;
        isMusic = audio.isMusic;
        isRingtone = audio.isRingtone;
        isAlarm = audio.isAlarm;
        isNotification = audio.isNotification;
        isPodcast = audio.isPodcast;
        songList = audio.songList;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Audio audio = (Audio) object;

        return id != null ? id.equals(audio.id) : audio.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public boolean isOnline() {
        return path.startsWith("http");
    }

    @Deprecated // 未完成
    public boolean exist(){
        // TODO: 17/6/18 检查如果是本地文件查看文件是否存在,如果是网络文件查看是否存在本地缓存以及本地缓存是否完整.
        return false;
    }
}
