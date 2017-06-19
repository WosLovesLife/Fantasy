package com.wosloveslife.dao;

/**
 * Created by leonard on 17/6/17.
 */

public interface AudioProperties {
    String ID = "id";

    String TITLE = "title";
    String ARTIST = "artist";
    String ALBUM = "album";
    String TITLE_PINYIN = "titlePinyin";
    String ARTIST_PINYIN = "artistPinyin";
    String ALBUM_PINYIN = "albumPinyin";

    String PATH = "path";
    String DURATION = "duration";
    String SIZE = "size";
    /** 年份 int */
    String YEAR = "year";
    /** 音轨 int */
    String TRACK = "track";
    /** 光盘编号 int */
    String DISC_ID = "discId";

    /** 音频文件是音乐 */
    String IS_MUSIC = "isMusic";
    String IS_ALARM = "isAlarm";
    String IS_RINGTONE = "isRingtone";
    String IS_PODCAST = "isPodcast";
    String IS_NOTIFICATION = "isNotification";
    String SONG_LIST = "songList";
    String JOIN_TIMESTAMP = "joinTimestamp";
}
