package cy.studiodemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;


import cy.studiodemo.R;
import cy.studiodemo.base.BaseActivity;
import cy.studiodemo.bean.CityModel;
import cy.studiodemo.engine.GlobalParams;
import cy.studiodemo.engine.SharedPreferencesManager;

/**
 * Created by cuiyue on 15/8/28.
 */
public class LauncherActivity extends BaseActivity implements View.OnClickListener {

    private static final int GO_HOME_TIME = 2 * 1000;
    private long startTime;

    private static final int MSG_CHECK_CITY = 1;
    private static final int MSG_GO_HOME = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CHECK_CITY:
                    initCity();
                    break;
                case MSG_GO_HOME:
                    gotoMainActivity();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler.sendEmptyMessage(MSG_CHECK_CITY);
        mHandler.sendEmptyMessageDelayed(MSG_GO_HOME, GO_HOME_TIME);
    }

    @Override
    protected int setContentView() {
        return R.layout.activity_launcher;
    }

    @Override
    protected void findViews() {

    }

    @Override
    protected void setListensers() {
    }

    @Override
    public void onClick(View view) {
    }

    private void initCity() {
        startTime = System.currentTimeMillis();
        CityModel cityModel = null;
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(mMyApplication);
        String cityName = sharedPreferencesManager.getString(GlobalParams.CITY);
        if (TextUtils.isEmpty(cityName)) {
            cityModel = new CityModel();
            cityModel.setCity_name(GlobalParams.DEFAULT_CITY_NAME);
            cityModel.setCity_id(GlobalParams.DEFAULT_CITY_ID);
            mMyApplication.setCurrentCity(cityModel);
            sharedPreferencesManager.putString(GlobalParams.CITY, GlobalParams.DEFAULT_CITY_NAME);
            return;
        }
        DbUtils db = DbUtils.create(mContext);
        try {
            cityModel = db.findFirst(Selector.from(CityModel.class).where(WhereBuilder.b("city_name", "=", cityName))); //再从数据库找city_id
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            if (cityModel == null) {
                cityModel = new CityModel();
                cityModel.setCity_name(GlobalParams.DEFAULT_CITY_NAME);
                cityModel.setCity_id(GlobalParams.DEFAULT_CITY_ID);
            }
            mMyApplication.setCurrentCity(cityModel);
            db.close();
        }
    }


    private void gotoMainActivity() {
        long endTime = System.currentTimeMillis();
        long diffTime = endTime - startTime;
        if (diffTime < GO_HOME_TIME) {
            try {
                Thread.sleep(GO_HOME_TIME - diffTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.welcome_out);
    }
}
