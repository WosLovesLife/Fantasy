package com.wosloveslife.fantasy.dao.engine;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.WorkerThread;

import com.github.promeg.pinyinhelper.Pinyin;
import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.fantasy.dao.bean.BFolder;
import com.wosloveslife.fantasy.manager.CustomConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.RealmList;

/**
 * Created by zhangh on 2017/2/8.
 */

public class ScanResourceEngine {

    @WorkerThread
    public static List<Audio> getMusicFromSystemDao(final Context context) {
        List<Audio> musicList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) return musicList;

        try {
            if (cursor.moveToFirst()) {
                long minDuration = CustomConfiguration.getMinDuration() * 1000;

                Set<String> fileFilter = null;
                List<BFolder> bFolders = CustomConfiguration.getFolders();
                if (bFolders != null) {
                    for (BFolder folder : bFolders) {
                        if (folder.isFiltered) {
                            if (fileFilter == null) {
                                fileFilter = new HashSet<>();
                            }
                            fileFilter.add(folder.filePath);
                        }
                    }
                }
                Set<BFolder> folders = new HashSet<>();

                // TODO: 17/6/18 先从数据库获取歌单
                RealmList<Sheet> songLists = new RealmList<>();
                Sheet songList = new Sheet();
                songLists.add(songList);
                int offset = 0;
                do {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String folder = path.substring(0, path.lastIndexOf("/"));
                            /* 将所有包含音乐的文件夹都记录下来 */
                    BFolder bFolder = new BFolder(null, folder, false);
                    folders.add(bFolder);

                    // 按照过滤配置过滤文件夹 这个判断要放在时间前面
                    if (fileFilter != null && fileFilter.contains(folder)) {
                        bFolder.isFiltered = true;
                        continue;
                    }

                    // 按照过滤配置过滤最小时间
                    long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    if (duration < minDuration) {
                        continue;
                    }

                    Audio bMusic = new Audio();

                    //歌曲ID
                    bMusic.id = String.valueOf(path.hashCode());
                    //歌曲标题
                    bMusic.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                    bMusic.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                    bMusic.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    //歌曲标题 拼音
                    bMusic.titlePinyin = Pinyin.toPinyin(bMusic.title, "");
                    //歌曲的歌手名 拼音
                    bMusic.artistPinyin = Pinyin.toPinyin(bMusic.artist, "");
                    //歌曲的专辑名 拼音
                    bMusic.albumPinyin = Pinyin.toPinyin(bMusic.album, "");

                    //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                    bMusic.duration = duration;
                    //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                    bMusic.path = path;
                    //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                    bMusic.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                    //=============以下字段都不重要.可有可无
                    //年份
                    bMusic.year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
                    //风格,字段被系统hide.可能根本不存在这东西
                    //bMusic.genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                    //轨道
                    bMusic.track = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
                    //是否是乐曲(歌曲)
                    bMusic.isMusic = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)) != 0;
                    //是否是铃声
                    bMusic.isRingtone = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE)) != 0;
                    //是否是提示音
                    bMusic.isAlarm = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_ALARM)) != 0;
                    //是否是通知音
                    bMusic.isNotification = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_NOTIFICATION)) != 0;
                    //是否是博客电台
                    bMusic.isPodcast = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)) != 0;

                    //加入的歌单, 这里都加入本地歌单
                    bMusic.songList = songLists;
                    //加入歌单的时间
                    bMusic.joinTimestamp = System.currentTimeMillis() + (++offset);

                    musicList.add(bMusic);
                } while (cursor.moveToNext());

                List<BFolder> folderList = new ArrayList<>();
                folderList.addAll(folders);
                CustomConfiguration.saveFolders(folderList);
            }
        } catch (Throwable e) {
            Logger.e(e, "从系统数据库读取音乐失败");
        } finally {
            cursor.close();
        }

        return musicList;
    }
}
