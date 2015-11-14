package cy.studiodemo.engine;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuiyue on 15/8/24.
 */
public class ObserverManager {

    private static ObserverManager mObserverManager;
    private List<OnCityChangedListener> mCityObservers;

    public interface OnCityChangedListener {
        void onCityChanged();
    }

    private ObserverManager() {
        mCityObservers = new ArrayList<OnCityChangedListener>();
    }

    public static ObserverManager getInstance() {
        if (null == mObserverManager) {
            synchronized (ObserverManager.class) {
                if (null == mObserverManager) {
                    mObserverManager = new ObserverManager();
                }
            }
        }
        return mObserverManager;
    }


    public void addCityChangedObserver(OnCityChangedListener onCityChangedListener) {
        if (onCityChangedListener != null) {
            synchronized (mCityObservers) {
                if (!mCityObservers.contains(onCityChangedListener)) {
                    mCityObservers.add(onCityChangedListener);
                }
            }
        }
    }

    public void removeCityChangedObserver(OnCityChangedListener onCityChangedListener) {
        synchronized (mCityObservers) {
            if (mCityObservers.contains(onCityChangedListener)) {
                mCityObservers.remove(onCityChangedListener);
            }
        }
    }

    public void notifyCityChanged() {
        synchronized (mCityObservers) {
            for (OnCityChangedListener onCityChangedListener : mCityObservers) {
                onCityChangedListener.onCityChanged();
            }
        }
    }
}
