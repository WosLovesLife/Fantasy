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
    public long modifyTimestamp;

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
