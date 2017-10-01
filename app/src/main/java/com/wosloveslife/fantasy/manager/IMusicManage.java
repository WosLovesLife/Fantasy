package com.wosloveslife.fantasy.manager;

import com.wosloveslife.dao.Audio;
import com.wosloveslife.fantasy.album.AlbumFile;
import com.wosloveslife.fantasy.lrc.BLyric;

import java.util.List;

/**
 * Created by zhangh on 2017/9/24.
 */

public interface IMusicManage {
    List<Audio> getAudios();

    boolean isFavorite(String audioId);

    AlbumFile getAlbum(String audioId);

    BLyric getLyric(String audioId);
}
