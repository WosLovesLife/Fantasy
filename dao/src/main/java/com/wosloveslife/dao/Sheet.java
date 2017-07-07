package com.wosloveslife.dao;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by zhangh on 2017/6/14.
 */

public class Sheet extends RealmObject implements SheetProperties {
    /** 用来判断一首歌的唯一值 */
    @NonNull
    public String id;

    /** 歌单名 */
    @Nullable
    public String title;
    /** 歌单作者 */
    @Nullable
    public String author;
    /** 歌单名的拼音形式, 用于排序 */
    @Nullable
    public String titlePinyin;
    /** 作者的拼音形式, 用于排序 */
    @Nullable
    public String authorPinyin;

    /** 歌曲列表 */
    @Nullable
    public RealmList<Audio> songs;
    /** 创建日期 */
    public long createTimestamp;
    /** 修改日期 */
    public long modifyTimestamp;

    /** 歌单类型(用户创建, 系统文件夹等) */
    @Type
    public int type;
    /** 文件状态(被过隐藏掉等) */
    @State
    public int state;
    /** 如果歌单对应一个真实的文件夹, 它的路径 */
    @Nullable
    public String path;

    public Sheet() {
    }

    public Sheet(@NonNull String id, @Nullable String title, @Nullable String author, @Nullable String titlePinyin,
                 @Nullable String authorPinyin, @Nullable RealmList<Audio> songs, long createTimestamp, long modifyTimestamp,
                 int type, int state, @Nullable String path) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.titlePinyin = titlePinyin;
        this.authorPinyin = authorPinyin;
        this.songs = songs;
        this.createTimestamp = createTimestamp;
        this.modifyTimestamp = modifyTimestamp;
        this.type = type;
        this.state = state;
        this.path = path;
    }

    public Sheet(@NonNull String id, long createTimestamp, @Type int type, @State int state, @Nullable String path) {
        this(id, null, null, null, null, null, createTimestamp, createTimestamp, type, state, path);
    }

    @Override
    public Sheet clone() {
        return new Sheet(id, title, author, titlePinyin, authorPinyin, songs, createTimestamp, modifyTimestamp, type, state, path);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Sheet sheet = (Sheet) object;

        return id.equals(sheet.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
