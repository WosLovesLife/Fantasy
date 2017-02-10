package com.wosloveslife.fantasy.bean;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhangh on 2017/1/2.
 */
public class BMusic {
    /** 主键id */
    public Long _id;
    /** 资源名(歌曲名) */
    public String title;
    /** 艺术家(歌手) */
    public String artist;
    /** 专辑名 */
    public String album;
    /** url路径,一般是本地文件路径, 如果是在线资源则对应网络url */
    public String path;
    /** 持续时间 */
    public long duration;
    /** 文件大小,如果是在线资源,该值需要根据Api返回的数据调整 */
    public long size;

    /** 风格,被系统数据库字段隐藏了,只用来作为用户输入的项. */
    public String genre;
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

    /** 资源名的拼音形式, 用于以歌曲名排序 */
    public String titlePinyin;
    /** 艺术家的拼音形式, 用于以歌手排序 */
    public String artistPinyin;
    /** 是否是在线资源 */
    public boolean mIsOnline;
    /** 是否是我的喜爱(belongTo中也会包含该字段) */
    public boolean isFavorite;
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
    public String belongTo;

    public BMusic() {
    }

    public BMusic(String title, String artist, String album, String path, long size) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.size = size;
    }

    public BMusic(Long _id, String title, String artist, String album, String path, long duration,
                  long size, String titlePinyin, String artistPinyin, boolean isOnline) {
        this._id = _id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
        this.size = size;
        this.titlePinyin = titlePinyin;
        this.artistPinyin = artistPinyin;
        mIsOnline = isOnline;
    }

    public BMusic(Long _id, String title, String artist, String titlePinyin, String artistPinyin, String album,
                  String path, long duration, long size, boolean isOnline,
                  String genre, int year, int track, int discId, boolean isMusic,
                  boolean isRingtone, boolean isAlarm, boolean isNotification, boolean isPodcast,
                  boolean isFavorite, String belongTo) {
        this._id = _id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
        this.size = size;
        this.genre = genre;
        this.year = year;
        this.track = track;
        this.discId = discId;
        this.isMusic = isMusic;
        this.isRingtone = isRingtone;
        this.isAlarm = isAlarm;
        this.isNotification = isNotification;
        this.isPodcast = isPodcast;
        this.titlePinyin = titlePinyin;
        this.artistPinyin = artistPinyin;
        mIsOnline = isOnline;
        this.isFavorite = isFavorite;
        this.belongTo = belongTo;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public void setDiscId(int discId) {
        this.discId = discId;
    }

    public void setMusic(boolean music) {
        isMusic = music;
    }

    public void setRingtone(boolean ringtone) {
        isRingtone = ringtone;
    }

    public void setAlarm(boolean alarm) {
        isAlarm = alarm;
    }

    public void setNotification(boolean notification) {
        isNotification = notification;
    }

    public void setPodcast(boolean podcast) {
        isPodcast = podcast;
    }

    public void setTitlePinyin(String titlePinyin) {
        this.titlePinyin = titlePinyin;
    }

    public void setArtistPinyin(String artistPinyin) {
        this.artistPinyin = artistPinyin;
    }

    public void setOnline(boolean online) {
        mIsOnline = online;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public void setBelongToSet(Set<String> belongTo) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : belongTo) {
            stringBuilder.append(s).append("-");
        }
        this.belongTo = stringBuilder.toString();
    }

    public Long get_id() {
        return _id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    public int getTrack() {
        return track;
    }

    public int getDiscId() {
        return discId;
    }

    public boolean isMusic() {
        return isMusic;
    }

    public boolean isRingtone() {
        return isRingtone;
    }

    public boolean isAlarm() {
        return isAlarm;
    }

    public boolean isNotification() {
        return isNotification;
    }

    public boolean isPodcast() {
        return isPodcast;
    }

    public String getTitlePinyin() {
        return titlePinyin;
    }

    public String getArtistPinyin() {
        return artistPinyin;
    }

    public boolean isOnline() {
        return mIsOnline;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public String getBelongTo() {
        return belongTo;
    }

    @NonNull
    public Set<String> getBelongToSet() {
        Set<String> strings = new HashSet<>();
        if (!TextUtils.isEmpty(belongTo)) {
            String[] split = belongTo.split("-");
            List<String> list = Arrays.asList(split);
            strings.addAll(list);
        }
        return strings;
    }

    //===========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BMusic bMusic = (BMusic) o;

        return mIsOnline == bMusic.mIsOnline && (title != null ? title.equals(bMusic.title)
                : bMusic.title == null && (artist != null ? artist.equals(bMusic.artist)
                : bMusic.artist == null && (path != null ? path.equals(bMusic.path)
                : bMusic.path == null)));
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (mIsOnline ? 1 : 0);
        return result;
    }
}
