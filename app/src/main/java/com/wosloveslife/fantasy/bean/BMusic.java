package com.wosloveslife.fantasy.bean;

/**
 * Created by zhangh on 2017/1/2.
 */
public class BMusic {
    public long id;
    public char pinyinIndex;
    public String title;
    public String album;
    public String artist;
    public String path;
    public long duration;
    public long size;

    //===========
    /** 0=idle;1=playing;2=pause */
    public int playState;
}
