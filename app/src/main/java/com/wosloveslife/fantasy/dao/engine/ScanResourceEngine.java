package com.wosloveslife.fantasy.dao.engine;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.WorkerThread;

import com.github.promeg.pinyinhelper.Pinyin;
import com.orhanobut.logger.Logger;
import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.dao.SheetIds;
import com.wosloveslife.dao.store.SheetStore;
import com.wosloveslife.fantasy.manager.SettingConfig;
import com.yesing.blibrary_wos.utils.secure.MD5Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.RealmList;

/**
 * Created by zhangh on 2017/2/8.
 */

public class ScanResourceEngine {

    @WorkerThread
    public static List<Audio> getMusicFromSystemDao(final Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) return new ArrayList<>();

        try {
            if (cursor.moveToFirst()) {
                long minDuration = SettingConfig.getMinDuration() * 1000;

                Set<String> fileFilter = SettingConfig.getFilteredFolders().toBlocking().first();
                Map<String, Sheet> sheets = new HashMap<>();

                List<Sheet> first = SheetStore.loadByType(Sheet.TYPE_DIR).toBlocking().first();
                for (Sheet dir : first) {
                    sheets.put(dir.path, dir);
                }

                // TODO: 17/6/18 先从数据库获取歌单
                Sheet localSheet = SheetStore.loadById(SheetIds.LOCAL).toBlocking().first();
                if (localSheet == null) {
                    localSheet = new Sheet(SheetIds.LOCAL, "本地音乐", "Def", "bendiyinyue", "def", null, System.currentTimeMillis(), System.currentTimeMillis(), Sheet.TYPE_DEF, Sheet.STATE_NORMAL, null);
                    SheetStore.insertOrReplace(localSheet).toBlocking().first();
                }
                if (localSheet.songs == null) {
                    localSheet.songs = new RealmList<>();
                } else {
                    localSheet.songs.clear();
                }

                sheets.put(localSheet.id, localSheet);

                int offset = 0; // 这个是避免歌曲的加入时间相同
                do {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    /* 将所有包含音乐的文件夹都记录下来 */
                    path = path.substring(0, path.lastIndexOf("/"));

                    // 按照过滤配置过滤文件夹 这个判断要放在时间前面
                    if (fileFilter != null && fileFilter.contains(path)) {
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
//                    bMusic.songList = songLists;
                    //加入歌单的时间
                    bMusic.joinTimestamp = System.currentTimeMillis() + (++offset);

                    /* 将没被过滤掉的文件夹存起来稍候插入数据库 */
                    if (!sheets.containsKey(path)) {
                        Sheet sheet = new Sheet(MD5Utils.getMd5value(path), System.currentTimeMillis(), Sheet.TYPE_DIR, Sheet.STATE_NORMAL, path);
                        sheets.put(sheet.path, sheet);
                    }
                    Sheet sheet = sheets.get(path);
                    if (sheet.songs == null) {
                        sheet.songs = new RealmList<>();
                    }
                    sheet.songs.add(bMusic);

                    if (localSheet.songs == null) {
                        localSheet.songs = new RealmList<>();
                    }
                    localSheet.songs.add(bMusic);

//                    musicList.add(bMusic);
//                    sheet.songs.add(bMusic);
                } while (cursor.moveToNext());

                SheetStore.insertOrReplace(sheets.values()).toBlocking().first();

//                Observable.just(dirs)
//                        .map(new Func1<Set<String>, List<Sheet>>() {
//                            @Override
//                            public List<Sheet> call(Set<String> strings) {
//                                List<Sheet> sheets = new ArrayList<>();
//                                for (String string : strings) {
//                                    sheets.add(new Sheet(MD5Utils.getMd5value(string), System.currentTimeMillis(), Sheet.TYPE_DIR, Sheet.STATE_NORMAL, string));
//                                }
//                                return sheets;
//                            }
//                        })
//                        .flatMap(new Func1<List<Sheet>, Observable<?>>() {
//                            @Override
//                            public Observable<?> call(List<Sheet> sheets) {
//                                return SheetStore.insertOrReplace(sheets);
//                            }
//                        })
//                        .toBlocking()
//                        .first();

                return localSheet.songs;
            }
        } catch (Throwable e) {
            Logger.e(e, "从系统数据库读取音乐失败");
        } finally {
            cursor.close();
        }

        return new ArrayList<>();
    }
}
