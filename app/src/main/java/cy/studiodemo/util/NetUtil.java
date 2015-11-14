package cy.studiodemo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetUtil {

    public static final int TYPE_WIFI = 0x01;
    public static final int TYPE_CM2G = 0x02;
    public static final int TYPE_CM3G = 0x03;
    public static final int TYPE_CM4G = 0x04;
    public static final int TYPE_CMWAP = 0x05;
    public static final int TYPE_CMNET = 0x06;

    public static NetworkInfo getActiveNetInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 检测网络是否可用
     *
     * @return
     */
    public static boolean isNetConnect(Context context) {
        NetworkInfo networkInfo = getActiveNetInfo(context);
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * 判断是否wifi网络连接
     *
     * @param context
     * @return
     */
    public static boolean isWifiNet(Context context) {
        return getNetType(context) == TYPE_WIFI;
    }

    /**
     * 获取当前网络类型
     *
     * @param context
     * @return 0 没有网络; 1 WIFI网络; 2 2G网络; 3 3G网络
     */
    public static int getNetType(Context context) {
        int netType = 0;
        NetworkInfo networkInfo = getActiveNetInfo(context);
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            int subtype = networkInfo.getSubtype();
            switch (subtype) {
                case TelephonyManager.NETWORK_TYPE_EDGE:// 移动2G/联通2G
                case TelephonyManager.NETWORK_TYPE_GPRS:// 移动2G/联通2G
                case TelephonyManager.NETWORK_TYPE_CDMA:// 电信2G
                case TelephonyManager.NETWORK_TYPE_1xRTT:// 电信2.5G
                    netType = TYPE_CM2G;
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:// 联通3G
                case TelephonyManager.NETWORK_TYPE_UMTS:// 联通3G
                case TelephonyManager.NETWORK_TYPE_EVDO_0:// 电信3G
                case TelephonyManager.NETWORK_TYPE_EVDO_A:// 电信3G
                case TelephonyManager.NETWORK_TYPE_EVDO_B:// 电信3G
                    netType = TYPE_CM3G;
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:// 可能有4G网络
                    netType = TYPE_CM4G;
                    break;
                default:
                    break;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = TYPE_WIFI;
        }
        return netType;
    }

}
