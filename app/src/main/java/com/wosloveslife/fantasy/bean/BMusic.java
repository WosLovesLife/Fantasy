package com.wosloveslife.fantasy.bean;

/**
 * Created by zhangh on 2017/1/2.
 */
public class BMusic {
    public long id;
    public String pinyinIndex;
    public String title;
    public String album;
    public String artist;
    public String path;
    public long duration;
    public long size;
    public boolean mIsOnline;

    public BMusic() {
    }

    public BMusic(long id, String pinyinIndex, String title, String album, String artist, String path, long duration, long size, boolean isOnline) {
        this.id = id;
        this.pinyinIndex = pinyinIndex;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.size = size;
        mIsOnline = isOnline;
    }

    //===========
    /** 0=idle;1=playing;2=pause */
//    public int playState;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BMusic bMusic = (BMusic) o;

        if (id != bMusic.id) return false;
        if (duration != bMusic.duration) return false;
        if (size != bMusic.size) return false;
        if (pinyinIndex != null ? !pinyinIndex.equals(bMusic.pinyinIndex) : bMusic.pinyinIndex != null)
            return false;
        if (title != null ? !title.equals(bMusic.title) : bMusic.title != null) return false;
        if (album != null ? !album.equals(bMusic.album) : bMusic.album != null) return false;
        if (artist != null ? !artist.equals(bMusic.artist) : bMusic.artist != null) return false;
        return path != null ? path.equals(bMusic.path) : bMusic.path == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (pinyinIndex != null ? pinyinIndex.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }
}
