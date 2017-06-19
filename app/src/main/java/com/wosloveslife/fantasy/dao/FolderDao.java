//package com.wosloveslife.fantasy.dao;
//
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteStatement;
//
//import com.wosloveslife.fantasy.dao.bean.BFolder;
//
//import de.greenrobot.dao.AbstractDao;
//import de.greenrobot.dao.Property;
//import de.greenrobot.dao.internal.DaoConfig;
//
//// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
///**
// * DAO for table "BFOLDER".
//*/
//public class FolderDao extends AbstractDao<BFolder, Long> {
//
//    public static final String TABLENAME = "BFOLDER";
//
//    /**
//     * Properties of entity BFolder.<br/>
//     * Can be used for QueryBuilder and for referencing column names.
//    */
//    public static class Properties {
//        public final static Property _id = new Property(0, Long.class, "_id", true, "_ID");
//        public final static Property FilePath = new Property(1, String.class, "filePath", false, "FILE_PATH");
//        public final static Property IsFiltered = new Property(2, Boolean.class, "isFiltered", false, "IS_FILTERED");
//    };
//
//
//    public FolderDao(DaoConfig config) {
//        super(config);
//    }
//
//    public FolderDao(DaoConfig config, DaoSession daoSession) {
//        super(config, daoSession);
//    }
//
//    /** Creates the underlying database table. */
//    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
//        String constraint = ifNotExists? "IF NOT EXISTS ": "";
//        db.execSQL("CREATE TABLE " + constraint + "\"BFOLDER\" (" + //
//                "\"_ID\" INTEGER PRIMARY KEY ," + // 0: _id
//                "\"FILE_PATH\" TEXT," + // 1: filePath
//                "\"IS_FILTERED\" INTEGER);"); // 2: isFiltered
//    }
//
//    /** Drops the underlying database table. */
//    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
//        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"BFOLDER\"";
//        db.execSQL(sql);
//    }
//
//    /** @inheritdoc */
//    @Override
//    protected void bindValues(SQLiteStatement stmt, BFolder entity) {
//        stmt.clearBindings();
//
//        Long _id = entity.get_id();
//        if (_id != null) {
//            stmt.bindLong(1, _id);
//        }
//
//        String filePath = entity.getFilePath();
//        if (filePath != null) {
//            stmt.bindString(2, filePath);
//        }
//
//        Boolean isFiltered = entity.getIsFiltered();
//        if (isFiltered != null) {
//            stmt.bindLong(3, isFiltered ? 1L: 0L);
//        }
//    }
//
//    /** @inheritdoc */
//    @Override
//    public Long readKey(Cursor cursor, int offset) {
//        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
//    }
//
//    /** @inheritdoc */
//    @Override
//    public BFolder readEntity(Cursor cursor, int offset) {
//        BFolder entity = new BFolder( //
//            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // _id
//            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // filePath
//            cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0 // isFiltered
//        );
//        return entity;
//    }
//
//    /** @inheritdoc */
//    @Override
//    public void readEntity(Cursor cursor, BFolder entity, int offset) {
//        entity.set_id(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
//        entity.setFilePath(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
//        entity.setIsFiltered(cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0);
//     }
//
//    /** @inheritdoc */
//    @Override
//    protected Long updateKeyAfterInsert(BFolder entity, long rowId) {
//        entity.set_id(rowId);
//        return rowId;
//    }
//
//    /** @inheritdoc */
//    @Override
//    public Long getKey(BFolder entity) {
//        if(entity != null) {
//            return entity.get_id();
//        } else {
//            return null;
//        }
//    }
//
//    /** @inheritdoc */
//    @Override
//    protected boolean isEntityUpdateable() {
//        return true;
//    }
//
//}
