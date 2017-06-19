//package com.wosloveslife.fantasy.dao;
//
//import android.database.Cursor;
//
//import com.orhanobut.logger.Logger;
//import com.wosloveslife.fantasy.dao.bean.BFolder;
//import com.yesing.blibrary_wos.utils.assist.WLogger;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///**
// * Created by zhangh on 2017/2/10.
// */
//
//public class FolderDbHelper{
//    private static final FolderDbHelper sFolderDbHelper = new FolderDbHelper();
//
//    FolderDao mDao;
//
//    private FolderDbHelper() {
//    }
//
//    public static FolderDbHelper getInstance() {
//        return sFolderDbHelper;
//    }
//
//    void initDb(FolderDao dao) {
//        mDao = dao;
//    }
//
//    private void insertOrReplace(BFolder entity) {
//        try {
//            mDao.insertOrReplace(entity);
//        } catch (Throwable e) {
//            Logger.e(e, "插入数据失败");
//        }
//    }
//
//    public void insertOrReplace(List<BFolder> entities) {
//        if (entities == null || entities.size() == 0) return;
//        try {
//            mDao.insertOrReplaceInTx(entities);
//        } catch (Throwable e) {
//            Logger.e(e, "一次性插入数据失败,尝试单独插入, 跳过错误的数据");
//            for (BFolder entity : entities) {
//                insertOrReplace(entity);
//            }
//        }
//    }
//
//    public List<BFolder> loadEntities() {
//        return mDao.loadAll();
//    }
//
//    public void clear() {
//        mDao.deleteAll();
//    }
//
//    public Set<String> getFilteredFolder() {
//        Set<String> fileFilter = new HashSet<>();
//        Cursor query = mDao.queryBuilder().where(FolderDao.Properties.IsFiltered.eq(true)).buildCursor().query();
//        if (query == null) return null;
//        try {
//            int ordinal = FolderDao.Properties.FilePath.ordinal;
//            if (query.moveToFirst()) {
//                do {
//                    fileFilter.add(query.getString(ordinal));
//                } while (query.moveToNext());
//            }
//        } catch (Throwable e) {
//            WLogger.w("call : 查询文件夹列表失败 ");
//        } finally {
//            query.close();
//        }
//        return fileFilter;
//    }
//}
