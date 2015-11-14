package cy.studiodemo.engine;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by cuiyue on 15/8/24.
 */
public class SharedPreferencesManager {
    private static SharedPreferencesManager mSharedPreferencesManager;
    private static SharedPreferences mSharedPreferences;

    private SharedPreferencesManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static SharedPreferencesManager getInstance(Context context) {
        if (null == mSharedPreferencesManager) {
            synchronized (SharedPreferencesManager.class) {
                if (null == mSharedPreferencesManager) {
                    mSharedPreferencesManager = new SharedPreferencesManager(context);
                }
            }
        }
        return mSharedPreferencesManager;
    }

    public String getString(String key) {
        return mSharedPreferences.getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public int getInt(String key) {
        return mSharedPreferences.getInt(key, 0);
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public long getLong(String key) {
        return mSharedPreferences.getLong(key, 0);
    }

    public void putLong(String key, long value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public Boolean getBoolean(String key) {
        return mSharedPreferences.getBoolean(key, false);
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }
}
