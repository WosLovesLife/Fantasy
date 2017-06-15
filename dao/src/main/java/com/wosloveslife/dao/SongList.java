package com.wosloveslife.dao;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by zhangh on 2017/6/14.
 */

public class SongList extends RealmObject {
    public static final String ID = "id";

    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String TITLE_PINYIN = "titlePinyin";
    public static final String AUTHOR_PINYIN = "authorPinyin";
    public static final String SONGS = "songs";
    public static final String CREATE_TIMESTAMP = "createTimestamp";
    public static final String MODIFY_TIMESTAMP = "modifyTimestamp";

    /** 用来判断一首歌的唯一值 */
    public String id;

    /** 歌单名 */
    public String title;
    /** 歌单作者 */
    public String author;
    /** 歌单名的拼音形式, 用于排序 */
    public String titlePinyin;
    /** 作者的拼音形式, 用于排序 */
    public String authorPinyin;

    /** 歌曲列表 */
    public RealmList<Audio> songs;
    /** 创建日期 */
    public long createTimestamp;
    public long modifyTimestamp;
}
