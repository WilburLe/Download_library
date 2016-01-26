package com.idotools.http.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import com.idotools.db.DbUtils;
import com.idotools.db.converter.ColumnConverter;
import com.idotools.db.converter.ColumnConverterFactory;
import com.idotools.db.sqlite.ColumnDbType;
import com.idotools.db.sqlite.Selector;
import com.idotools.http.HttpHandler;
import com.idotools.http.HttpUtils;
import com.idotools.http.ResponseInfo;
import com.idotools.http.callback.RequestCallBack;
import com.idotools.http.exception.DbException;
import com.idotools.http.exception.HttpException;
import com.idotools.http.utils.LogUtils;
import com.idotools.http.utils.MD5;
import com.idotools.http.utils.PackageUtils;

/**
 * Author: wyouflf Date: 13-11-10 Time: 下午8:10
 */
public class DownloadManager {

    private List<DownloadInfo> downloadInfoList;

    private int maxDownloadThread = 1;

    private Context mContext;
    private DbUtils db;

    /* package */
    public DownloadManager(Context appContext) {
        LogUtils.allowD = true;
        ColumnConverterFactory.registerColumnConverter(HttpHandler.State.class, new HttpHandlerStateConverter());
        mContext = appContext;
        db = DbUtils.create(mContext);
        try {
            downloadInfoList = db.findAll(Selector.from(DownloadInfo.class));
        } catch (DbException e) {
            LogUtils.e(e.getMessage(), e);
        }
        if (downloadInfoList == null) {
            downloadInfoList = new ArrayList<DownloadInfo>();
        }
    }

    public int getDownloadInfoListCount() {
        return downloadInfoList.size();
    }

    public DownloadInfo getDownloadInfo(int index) {
        return downloadInfoList.get(index);
    }
    
    public DownloadInfo getDownloadInfo(String pkg,int versionCode){
        for(DownloadInfo info:downloadInfoList){
            if(pkg.equals(info.getPkg().equals(pkg))){
                return info;
            }
        }
        return null;
    }
    
    public static String getMD5ApkName(String pkg,int versionCode){
        return MD5.getMessageDigest(pkg.getBytes()) + versionCode + ".apk"; 
    }
    public static String getMD5ApkTmpName(String pkg,int versionCode){
        return MD5.getMessageDigest(pkg.getBytes()) + versionCode + ".tmp"; 
    }
    
    public boolean checkDownloaded(String pkg,int versionCode,String savePath){
        boolean flag = false;
        String path = savePath + getMD5ApkName(pkg, versionCode);
        if(new File(path).exists()){
            return true;
        }
        return flag;
    }
    
    public void downloadApkFile(String url,String fileName,String pkg,int versionCode,String savePath,boolean autoResume,final RequestCallBack<File> callback) throws DbException {
        if(checkDownloaded(pkg,versionCode,savePath)){
            PackageUtils.installApp(mContext, savePath);
            return;
        }
        String tmpApkPath = savePath + getMD5ApkTmpName(pkg, versionCode);
        String apkPath = savePath + getMD5ApkName(pkg, versionCode);
        final DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setDownloadUrl(url);
        downloadInfo.setAutoResume(autoResume);
        downloadInfo.setFileName(fileName);
        downloadInfo.setFileTmpPath(tmpApkPath);
        downloadInfo.setFileSavePath(apkPath);
        downloadInfo.setPkg(pkg);
        downloadInfo.setVersionCode(versionCode);
        downloadInfo.setAutoRenameTmp(true);
        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(maxDownloadThread);
        HttpHandler<File> handler = http.download(url, tmpApkPath, autoResume, new ManagerCallBack(downloadInfo, callback));
        downloadInfo.setHandler(handler);
        downloadInfo.setState(handler.getState());
        downloadInfoList.add(downloadInfo);
        db.saveBindingId(downloadInfo);
    }

