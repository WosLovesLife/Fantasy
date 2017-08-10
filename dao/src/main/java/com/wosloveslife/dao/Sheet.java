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
    private String id;

    /** 歌单名 */
    @Nullable
    private String title;
    /** 歌单作者 */
    @Nullable
    private String author;
    /** 歌单名的拼音形式, 用于排序 */
    @Nullable
    private String titlePinyin;
    /** 作者的拼音形式, 用于排序 */
    @Nullable
    private String authorPinyin;

    /** 歌曲列表 */
    @NonNull
    private RealmList<Audio> songs;

    /** 创建日期 */
    private long createTimestamp;
    /** 修改日期 */
    private long modifyTimestamp;
    /** 歌单类型(用户创建, 系统文件夹等) */
    @Type
    private int type;
    /** 文件状态(被过隐藏掉等) */
    @State
    private int state;
    /** 如果歌单对应一个真实的文件夹, 它的路径 */
    @Nullable
    private String path;

    public Sheet() {
        songs = new RealmList<>();
    }

    public Sheet(@NonNull String id, @Nullable String title, @Nullable String author, @Nullable String titlePinyin,
                 @Nullable String authorPinyin, @Nullable RealmList<Audio> songs, long createTimestamp, long modifyTimestamp,
                 int type, int state, @Nullable String path) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.titlePinyin = titlePinyin;
        this.authorPinyin = authorPinyin;
        if (songs != null) {
            this.songs = songs;
        } else {
            this.songs = new RealmList<>();
        }
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
    public String getAuthor() {
        return author;
    }

    public void setAuthor(@Nullable String author) {
        this.author = author;
    }

    @Nullable
    public String getTitlePinyin() {
        return titlePinyin;
    }

    public void setTitlePinyin(@Nullable String titlePinyin) {
        this.titlePinyin = titlePinyin;
    }

    @Nullable
    public String getAuthorPinyin() {
        return authorPinyin;
    }

    public void setAuthorPinyin(@Nullable String authorPinyin) {
        this.authorPinyin = authorPinyin;
    }

    @NonNull
    public RealmList<Audio> getSongs() {
        return songs;
    }

    public void setSongs(@NonNull RealmList<Audio> songs) {
        this.songs = songs;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public long getModifyTimestamp() {
        return modifyTimestamp;
    }

    public void setModifyTimestamp(long modifyTimestamp) {
        this.modifyTimestamp = modifyTimestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(@Nullable String path) {
        this.path = path;
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
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Sheet{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", titlePinyin='" + titlePinyin + '\'' +
                ", authorPinyin='" + authorPinyin + '\'' +
                ", songs=" + songs +
                ", createTimestamp=" + createTimestamp +
                ", modifyTimestamp=" + modifyTimestamp +
                ", type=" + type +
                ", state=" + state +
                ", path='" + path + '\'' +
                '}';
    }
}
