package com.wosloveslife.fantasy.manager;

import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.dao.Sheet;
import com.wosloveslife.fantasy.helper.SPHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leonard on 17/6/18.
 */

public class MusicConfig {
    private static final String KEY_LAST_SHEET_ID = "fantasy.manager.MusicManager.KEY_LAST_SHEET";

    @NonNull
    public final List<Audio> mMusicList;
    @NonNull
    public final Map<String, Sheet> mSheets;
    @Nullable
    public Audio mCurrentMusic;
    @Nullable
    public String mCurrentSheetId;

    public MusicConfig() {
        mMusicList = new ArrayList<>();
        mSheets = new HashMap<>();

        mCurrentSheetId = getLastSheetId();
    }

    public int getMusicCount() {
        return mMusicList.size();
    }

    //=======================================获取音乐-start===========================================

    @AnyThread
    @Nullable
    public Audio getMusic(int position) {
        if (position >= 0 && position < mMusicList.size()) {
            return mMusicList.get(position);
        }
        return null;
    }

    @AnyThread
    @Nullable
    public Audio getMusicByTitlePinYin(String pinyin) {
        if (!TextUtils.isEmpty(pinyin)) {
            for (Audio audio : mMusicList) {
                if (TextUtils.equals(audio.titlePinyin, pinyin)) {
                    return audio;
                }
            }
        }
        return null;
    }

    public Audio getFirst() {
        return getMusic(0);
    }

    public Audio getLast() {
        return getMusic(getMusicCount() - 1);
    }

    @Nullable
    public Audio getNext(Audio audio) {
        if (audio == null) {
            return null;
        }
        return getMusic(mMusicList.indexOf(audio) + 1);
    }

    @Nullable
    public Audio getNext(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        for (int i = 0; i < mMusicList.size(); i++) {
            Audio audio = mMusicList.get(i);
            if (TextUtils.equals(audio.id, id)) {
                return getMusic(i + 1);
            }
        }
        return null;
    }

    @Nullable
    public Audio getPrevious(Audio audio) {
        if (audio == null) {
            return null;
        }
        return getMusic(mMusicList.indexOf(audio) - 1);
    }

    @Nullable
    public Audio getPrevious(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        for (int i = 0; i < mMusicList.size(); i++) {
            Audio audio = mMusicList.get(i);
            if (TextUtils.equals(audio.id, id)) {
                return getMusic(i - 1);
            }
        }
        return null;
    }

    public void saveLastSheetId(String sheetId) {
        mCurrentSheetId = sheetId;
        SPHelper.getInstance().save(KEY_LAST_SHEET_ID, sheetId);
    }

    @Nullable
    public String getLastSheetId() {
        if (!TextUtils.isEmpty(mCurrentSheetId)) {
            return mCurrentSheetId;
        }
        return SPHelper.getInstance().get(KEY_LAST_SHEET_ID, null);
    }
}
