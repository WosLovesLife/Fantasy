package com.wosloveslife.dao;

import io.realm.RealmObject;

/**
 * Created by zhangh on 2017/6/14.
 */

public class Audio extends RealmObject{
    public static final String ID = "id";

    public static final String TITLE = "title";
    public static final String ARTIST = "artist";
    public static final String ALBUM = "album";
    public static final String TITLE_PINYIN = "titlePinyin";
    public static final String ARTIST_PINYIN = "artistPinyin";
    public static final String ALBUM_PINYIN = "albumPinyin";

    public static final String PATH = "path";
    public static final String DURATION = "duration";
    public static final String SIZE = "size";
    /** 年份 int */
    public static final String YEAR = "year";
    /** 音轨 int */
    public static final String TRACK = "track";
    /** 光盘编号 int */
    public static final String DISC_ID = "discId";

    /** 音频文件是音乐 */
    public static final String IS_MUSIC = "isMusic";
    public static final String IS_ALARM = "isAlarm";
    public static final String IS_RINGTONE = "isRingtone";
    public static final String IS_PODCAST = "isPodcast";
    public static final String IS_NOTIFICATION = "isNotification";
    public static final String JOIN_TIMESTAMP = "joinTimestamp";

    /** 用来判断一首歌的唯一值 */
    public String id;

    /** 资源名(歌曲名) */
    public String title;
    /** 艺术家(歌手) */
    public String artist;
    /** 专辑名 */
    public String album;
    /** 资源名的拼音形式, 用于排序 */
    public String titlePinyin;
    /** 艺术家的拼音形式, 用于排序 */
    public String artistPinyin;
    /** 专辑的拼音形式, 用于排序 */
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
}
