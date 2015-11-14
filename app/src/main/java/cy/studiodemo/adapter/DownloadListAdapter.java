package cy.studiodemo.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import cy.studiodemo.R;
import cy.studiodemo.bean.DownloadAppInfo;
import cy.studiodemo.fragment.DownloadAppFragment;

/**
 * Created by cuiyue on 15/9/6.
 */
public class DownloadListAdapter extends BaseAdapter {

    private Context mContext;
    private BitmapUtils mBitmapUtils;
    private Handler mHandler;
    private ArrayList<DownloadAppInfo> mDownloadAppInfos;
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public DownloadListAdapter(Context context, BitmapUtils bitmapUtils, Handler handler, ArrayList<DownloadAppInfo> downloadAppInfos) {
        this.mContext = context;
        this.mBitmapUtils = bitmapUtils;
        this.mHandler = handler;
        this.mDownloadAppInfos = downloadAppInfos;
    }

    @Override
    public int getCount() {
        return mDownloadAppInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mDownloadAppInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(ArrayList<DownloadAppInfo> downloadAppInfos) {
        this.mDownloadAppInfos = downloadAppInfos;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_download, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_down_name = (TextView) convertView.findViewById(R.id.tv_down_name);
            viewHolder.tv_download_size = (TextView) convertView.findViewById(R.id.tv_download_size);
            viewHolder.tv_download_status = (TextView) convertView.findViewById(R.id.tv_download_status);
            viewHolder.iv_download_icon = (ImageView) convertView.findViewById(R.id.iv_download_icon);
            viewHolder.bt_download = (Button) convertView.findViewById(R.id.bt_download);
            viewHolder.pb_download = (ProgressBar) convertView.findViewById(R.id.pb_download);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final DownloadAppInfo downloadAppInfo = mDownloadAppInfos.get(position);
        viewHolder.tv_down_name.setText(downloadAppInfo.getName());
        switch (downloadAppInfo.getStatus()) {
            case DownloadAppInfo.STATUS_NOT_DOWNLOAD:
                viewHolder.tv_download_status.setText("未下载");
                viewHolder.bt_download.setText("下载");
                viewHolder.bt_download.setEnabled(true);
                viewHolder.bt_download.setTextColor(mContext.getResources().getColor(R.color.white));
                break;
            case DownloadAppInfo.STATUS_WAIT:
                viewHolder.tv_download_status.setText("排队中.请稍后");
                viewHolder.bt_download.setText("等待");
                viewHolder.bt_download.setEnabled(true);
                viewHolder.bt_download.setTextColor(mContext.getResources().getColor(R.color.white));
                break;
            case DownloadAppInfo.STATUS_START:
                viewHolder.tv_download_status.setText("连接中");
                viewHolder.bt_download.setText("连接中");
                viewHolder.bt_download.setEnabled(false);
                viewHolder.bt_download.setTextColor(mContext.getResources().getColor(R.color.accent_yellow));
                break;
            case DownloadAppInfo.STATUS_DOWNLOADING:
                viewHolder.tv_download_status.setText("下载中");
                viewHolder.bt_download.setText("暂停");
                viewHolder.bt_download.setEnabled(true);
                viewHolder.bt_download.setTextColor(mContext.getResources().getColor(R.color.white));
                break;
            case DownloadAppInfo.STATUS_PAUSE:
                viewHolder.tv_download_status.setText("暂停");
                viewHolder.bt_download.setText("继续");
                viewHolder.bt_download.setEnabled(true);
                viewHolder.bt_download.setTextColor(mContext.getResources().getColor(R.color.white));
                break;
            case DownloadAppInfo.STATUS_COMPLETE:
                viewHolder.tv_download_status.setText("下载完成");
                viewHolder.bt_download.setText("安装");
                viewHolder.bt_download.setEnabled(true);
                viewHolder.bt_download.setTextColor(mContext.getResources().getColor(R.color.white));
                break;
            case DownloadAppInfo.STATUS_INSTALLED:
                viewHolder.tv_download_status.setText("下载完成");
                viewHolder.bt_download.setText("打开");
                viewHolder.bt_download.setEnabled(true);
                viewHolder.bt_download.setTextColor(mContext.getResources().getColor(R.color.white));
                break;
            case DownloadAppInfo.STATUS_DOWNLOAD_ERROR:
                viewHolder.tv_download_status.setText("下载失败");
                viewHolder.bt_download.setText("重新下载");
                viewHolder.bt_download.setEnabled(true);
                viewHolder.bt_download.setTextColor(mContext.getResources().getColor(R.color.white));
                break;
        }
        mBitmapUtils.display(viewHolder.iv_download_icon, downloadAppInfo.getImg());

        int max = downloadAppInfo.getMax();
        int progress = downloadAppInfo.getProgress();

        if (max == 0 || progress == 0) {
            viewHolder.tv_download_size.setVisibility(View.INVISIBLE);
            viewHolder.pb_download.setProgress(0);
            viewHolder.pb_download.setMax(0);
        } else {
            viewHolder.tv_download_size.setVisibility(View.VISIBLE);
            String downloadPerSize = getDownloadPerSize(progress, max);
            viewHolder.tv_download_size.setText(downloadPerSize);
            viewHolder.pb_download.setMax(max);
            viewHolder.pb_download.setProgress(progress);
        }

        viewHolder.bt_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (downloadAppInfo.getStatus()) {
                    case DownloadAppInfo.STATUS_NOT_DOWNLOAD:
                        Message msgStart = Message.obtain();
                        msgStart.what = DownloadAppFragment.MSG_DOWNLOAD_START;
                        msgStart.obj = position;
                        mHandler.sendMessage(msgStart);
                        break;
                    case DownloadAppInfo.STATUS_WAIT:
                        Message msgCancelWait = Message.obtain();
                        msgCancelWait.what = DownloadAppFragment.MSG_DOWNLOAD_CANCEL_WAIT;
                        msgCancelWait.obj = position;
                        mHandler.sendMessage(msgCancelWait);
                        break;
                    case DownloadAppInfo.STATUS_DOWNLOADING:
                        Message msgPause = Message.obtain();
                        msgPause.what = DownloadAppFragment.MSG_DOWNLOAD_PAUSE;
                        msgPause.obj = position;
                        mHandler.sendMessage(msgPause);
                        break;
                    case DownloadAppInfo.STATUS_PAUSE:
                        Message msgReStart = Message.obtain();
                        msgReStart.what = DownloadAppFragment.MSG_DOWNLOAD_START;
                        msgReStart.obj = position;
                        mHandler.sendMessage(msgReStart);
                        break;
                    case DownloadAppInfo.STATUS_COMPLETE:
                        Message msgInstall = Message.obtain();
                        msgInstall.what = DownloadAppFragment.MSG_INSTALL_APP;
                        msgInstall.obj = position;
                        mHandler.sendMessage(msgInstall);
                        break;
                    case DownloadAppInfo.STATUS_INSTALLED:
                        Message msgOpen = Message.obtain();
                        msgOpen.what = DownloadAppFragment.MSG_OPEN_APP;
                        msgOpen.obj = position;
                        mHandler.sendMessage(msgOpen);
                        break;
                    case DownloadAppInfo.STATUS_DOWNLOAD_ERROR:
                        break;
                }
            }
        });
        return convertView;
    }

    private String getDownloadPerSize(long finished, long total) {
        return DF.format((float) finished / (1024 * 1024)) + "M/" + DF.format((float) total / (1024 * 1024)) + "M";
    }

    private class ViewHolder {
        public TextView tv_down_name;
        public TextView tv_download_size;
        public TextView tv_download_status;
        public ImageView iv_download_icon;
        public Button bt_download;
        public ProgressBar pb_download;
    }
}
