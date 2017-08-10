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
    private String id;

    /** 资源名(歌曲名) */
    @Nullable
    private String title;
    /** 艺术家(歌手) */
    @Nullable
    private String artist;
    /** 专辑名 */
    @Nullable
    private String album;
    /** 资源名的拼音形式, 用于排序 */
    @Nullable
    private String titlePinyin;
    /** 艺术家的拼音形式, 用于排序 */
    @Nullable
    private String artistPinyin;
    /** 专辑的拼音形式, 用于排序 */
    @Nullable
    private String albumPinyin;

    /** url路径,一般是本地文件路径, 如果是在线资源则对应网络url */
    private String path;
    /** 持续时间 */
    private long duration;
    /** 文件大小,如果是在线资源,该值需要根据Api返回的数据调整 */
    private long size;

    /** 年份 */
    private int year;
    /** 音轨 */
    private int track;
    /** 光盘编号, 系统数据库貌似没有这个字段 */
    private int discId;

    /** 是否是乐曲,对应系统的数据0为false */
    private boolean isMusic;
    /** 是否是铃声,对应系统的数据0为false */
    private boolean isRingtone;
    /** 是否是提示音,对应系统的数据0为false */
    private boolean isAlarm;
    /** 是否是通知音,对应系统的数据0为false */
    private boolean isNotification;
    /** 是否是播客电台,对应系统的数据0为false */
    private boolean isPodcast;
    /** 所属歌单 */
    @NonNull
    private RealmList<Sheet> songList;

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
    private long joinTimestamp;

    public Audio() {
        songList = new RealmList<>();
    }

    @Override
    public Audio clone() {
        Audio audio = new Audio();
        audio.id = id;
        audio.title = title;
        audio.artist = artist;
        audio.album = album;
        audio.titlePinyin = titlePinyin;
        audio.artistPinyin = artistPinyin;
        audio.albumPinyin = albumPinyin;
        audio.path = path;
        audio.duration = duration;
        audio.size = size;
        audio.year = year;
        audio.track = track;
        audio.discId = discId;
        audio.isMusic = isMusic;
        audio.isRingtone = isRingtone;
        audio.isAlarm = isAlarm;
        audio.isNotification = isNotification;
        audio.isPodcast = isPodcast;
        audio.songList = songList;
        return audio;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    public void setArtist(@Nullable String artist) {
        this.artist = artist;
    }

    @Nullable
    public String  getAlbum() {
        return album;
    }

    public void setAlbum(@Nullable String album) {
        this.album = album;
    }

    @Nullable
    public String getTitlePinyin() {
        return titlePinyin;
    }

    public void setTitlePinyin(@Nullable String titlePinyin) {
        this.titlePinyin = titlePinyin;
    }

    @Nullable
    public String getArtistPinyin() {
        return artistPinyin;
    }

    public void setArtistPinyin(@Nullable String artistPinyin) {
        this.artistPinyin = artistPinyin;
    }

    @Nullable
    public String getAlbumPinyin() {
        return albumPinyin;
    }

    public void setAlbumPinyin(@Nullable String albumPinyin) {
        this.albumPinyin = albumPinyin;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getDiscId() {
        return discId;
    }

    public void setDiscId(int discId) {
        this.discId = discId;
    }

    public boolean isMusic() {
        return isMusic;
    }

    public void setMusic(boolean music) {
        isMusic = music;
    }

    public boolean isRingtone() {
        return isRingtone;
    }

    public void setRingtone(boolean ringtone) {
        isRingtone = ringtone;
    }

    public boolean isAlarm() {
        return isAlarm;
    }

    public void setAlarm(boolean alarm) {
        isAlarm = alarm;
    }

    public boolean isNotification() {
        return isNotification;
    }

    public void setNotification(boolean notification) {
        isNotification = notification;
    }

    public boolean isPodcast() {
        return isPodcast;
    }

    public void setPodcast(boolean podcast) {
        isPodcast = podcast;
    }

    @NonNull
    public RealmList<Sheet> getSongList() {
        return songList;
    }

    public void setSongList(@NonNull RealmList<Sheet> songList) {
        this.songList = songList;
    }

    public long getJoinTimestamp() {
        return joinTimestamp;
    }

    public void setJoinTimestamp(long joinTimestamp) {
        this.joinTimestamp = joinTimestamp;
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
    public boolean exist() {
        // TODO: 17/6/18 检查如果是本地文件查看文件是否存在,如果是网络文件查看是否存在本地缓存以及本地缓存是否完整.
        return false;
    }
}
