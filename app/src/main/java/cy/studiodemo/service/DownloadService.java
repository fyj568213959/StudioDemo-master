package cy.studiodemo.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cy.studiodemo.bean.DownloadAppInfo;
import cy.studiodemo.engine.GlobalParams;
import cy.studiodemo.engine.XUtilsManager;
import cy.studiodemo.util.Utils;


/**
 * IntentService 总结:
 * 一启动方式
 * 1:Bind方式 如果是Activity用Bind启动IntentService,Activity挂掉，IntentService也会挂掉.IntentService会依次执行-->.构造函数,onCreate,onBind
 * 2:start方式 如果是start启动IntentService,IntentService会依次执行-->构造函数,onCreate,onStartCommand,onHandleIntent(新线程),onDestroy
 * (注意：执行完onHandleIntent()方法会自动Destroy)
 * 3.先Bind，后start的方式,执行完onHandleIntent()方法不会自动Destroy，当Activity调用unbindService()方法。IntentService会触发onUnbind方法。并且判断onHandleIntent方法是否执行完毕
 * 如果onHandleIntent方法执行完毕会直接调用onDestroy,没有执行完毕，会等着onHandleIntent()执行完毕之后再onDestroy.
 * 如果没有手动调用unbindService()，当activity,Destroy的时候系统会调用unbindService().
 */
public class DownloadService extends IntentService {

    private HttpUtils mHttpUtils;
    private DownLoadListener mDownLoadListener;
    private HashMap<String, HttpHandler<File>> mHttpHandlers = new HashMap<String, HttpHandler<File>>();
    private HashMap<String, DownloadAppInfo> mDownloadAppInfos = new HashMap<String, DownloadAppInfo>();
    public static final String ACTION_DOWNLOAD_CHECK_ACTIVITY = "action.download.check.activity"; // Activity检查当前是否有任务正在下载
    public static final String ACTION_DOWNLOAD_CHECK_FRAGMENT = "action.download.check.fragment"; // fragment检查当前是否有任务正在下载
    public static final String ACTION_DOWNLOAD_START = "action.download.start"; // 开始下载
    public static final String ACTION_DOWNLOAD_PAUSE = "action.download.pause"; // 暂停下载
    public static final String ACTION_DOWNLOAD_CANCEL_WAIT = "action.download.cancel.wait"; // 取消等待
    public static final String ACTION_DOWNLOAD_STOP = "action.download.stop"; // 停止下载

    public interface DownLoadListener {

        public abstract void onDownLoadWait(DownloadAppInfo downloadAppInfo);

        public abstract void onDownLoadStart(DownloadAppInfo downloadAppInfo);

        public abstract void onDownloading(DownloadAppInfo downloadAppInfo);

        public abstract void onDownLoadSuccess(DownloadAppInfo downloadAppInfo);

        public abstract void onDownLoadError(DownloadAppInfo downloadAppInfo);

        public abstract void onDownLoadCancell(DownloadAppInfo downloadAppInfo);

        public abstract void onDownLoadWaitCancell(DownloadAppInfo downloadAppInfo);

        public abstract void onAppInstall(DownloadAppInfo downloadAppInfo);

        public abstract void stopDownloadService();

    }

    public DownloadService() {
        super("DownloadService");
        Log.i("abc", "Service");
    }

