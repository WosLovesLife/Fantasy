package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DbGenerate {

    public static void main(String[] args) {
        /*
        long id,
        String title,
        String artist,
        String titlePinyin,
        String artistPinyin,
        String album,
        String path,
        long duration,
        long size,
        boolean isOnline,
        String genre,
        int year,
        int track,
        int discId,
        boolean isMusic,
        boolean isRingtone,
        boolean isAlarm,
        boolean isNotification,
        boolean isPodcast
        */

        // 创建 歌曲总表
        Schema schema = new Schema(1, "com.wosloveslife.fantasy");
        Entity entity = schema.addEntity("MusicEntity");
        entity.addLongProperty("songId").primaryKey(); // 主ID 自增长
        entity.addStringProperty("title"); // 音乐名
        entity.addStringProperty("artist"); // 艺术家
        entity.addStringProperty("titlePinyin");
        entity.addStringProperty("artistPinyin");
        entity.addStringProperty("album");
        entity.addStringProperty("path");
        entity.addLongProperty("duration");
        entity.addLongProperty("size");
        entity.addBooleanProperty("isOnline");
        entity.addStringProperty("genre");
        entity.addIntProperty("year");
        entity.addIntProperty("track");
        entity.addIntProperty("discId");
        entity.addBooleanProperty("isMusic");
        entity.addBooleanProperty("isAlarm");
        entity.addBooleanProperty("isRingtone");
        entity.addBooleanProperty("isPodcast");
        entity.addBooleanProperty("isNotification");
        entity.addBooleanProperty("isFavorite");    // 是否是收藏的音乐
        // 这首歌所在的音乐列表, 这个字段很关键, 它需要和歌单列表数据表所对应上.
        // 它使用一个字符串来存储一个数组,使用 "-" 短横杠来分割,通过数字来表示其所属组(这样能够节省开销)
        // 至于为什么不每个歌单存储自己的歌曲列表呢? 一个是这样存一个总表节省空间
        // 更重要的是,这样可以快捷的查询到一首歌曲所属的每一个歌单而不用遍历每一个歌单来确定某个歌单是否包含某首歌
        // 例如: 在删除歌曲时应该询问用户删除的范围, 是只从本地删除还是删除该歌在所有歌单的记录, 亦或是只删除其在某一个歌单的记录
        // 再例如: 在用户将一首歌添加到某一个歌单时弹出歌单列表,并标记出已包含该首歌曲的歌单
        // !!!最关键的!!!: 如何使歌单和歌曲对应上:
        // 每一个歌单都应该对应一个唯一的ID(以数字的16进制表示可以节省空间)(可以按照用户创建的顺序)
        // 预占用0-9的范围, 其中0是本地列表,1是收藏列表, 2是最近播放, 3是下载管理
        entity.addStringProperty("belongTo").primaryKey();
        entity.addLongProperty("joinTimestamp");


        // 创建 歌曲总表
//        Schema schema = new Schema(1, "com.wosloveslife.fantasy.folder");
        Entity entity2 = schema.addEntity("BFolder");
        entity2.addLongProperty("_id").primaryKey(); // 主ID 自增长
        entity2.addStringProperty("filePath"); // 所在文件夹
        entity2.addBooleanProperty("isFiltered"); // 是否被过滤

        try {
            new DaoGenerator().generateAll(schema, "daogenerate/src/gen");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
