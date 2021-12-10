package com.intretech.app.umsdashboard_new.download.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.intretech.app.umsdashboard_new.UdbApplication;
import com.intretech.app.umsdashboard_new.bean.DaoMaster;
import com.intretech.app.umsdashboard_new.bean.DaoSession;
import com.intretech.app.umsdashboard_new.bean.DownloadInfo;
import com.intretech.app.umsdashboard_new.bean.DownloadInfoDao;

import org.greenrobot.greendao.query.QueryBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * 断点续传
 * 数据库工具类-geendao运用
 */
public class DbDownUtil {
    private static DbDownUtil db;
    private final static String dbName = "AppDb";
    public DaoMaster.DevOpenHelper openHelper;
    private final Context context = UdbApplication.getContext();


    private DbDownUtil() {
        openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
    }


    /**
     * 获取单例
     *
     * @return
     */
    public static DbDownUtil getInstance() {
        if (db == null) {
            synchronized (DbDownUtil.class) {
                if (db == null) {
                    db = new DbDownUtil();
                }
            }
        }
        return db;
    }


    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }


    public void insertOrReplace(DownloadInfo info) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownloadInfoDao downInfoDao = daoSession.getDownloadInfoDao();
        downInfoDao.insertOrReplace(info);
    }

    public void deleteDownInfo(DownloadInfo info) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownloadInfoDao downInfoDao = daoSession.getDownloadInfoDao();
        downInfoDao.delete(info);
    }

    @Nullable
    public DownloadInfo queryDownBy(String url) {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownloadInfoDao downInfoDao = daoSession.getDownloadInfoDao();
        QueryBuilder<DownloadInfo> qb = downInfoDao.queryBuilder();
        qb.where(DownloadInfoDao.Properties.Url.eq(url));
        List<DownloadInfo> list = qb.list();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public List<DownloadInfo> queryDownAll() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        DownloadInfoDao downInfoDao = daoSession.getDownloadInfoDao();
        QueryBuilder<DownloadInfo> qb = downInfoDao.queryBuilder();
        return qb.list();
    }
}