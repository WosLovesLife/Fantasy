package com.wosloveslife.fantasy.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.wosloveslife.fantasy.bean.BMusic;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "MUSIC_ENTITY".
 */
public class MusicEntityDao extends AbstractDao<BMusic, Long> {

    public static final String TABLENAME = "MUSIC_ENTITY";

    /**
     * Properties of entity BMusic.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property _id = new Property(0, Long.class, "_id", true, "_ID");
        public final static Property Title = new Property(1, String.class, "title", false, "TITLE");
        public final static Property Artist = new Property(2, String.class, "artist", false, "ARTIST");
        public final static Property TitlePinyin = new Property(3, String.class, "titlePinyin", false, "TITLE_PINYIN");
        public final static Property ArtistPinyin = new Property(4, String.class, "artistPinyin", false, "ARTIST_PINYIN");
        public final static Property Album = new Property(5, String.class, "album", false, "ALBUM");
        public final static Property Path = new Property(6, String.class, "path", false, "PATH");
        public final static Property Duration = new Property(7, Long.class, "duration", false, "DURATION");
        public final static Property Size = new Property(8, Long.class, "size", false, "SIZE");
        public final static Property IsOnline = new Property(9, Boolean.class, "isOnline", false, "IS_ONLINE");
        public final static Property Genre = new Property(10, String.class, "genre", false, "GENRE");
        public final static Property Year = new Property(11, Integer.class, "year", false, "YEAR");
        public final static Property Track = new Property(12, Integer.class, "track", false, "TRACK");
        public final static Property DiscId = new Property(13, Integer.class, "discId", false, "DISC_ID");
        public final static Property IsMusic = new Property(14, Boolean.class, "isMusic", false, "IS_MUSIC");
        public final static Property IsAlarm = new Property(15, Boolean.class, "isAlarm", false, "IS_ALARM");
        public final static Property IsRingtone = new Property(16, Boolean.class, "isRingtone", false, "IS_RINGTONE");
        public final static Property IsPodcast = new Property(17, Boolean.class, "isPodcast", false, "IS_PODCAST");
        public final static Property IsNotification = new Property(18, Boolean.class, "isNotification", false, "IS_NOTIFICATION");
        public final static Property IsFavorite = new Property(19, Boolean.class, "isFavorite", false, "IS_FAVORITE");
        public final static Property BelongTo = new Property(20, String.class, "belongTo", false, "BELONG_TO");
    }

    public MusicEntityDao(DaoConfig config) {
        super(config);
    }

    public MusicEntityDao(DaoConfig config, MusicDaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"MUSIC_ENTITY\" (" + //
                "\"_ID\" INTEGER PRIMARY KEY ," + // 0: _id
                "\"TITLE\" TEXT," + // 1: title
                "\"ARTIST\" TEXT," + // 2: artist
                "\"TITLE_PINYIN\" TEXT," + // 3: titlePinyin
                "\"ARTIST_PINYIN\" TEXT," + // 4: artistPinyin
                "\"ALBUM\" TEXT," + // 5: album
                "\"PATH\" TEXT UNIQUE ," + // 6: path
                "\"DURATION\" INTEGER," + // 7: duration
                "\"SIZE\" INTEGER," + // 8: size
                "\"IS_ONLINE\" INTEGER," + // 9: isOnline
                "\"GENRE\" TEXT," + // 10: genre
                "\"YEAR\" INTEGER," + // 11: year
                "\"TRACK\" INTEGER," + // 12: track
                "\"DISC_ID\" INTEGER," + // 13: discId
                "\"IS_MUSIC\" INTEGER," + // 14: isMusic
                "\"IS_ALARM\" INTEGER," + // 15: isAlarm
                "\"IS_RINGTONE\" INTEGER," + // 16: isRingtone
                "\"IS_PODCAST\" INTEGER," + // 17: isPodcast
                "\"IS_NOTIFICATION\" INTEGER," + // 18: isNotification
                "\"IS_FAVORITE\" INTEGER," + // 19: isFavorite
                "\"BELONG_TO\" TEXT);"); // 20: belongTo
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MUSIC_ENTITY\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, BMusic entity) {
        stmt.clearBindings();

        Long _id = entity.get_id();
        if (_id != null) {
            stmt.bindLong(1, _id);
        }

        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }

        String artist = entity.getArtist();
        if (artist != null) {
            stmt.bindString(3, artist);
        }

        String titlePinyin = entity.getTitlePinyin();
        if (titlePinyin != null) {
            stmt.bindString(4, titlePinyin);
        }

        String artistPinyin = entity.getArtistPinyin();
        if (artistPinyin != null) {
            stmt.bindString(5, artistPinyin);
        }

        String album = entity.getAlbum();
        if (album != null) {
            stmt.bindString(6, album);
        }

        String path = entity.getPath();
        if (path != null) {
            stmt.bindString(7, path);
        }

        long duration = entity.getDuration();
        stmt.bindLong(8, duration);

        long size = entity.getSize();
        stmt.bindLong(9, size);

        boolean isOnline = entity.isOnline();
        stmt.bindLong(10, isOnline ? 1L : 0L);

        String genre = entity.getGenre();
        if (genre != null) {
            stmt.bindString(11, genre);
        }

        int year = entity.getYear();
        stmt.bindLong(12, year);

        int track = entity.getTrack();
        stmt.bindLong(13, track);

        int discId = entity.getDiscId();
        stmt.bindLong(14, discId);

        boolean isMusic = entity.isMusic();
        stmt.bindLong(15, isMusic ? 1L : 0L);

        boolean isAlarm = entity.isAlarm();
        stmt.bindLong(16, isAlarm ? 1L : 0L);

        boolean isRingtone = entity.isRingtone();
        stmt.bindLong(17, isRingtone ? 1L : 0L);

        boolean isPodcast = entity.isPodcast();
        stmt.bindLong(18, isPodcast ? 1L : 0L);

        boolean isNotification = entity.isNotification();
        stmt.bindLong(19, isNotification ? 1L : 0L);

        boolean isFavorite = entity.isFavorite();
        stmt.bindLong(20, isFavorite ? 1L : 0L);

        String belongTo = entity.getBelongTo();
        if (belongTo != null) {
            stmt.bindString(21, belongTo);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset) ? 0 : cursor.getLong(offset);
    }

    /** @inheritdoc */
    @Override
    public BMusic readEntity(Cursor cursor, int offset) {
        return new BMusic( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // _id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // title
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // artist
                cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // titlePinyin
                cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // artistPinyin
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // album
                cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // path
                cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // duration
                cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8), // size
                cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0, // isOnline
                cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // genre
                cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11), // year
                cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12), // track
                cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13), // discId
                cursor.isNull(offset + 14) ? null : cursor.getShort(offset + 14) != 0, // isMusic
                cursor.isNull(offset + 15) ? null : cursor.getShort(offset + 15) != 0, // isAlarm
                cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0, // isRingtone
                cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0, // isPodcast
                cursor.isNull(offset + 18) ? null : cursor.getShort(offset + 18) != 0, // isNotification
                cursor.isNull(offset + 19) ? null : cursor.getShort(offset + 19) != 0, // isFavorite
                cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20) // belongTo
        );
    }

    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, BMusic entity, int offset) {
        entity.set_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setArtist(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTitlePinyin(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setArtistPinyin(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setAlbum(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setPath(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setDuration(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setSize(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
        entity.setOnline(cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0);
        entity.setGenre(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setYear(cursor.isNull(offset + 11) ? null : cursor.getInt(offset + 11));
        entity.setTrack(cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12));
        entity.setDiscId(cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13));
        entity.setMusic(cursor.isNull(offset + 14) ? null : cursor.getShort(offset + 14) != 0);
        entity.setAlarm(cursor.isNull(offset + 15) ? null : cursor.getShort(offset + 15) != 0);
        entity.setRingtone(cursor.isNull(offset + 16) ? null : cursor.getShort(offset + 16) != 0);
        entity.setPodcast(cursor.isNull(offset + 17) ? null : cursor.getShort(offset + 17) != 0);
        entity.setNotification(cursor.isNull(offset + 18) ? null : cursor.getShort(offset + 18) != 0);
        entity.setFavorite(cursor.isNull(offset + 19) ? null : cursor.getShort(offset + 19) != 0);
        entity.setBelongTo(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
    }

    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(BMusic entity, long rowId) {
        entity.set_id(rowId);
        return rowId;
    }

    /** @inheritdoc */
    @Override
    public Long getKey(BMusic entity) {
        if (entity != null) {
            return entity.get_id();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }
}
