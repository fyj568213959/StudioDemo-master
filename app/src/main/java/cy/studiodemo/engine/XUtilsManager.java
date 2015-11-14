package cy.studiodemo.engine;

import android.content.Context;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;

import cy.studiodemo.util.Utils;

public class XUtilsManager {
    private static XUtilsManager mXutilsManager;
    private BitmapUtils mBitmapUtils;
    private HttpUtils mHttpUtils;

    private XUtilsManager(Context context) {
        mHttpUtils = new HttpUtils();
        mBitmapUtils = BitmapUtils.create(context);
        mBitmapUtils.configDiskCachePath(Utils.getCachePath(context));
    }

    public static XUtilsManager getInstance(Context context) {
        if (null == mXutilsManager) {
            synchronized (XUtilsManager.class) {
                if (null == mXutilsManager) {
                    mXutilsManager = new XUtilsManager(context);
                }
            }
        }
        return mXutilsManager;
    }

    public BitmapUtils getBitmapUtils() {
        return mBitmapUtils;
    }

    public HttpUtils getHttpUtils() {
        return mHttpUtils;
    }
}