    @Override
    public void onCreate() {
        Log.i("abc", "Service---onCreate");
        super.onCreate();
        XUtilsManager xUtilsManager = XUtilsManager.getInstance(getApplicationContext());
        mHttpUtils = xUtilsManager.getHttpUtils();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("abc", "Service---onBind");
        return new DownloadServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("abc", "Service---onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i("abc", "Service---onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("abc", "Service---onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.i("abc", "Service---onHandleIntent--action-->" + action);
        DownloadAppInfo downloadAppInfo = (DownloadAppInfo) intent.getSerializableExtra(GlobalParams.DOWNLOAD_APP_KEY);
        if (downloadAppInfo != null) {
            mDownloadAppInfos.put(downloadAppInfo.getUrl(), downloadAppInfo);
        }
        if (ACTION_DOWNLOAD_START.equals(action)) {
            startDownloadApp(downloadAppInfo);
        } else if (ACTION_DOWNLOAD_PAUSE.equals(action)) {
            pauseDownloadApp(downloadAppInfo);
        } else if (ACTION_DOWNLOAD_CANCEL_WAIT.equals(action)) {
            cancelWaitDownloadApp(downloadAppInfo);
        } else if (ACTION_DOWNLOAD_CHECK_ACTIVITY.equals(action)) {
            callBackIsAppDownloading(true);
        } else if (ACTION_DOWNLOAD_CHECK_FRAGMENT.equals(action)) {
            callBackIsAppDownloading(false);
        } else if (ACTION_DOWNLOAD_STOP.equals(action)) {
            stopAllDownloadApp();
        }
    }

    private void startDownloadApp(final DownloadAppInfo downloadAppInfo) {
        String url = downloadAppInfo.getUrl();
        String path = Utils.getCachePath(getApplicationContext()) + downloadAppInfo.getName() + ".apk";
        downloadAppInfo.setPath(path);
        HttpHandler<File> httpHandler = mHttpUtils.download(url, path, true, true, new RequestCallBack<File>() {

            @Override
            public void onStart() {
                downloadAppInfo.setStatus(DownloadAppInfo.STATUS_START);
                if (mDownLoadListener != null) {
                    mDownLoadListener.onDownLoadStart(downloadAppInfo);
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                downloadAppInfo.setStatus(DownloadAppInfo.STATUS_DOWNLOADING);
                downloadAppInfo.setMax((int) total);
                downloadAppInfo.setProgress((int) current);
                if (mDownLoadListener != null) {
                    mDownLoadListener.onDownloading(downloadAppInfo);
                }
            }

            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                downloadAppInfo.setStatus(DownloadAppInfo.STATUS_COMPLETE);
                if (mDownLoadListener != null) {
                    mDownLoadListener.onDownLoadSuccess(downloadAppInfo);
                }
                replaceDownloadAppInfos();
                installApp(downloadAppInfo);
                if (!checkIsAppDownloading()) {
                    if (mDownLoadListener != null) {
                        mDownLoadListener.stopDownloadService();
                    }
                }
            }

            @Override
            public void onCancelled() {
                String cancelledUrl = downloadAppInfo.getUrl();
                Log.i("abc", "cancelledUrl--->" + cancelledUrl);
                downloadAppInfo.setStatus(DownloadAppInfo.STATUS_PAUSE);
                if (mDownLoadListener != null) {
                    mDownLoadListener.onDownLoadCancell(downloadAppInfo);
                }
                replaceDownloadAppInfos();
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                if (checkAppISOK(downloadAppInfo)) {
                    if (Utils.isAppInstalled(getApplicationContext(), downloadAppInfo.getPackageName())) {
                        if (mDownLoadListener != null) {
                            mDownLoadListener.onDownLoadSuccess(downloadAppInfo);
                        }
                        downloadAppInfo.setStatus(DownloadAppInfo.STATUS_INSTALLED);
                    } else {
                        if (mDownLoadListener != null) {
                            mDownLoadListener.onDownLoadSuccess(downloadAppInfo);
                        }
                        downloadAppInfo.setStatus(DownloadAppInfo.STATUS_COMPLETE);
                    }
                } else {
                    if (mDownLoadListener != null) {
                        mDownLoadListener.onDownLoadError(downloadAppInfo);
                    }
                    downloadAppInfo.setStatus(DownloadAppInfo.STATUS_DOWNLOAD_ERROR);
                }
                replaceDownloadAppInfos();
            }
        });
        saveHttpHandler(url, httpHandler);
        downloadAppInfo.setStatus(DownloadAppInfo.STATUS_WAIT);
        if (mDownLoadListener != null) {
            mDownLoadListener.onDownLoadWait(downloadAppInfo);
        }
    }

    /**
     * 检查文件是否已经下载
     */
    private boolean checkAppISOK(DownloadAppInfo downloadAppInfo) {
        String path = downloadAppInfo.getPath();
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            try {
                int available = fis.available();
                int max = downloadAppInfo.getMax();
                return available == max;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }
        return false;
    }

    private void installApp(DownloadAppInfo downloadAppInfo) {
        String path = downloadAppInfo.getPath();
        Utils.installApp(getApplicationContext(), new File(path));
    }

    private void saveHttpHandler(String url, HttpHandler<File> httpHandler) {
        mHttpHandlers.put(url, httpHandler);
    }

    private void pauseDownloadApp(DownloadAppInfo downloadAppInfo) {
        HttpHandler<File> fileHttpHandler = mHttpHandlers.get(downloadAppInfo.getUrl());
        if (fileHttpHandler != null && !fileHttpHandler.isCancelled()) {
            fileHttpHandler.cancel();
        }
    }

    private void cancelWaitDownloadApp(DownloadAppInfo downloadAppInfo) {
        Iterator<Map.Entry<String, DownloadAppInfo>> downloadAppInfoIterator = mDownloadAppInfos.entrySet().iterator();
        while (downloadAppInfoIterator.hasNext()) {
            Map.Entry<String, DownloadAppInfo> entry = downloadAppInfoIterator.next();
            if (downloadAppInfo.getUrl().equals(entry.getKey())) {
                DownloadAppInfo entryValue = entry.getValue();
                entryValue.setStatus(DownloadAppInfo.STATUS_PAUSE);
            }
        }
        Iterator<Map.Entry<String, HttpHandler<File>>> httpHandlerIterator = mHttpHandlers.entrySet().iterator();
        while (httpHandlerIterator.hasNext()) {
            Map.Entry<String, HttpHandler<File>> entry = httpHandlerIterator.next();
            if (downloadAppInfo.getUrl().equals(entry.getKey())) {
                HttpHandler<File> httpHandler = entry.getValue();
                httpHandler.cancel();
                httpHandlerIterator.remove();
            }
        }
    }


    /**
     * 从数据库查找
     */
    public ArrayList<DownloadAppInfo> findDownloadAppInfos() {
        DbUtils db = DbUtils.create(getApplicationContext());
        ArrayList<DownloadAppInfo> downloadAppInfos = null;
        try {
            downloadAppInfos = (ArrayList<DownloadAppInfo>) db.findAll(DownloadAppInfo.class);//通过类型查找
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return downloadAppInfos;
    }

    /**
     * 替换
     */
    public void replaceDownloadAppInfos() {
        Iterator<Map.Entry<String, DownloadAppInfo>> iterator = mDownloadAppInfos.entrySet().iterator();
        ArrayList<DownloadAppInfo> downloadAppInfos = new ArrayList<DownloadAppInfo>();
        while (iterator.hasNext()) {
            Map.Entry<String, DownloadAppInfo> entry = iterator.next();
            DownloadAppInfo value = entry.getValue();
            downloadAppInfos.add(value);
        }
        DbUtils db = DbUtils.create(getApplicationContext());
        try {
            db.deleteAll(downloadAppInfos);
            db.saveAll(downloadAppInfos);
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void stopAllDownloadApp() {
        Iterator<Map.Entry<String, HttpHandler<File>>> iterator = mHttpHandlers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, HttpHandler<File>> entry = iterator.next();
            HttpHandler<File> httpHandler = entry.getValue();
            if (httpHandler != null && !httpHandler.isCancelled()) {
                httpHandler.cancel();
            }
        }
        replaceDownloadAppInfos();
        Intent intent = new Intent();
        intent.setAction(GlobalParams.DOWNLOAD_APP_STOP_ACTION);
        sendBroadcast(intent);

        if (mDownLoadListener != null) {
            mDownLoadListener.stopDownloadService();
        }
    }


    private boolean checkIsAppDownloading() {
        boolean isAppDownloading = false;
        Iterator<Map.Entry<String, DownloadAppInfo>> iterator = mDownloadAppInfos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DownloadAppInfo> entry = iterator.next();
            DownloadAppInfo downloadAppInfo = entry.getValue();
            if (downloadAppInfo != null) {
                if (DownloadAppInfo.STATUS_DOWNLOADING == downloadAppInfo.getStatus()) {
                    isAppDownloading = true;
                }
            }
        }
        return isAppDownloading;
    }


    private void callBackIsAppDownloading(boolean isActivity) {
        boolean isAppDownloading = checkIsAppDownloading();
        if (!isAppDownloading) {
            if (mDownLoadListener != null) {
                mDownLoadListener.stopDownloadService();
            }
        }
        if (isActivity) {
            Intent intent = new Intent();
            intent.setAction(GlobalParams.DOWNLOAD_APP_CHECK_ACTION);
            intent.putExtra(GlobalParams.DOWNLOAD_APP_CHECK_KEY, isAppDownloading);
            sendBroadcast(intent);
        }
    }

    public class DownloadServiceBinder extends Binder {
        public DownloadService getDownloadService() {
            return DownloadService.this;
        }
    }

    public void setMyDownLoadListener(DownLoadListener downLoadListener) {
        this.mDownLoadListener = downLoadListener;
    }
}