    public boolean checkDownloadIng(DownloadInfo info){
        try {
            for(DownloadInfo vo:downloadInfoList){
                if(vo.pkg.equals(info.pkg) && vo.getVersionCode()==info.getVersionCode()){
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
    
    public void addNewDownload(String url, String fileName, String target, boolean autoResume, boolean autoRename, final RequestCallBack<File> callback) throws DbException {
        final DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setDownloadUrl(url);
        downloadInfo.setAutoResume(autoResume);
        downloadInfo.setFileName(fileName);
        downloadInfo.setFileSavePath(target);
        downloadInfo.setAutoRenameTmp(autoRename);
        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(maxDownloadThread);
        HttpHandler<File> handler = http.download(url, target, autoResume, autoRename, new ManagerCallBack(downloadInfo, callback));
        downloadInfo.setHandler(handler);
        downloadInfo.setState(handler.getState());
        downloadInfoList.add(downloadInfo);
        db.saveBindingId(downloadInfo);
    }

    public void resumeDownload(int index, final RequestCallBack<File> callback) throws DbException {
        final DownloadInfo downloadInfo = downloadInfoList.get(index);
        resumeDownload(downloadInfo, callback);
    }

    public void resumeDownload(DownloadInfo downloadInfo, final RequestCallBack<File> callback) throws DbException {
        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(maxDownloadThread);
        HttpHandler<File> handler = http.download(downloadInfo.getDownloadUrl(), downloadInfo.getFileSavePath(), downloadInfo.isAutoResume(), new ManagerCallBack(
                downloadInfo, callback));
        downloadInfo.setHandler(handler);
        downloadInfo.setState(handler.getState());
        db.saveOrUpdate(downloadInfo);
    }

    public void removeDownload(int index) throws DbException {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        removeDownload(downloadInfo);
    }

    public void removeDownload(DownloadInfo downloadInfo) throws DbException {
        HttpHandler<File> handler = downloadInfo.getHandler();
        if (handler != null && !handler.isCancelled()) {
            handler.cancel();
        }
        downloadInfoList.remove(downloadInfo);
        db.delete(downloadInfo);
    }

    public void stopDownload(int index) throws DbException {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        stopDownload(downloadInfo);
    }

    public void stopDownload(DownloadInfo downloadInfo) throws DbException {
        HttpHandler<File> handler = downloadInfo.getHandler();
        if (handler != null && !handler.isCancelled()) {
            handler.cancel();
        } else {
            downloadInfo.setState(HttpHandler.State.CANCELLED);
        }
        db.saveOrUpdate(downloadInfo);
    }

    public void stopAllDownload() throws DbException {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null && !handler.isCancelled()) {
                handler.cancel();
            } else {
                downloadInfo.setState(HttpHandler.State.CANCELLED);
            }
        }
        db.saveOrUpdateAll(downloadInfoList);
    }

    public void backupDownloadInfoList() throws DbException {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
        }
        db.saveOrUpdateAll(downloadInfoList);
    }

    public int getMaxDownloadThread() {
        return maxDownloadThread;
    }

    public void setMaxDownloadThread(int maxDownloadThread) {
        this.maxDownloadThread = maxDownloadThread;
    }

    public class ManagerCallBack extends RequestCallBack<File> {
        private DownloadInfo downloadInfo;
        private RequestCallBack<File> baseCallBack;

        public RequestCallBack<File> getBaseCallBack() {
            return baseCallBack;
        }

        public void setBaseCallBack(RequestCallBack<File> baseCallBack) {
            this.baseCallBack = baseCallBack;
        }

        private ManagerCallBack(DownloadInfo downloadInfo, RequestCallBack<File> baseCallBack) {
            this.baseCallBack = baseCallBack;
            this.downloadInfo = downloadInfo;
        }

        @Override
        public Object getUserTag() {
            if (baseCallBack == null)
                return null;
            return baseCallBack.getUserTag();
        }

        @Override
        public void setUserTag(Object userTag) {
            if (baseCallBack == null)
                return;
            baseCallBack.setUserTag(userTag);
        }

        @Override
        public void onStart() {
            LogUtils.d(downloadInfo.getPkg() + " onStart");
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            try {
                db.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onStart();
            }
        }

        @Override
        public void onCancelled() {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            try {
                db.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onCancelled();
            }
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            downloadInfo.setFileLength(total);
            downloadInfo.setProgress(current);
            try {
                db.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onLoading(total, current, isUploading);
            }
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            if(downloadInfo.getFileLength() != downloadInfo.getProgress()){     //不一致 返回
                return;
            }
            if(downloadInfo.getAutoRenameTmp()){                                //成功后改名
                File tarFile = new File(downloadInfo.getFileTmpPath());
                if(tarFile.exists()){
                    File newFile = new File(downloadInfo.getFileSavePath());
                    tarFile.renameTo(newFile);
                }
            }
            LogUtils.d(downloadInfo.getPkg() + " onSuccess");
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            try {
                db.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onSuccess(responseInfo);
            }
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            LogUtils.d(downloadInfo.getPkg() + " onFailure");
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            try {
                db.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onFailure(error, msg);
            }
        }
    }
    
    private class HttpHandlerStateConverter implements ColumnConverter<HttpHandler.State> {

        @Override
        public HttpHandler.State getFieldValue(Cursor cursor, int index) {
            return HttpHandler.State.valueOf(cursor.getInt(index));
        }

        @Override
        public HttpHandler.State getFieldValue(String fieldStringValue) {
            if (fieldStringValue == null)
                return null;
            return HttpHandler.State.valueOf(fieldStringValue);
        }

        @Override
        public Object fieldValue2ColumnValue(HttpHandler.State fieldValue) {
            return fieldValue.value();
        }

        @Override
        public ColumnDbType getColumnDbType() {
            return ColumnDbType.INTEGER;
        }
    }
}
