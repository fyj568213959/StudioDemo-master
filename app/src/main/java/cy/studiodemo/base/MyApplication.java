package cy.studiodemo.base;

import android.app.Application;
import android.content.Context;

import cy.studiodemo.bean.CityModel;
import cy.studiodemo.util.Utils;

/**
 * Created by cuiyue on 15/8/25.
 */
public class MyApplication extends Application {

    private CityModel mCurrentCity;

    @Override
    public void onCreate() {
        super.onCreate();
        boolean applicationRepeat = Utils.isApplicationRepeat(this);
        if (applicationRepeat) {
            return;
        }
    }

    public CityModel getCurrentCity() {
        return mCurrentCity;
    }

    public void setCurrentCity(CityModel currentCity) {
        this.mCurrentCity = currentCity;
    }
}
