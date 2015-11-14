package cy.studiodemo.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cy.studiodemo.R;
import cy.studiodemo.activity.MainActivity;
import cy.studiodemo.adapter.DownloadListAdapter;
import cy.studiodemo.base.BaseFragment;
import cy.studiodemo.bean.DownloadAppInfo;
import cy.studiodemo.engine.GlobalParams;
import cy.studiodemo.service.DownloadService;
import cy.studiodemo.util.Utils;

/**
 * Created by cuiyue on 15/7/27.
 */
public class DownloadAppFragment extends BaseFragment {

    private ArrayList<DownloadAppInfo> mDownLoadAppInfos;
    private ListView lv_download;
    private DownloadListAdapter mDownloadListAdapter;
    private DownloadService mDownloadService;

    public static final int MSG_DOWNLOAD_START = 1;
    public static final int MSG_DOWNLOAD_PAUSE = 2;
    public static final int MSG_DOWNLOAD_CANCEL_WAIT = 3;
    public static final int MSG_INSTALL_APP = 4;
    public static final int MSG_OPEN_APP = 5;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int position = (int) msg.obj;
            switch (msg.what) {
                case MSG_DOWNLOAD_START:
                    startDownloadApp(position);
                    break;
                case MSG_DOWNLOAD_PAUSE:
                    pauseDownloadApp(position);
                    break;
                case MSG_DOWNLOAD_CANCEL_WAIT:
                    cancelWaitDownloadApp(position);
                    break;
                case MSG_INSTALL_APP:
                    installApp(position);
                    break;
                case MSG_OPEN_APP:
                    openApp(position);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayList<DownloadAppInfo> downLoadAppInfo = getDownLoadAppInfo();
        fillData(downLoadAppInfo);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean userVisibleHint = getUserVisibleHint();
        if (userVisibleHint) {
            Intent intent = new Intent(mMyApplication, DownloadService.class);
            mMyApplication.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent(mMyApplication, DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD_CHECK_FRAGMENT);
        mMyApplication.startService(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void initVariable() {

    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_download_app;
    }

    @Override
    protected void findViews(View rootView) {
        lv_download = (ListView) rootView.findViewById(R.id.lv_download);
    }

    @Override
    protected void setListensers() {

    }

    private ArrayList<DownloadAppInfo> getDownLoadAppInfo() {
        ArrayList<DownloadAppInfo> downloadAppInfos = new ArrayList<DownloadAppInfo>();
        //在内存中模拟数据
        for (int i = 0; i < GlobalParams.DOWNLOAD_APP_NAMES.length; i++) {
            DownloadAppInfo downloadAppInfo = new DownloadAppInfo();
            downloadAppInfo.setName(GlobalParams.DOWNLOAD_APP_NAMES[i]);
            downloadAppInfo.setUrl(GlobalParams.DOWNLOAD_APP_URLS[i]);
            downloadAppInfo.setImg(GlobalParams.DOWNLOAD_APP_ICONS[i]);
            downloadAppInfo.setPosition(i);
            downloadAppInfo.setStatus(0);
            downloadAppInfo.setId(i + 1);
            downloadAppInfos.add(downloadAppInfo);
        }
        if (mDownloadService == null) {
            return downloadAppInfos;
        }
        ArrayList<DownloadAppInfo> downloadAppInfosDB = mDownloadService.findDownloadAppInfos();
        if (downloadAppInfosDB == null || downloadAppInfosDB.size() == 0) {
            return downloadAppInfos;
        }

        //把数据库里面的数据赋值给内存
        for (int i = 0; i < downloadAppInfosDB.size(); i++) {
            DownloadAppInfo downloadAppInfoDB = downloadAppInfosDB.get(i);
            for (int j = 0; j < downloadAppInfos.size(); j++) {
                if (downloadAppInfoDB.getName().equals(downloadAppInfos.get(j).getName())) {
                    int status = downloadAppInfoDB.getStatus();
                    int max = downloadAppInfoDB.getMax();
                    int progress = downloadAppInfoDB.getProgress();
                    String path = downloadAppInfoDB.getPath();
                    downloadAppInfos.get(j).setProgress(progress);
                    downloadAppInfos.get(j).setMax(max);
                    downloadAppInfos.get(j).setPath(path);
                    downloadAppInfos.get(j).setStatus(status);
                }
            }
        }
        //判断该APP是否已经安装
        for (int i = 0; i < downloadAppInfos.size(); i++) {
            DownloadAppInfo downloadAppInfo = downloadAppInfos.get(i);
            String path = downloadAppInfo.getPath();
            if (!TextUtils.isEmpty(path)) {
                String apkFilePackage = Utils.getApkFilePackage(mMyApplication, new File(path));
                if (!TextUtils.isEmpty(apkFilePackage)) {
                    if (Utils.isAppInstalled(mMyApplication, apkFilePackage)) {
                        downloadAppInfo.setStatus(DownloadAppInfo.STATUS_INSTALLED);
                        downloadAppInfo.setPackageName(apkFilePackage);
                    }
                }
            }
        }
        return downloadAppInfos;
    }

    private void fillData(ArrayList<DownloadAppInfo> downloadAppInfo) {
        mDownLoadAppInfos = downloadAppInfo;
        if (mDownloadListAdapter == null) {
            mDownloadListAdapter = new DownloadListAdapter(mContext, mBitmapUtils, mHandler, mDownLoadAppInfos);
            lv_download.setAdapter(mDownloadListAdapter);
        } else {
            mDownloadListAdapter.setData(mDownLoadAppInfos);
            mDownloadListAdapter.notifyDataSetChanged();
        }
    }

    private void refreshList(final DownloadAppInfo downloadAppInfo) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null || mainActivity.isFinishing()) {
            return;
        }
        mainActivity.runOnUiThread(new Runnable() {
            public void run() {
                DownloadAppInfo oldDownloadAppInfo = mDownLoadAppInfos.get(downloadAppInfo.getPosition());
                oldDownloadAppInfo.setStatus(downloadAppInfo.getStatus());
                oldDownloadAppInfo.setPath(downloadAppInfo.getPath());
                oldDownloadAppInfo.setPackageName(downloadAppInfo.getPackageName());
                oldDownloadAppInfo.setMax(downloadAppInfo.getMax());
                oldDownloadAppInfo.setProgress(downloadAppInfo.getProgress());
                mDownloadListAdapter.setData(mDownLoadAppInfos);
                mDownloadListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void startDownloadApp(int position) {
        DownloadAppInfo downloadAppInfo = mDownLoadAppInfos.get(position);
        Intent intent = new Intent(mMyApplication, DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD_START);
        intent.putExtra(GlobalParams.DOWNLOAD_APP_KEY, downloadAppInfo);
        mMyApplication.startService(intent);
    }

    private void pauseDownloadApp(int position) {
        DownloadAppInfo downloadAppInfo = mDownLoadAppInfos.get(position);
        Intent intent = new Intent(mMyApplication, DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD_PAUSE);
        intent.putExtra(GlobalParams.DOWNLOAD_APP_KEY, downloadAppInfo);
        mMyApplication.startService(intent);
    }

    private void cancelWaitDownloadApp(int position) {
        DownloadAppInfo downloadAppInfo = mDownLoadAppInfos.get(position);
        Intent intent = new Intent(mMyApplication, DownloadService.class);
        intent.setAction(DownloadService.ACTION_DOWNLOAD_CANCEL_WAIT);
        intent.putExtra(GlobalParams.DOWNLOAD_APP_KEY, downloadAppInfo);
        mMyApplication.startService(intent);
    }

    private void installApp(int position) {
        final DownloadAppInfo downloadAppInfo = mDownLoadAppInfos.get(position);
        String path = downloadAppInfo.getPath();
        Utils.installApp(mMyApplication, new File(path));
    }

    private void openApp(int position) {
        final DownloadAppInfo downloadAppInfo = mDownLoadAppInfos.get(position);
        final String packagename = downloadAppInfo.getPackageName();
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = mMyApplication.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = mMyApplication.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder localBinder) {
            mDownloadService = ((DownloadService.DownloadServiceBinder) localBinder).getDownloadService(); //获取Myservice对象
            mDownloadService.setMyDownLoadListener(new MyDownLoadListener());
            ArrayList<DownloadAppInfo> downLoadAppInfo = getDownLoadAppInfo();
            fillData(downLoadAppInfo);
        }

        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    private class MyDownLoadListener implements DownloadService.DownLoadListener {

        @Override
        public void onDownLoadWait(DownloadAppInfo downloadAppInfo) {
            refreshList(downloadAppInfo);
        }

        @Override
        public void onDownLoadStart(DownloadAppInfo downloadAppInfo) {
            refreshList(downloadAppInfo);
        }

        @Override
        public void onDownloading(DownloadAppInfo downloadAppInfo) {
            refreshList(downloadAppInfo);
        }

        @Override
        public void onDownLoadSuccess(DownloadAppInfo downloadAppInfo) {
            refreshList(downloadAppInfo);
        }

        @Override
        public void onDownLoadError(DownloadAppInfo downloadAppInfo) {
            refreshList(downloadAppInfo);
        }

        @Override
        public void onDownLoadCancell(DownloadAppInfo downloadAppInfo) {
            refreshList(downloadAppInfo);
        }

        @Override
        public void onDownLoadWaitCancell(DownloadAppInfo downloadAppInfo) {

        }

        @Override
        public void onAppInstall(DownloadAppInfo downloadAppInfo) {
            refreshList(downloadAppInfo);
        }

        @Override
        public void stopDownloadService() {
            try {
                mMyApplication.unbindService(mServiceConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
