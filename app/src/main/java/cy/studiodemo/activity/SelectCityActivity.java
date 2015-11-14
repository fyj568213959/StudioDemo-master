package cy.studiodemo.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cy.studiodemo.R;
import cy.studiodemo.adapter.CitySearchAdapter;
import cy.studiodemo.adapter.HotCityGridAdapter;
import cy.studiodemo.adapter.CityListAdapter;
import cy.studiodemo.base.BaseActivity;
import cy.studiodemo.bean.CityModel;
import cy.studiodemo.bean.Contacts;
import cy.studiodemo.engine.CityDBManager;
import cy.studiodemo.engine.ContactsHelper;
import cy.studiodemo.engine.GlobalParams;
import cy.studiodemo.engine.SharedPreferencesManager;
import cy.studiodemo.view.MyGridView;
import cy.studiodemo.view.MyLetterListView;

/**
 * Created by cuiyue on 15/8/21.
 */
public class SelectCityActivity extends BaseActivity implements View.OnClickListener {

    private LocationClient mLocationClient;
    private SQLiteDatabase database;
    private WindowManager windowManager;
    private OverlayThread overlayThread;
    private Handler handler;
    private View city_locating_state;
    private View city_locate_failed;
    private TextView city_locate_state;
    private ProgressBar city_locating_progress;
    private ImageView city_locate_success_img;
    private ListView mCityLit;
    private ListView mSearchList;
    private ImageButton ib_clear_search;
    private TextView overlay;
    private EditText et_search;
    private ImageButton backbutton;
    private MyLetterListView letterListView;
    private CitySearchAdapter mCitySearchAdapter;
    private HashMap<String, Integer> alphaIndexer;
    private ArrayList<CityModel> mCityNames;
    private String mLocationCityName;
    private String[] sections;
    private String[] hotcity = new String[]{"北京市", "上海市", "广州市", "深圳市", "杭州市", "南京市", "天津市", "武汉市", "重庆市"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initList();
        intBaiDuLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        windowManager.removeView(overlay);
    }

    @Override
    protected int setContentView() {
        return R.layout.activity_select_city;
    }

    @Override
    protected void findViews() {
        et_search = (EditText) findViewById(R.id.et_search);
        ib_clear_search = (ImageButton) findViewById(R.id.ib_clear_search);
        mCityLit = (ListView) findViewById(R.id.public_allcity_list);
        mSearchList = (ListView) findViewById(R.id.public_search_list);
        letterListView = (MyLetterListView) findViewById(R.id.cityLetterListView);
    }

    @Override
    protected void setListensers() {
        ib_clear_search.setOnClickListener(this);
        et_search.addTextChangedListener(new MyTextWatcher());
        mSearchList.setOnItemClickListener(new SearchListOnItemClickListener());
    }

    private void initList() {
        initCityList();
        initLetterList();
        initSearchListData();
    }

    private void initSearchListData() {
        ContactsHelper.getInstance(mMyApplication).startLoadContacts();
    }

    private void intBaiDuLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyBDLocationListener());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 0;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(false);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    private void initCityList() {
        initCityListHeader();
        CityDBManager dbManager = CityDBManager.getInstance(mMyApplication);
        dbManager.openDateBase();
        dbManager.closeDatabase();
        database = SQLiteDatabase.openOrCreateDatabase(CityDBManager.DB_PATH + "/" + CityDBManager.DB_NAME, null);
        mCityNames = getCityNames();
        database.close();
        mCityLit.setAdapter(new CityListAdapter(this, mCityNames));
        mCityLit.setOnItemClickListener(new CityListOnItemClickListener());
    }

    private void initLetterList() {
        initOverlay();
        alphaIndexer = new HashMap<String, Integer>();
        sections = new String[mCityNames.size()];
        for (int i = 0; i < mCityNames.size(); i++) {
            String currentStr = mCityNames.get(i).getNameSort();
            String previewStr = (i - 1) >= 0 ? mCityNames.get(i - 1).getNameSort() : " ";
            if (!previewStr.equals(currentStr)) {
                String name = mCityNames.get(i).getNameSort();
                alphaIndexer.put(name, i);
                sections[i] = name;
            }
        }
        handler = new Handler();
        overlayThread = new OverlayThread();
        letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
    }

    private void initCityListHeader() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //定位城市
        View header_city_locating_title = layoutInflater.inflate(R.layout.header_city_locating_title, mCityLit, false);
        mCityLit.addHeaderView(header_city_locating_title, null, false);
        //定位城市内容
        View header_city_locating = layoutInflater.inflate(R.layout.header_city_locating, mCityLit, false);
        city_locating_state = header_city_locating.findViewById(R.id.city_locating_state);
        city_locate_state = ((TextView) header_city_locating.findViewById(R.id.city_locate_state));
        city_locating_progress = ((ProgressBar) header_city_locating.findViewById(R.id.city_locating_progress));
        city_locate_success_img = ((ImageView) header_city_locating.findViewById(R.id.city_locate_success_img));
        city_locate_failed = header_city_locating.findViewById(R.id.city_locate_failed);
        city_locating_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mLocationCityName)) {
                } else {
                    selectCity(mLocationCityName);
                }
            }
        });
        mCityLit.addHeaderView(header_city_locating, null, false);
        //热门城市
        View header_city_hot_title = layoutInflater.inflate(R.layout.header_city_hot_title, mCityLit, false);
        mCityLit.addHeaderView(header_city_hot_title, null, false);
        //热门城市内容
        View header_city_hot = layoutInflater.inflate(R.layout.header_city_hot, mCityLit, false);
        final MyGridView localGridView = (MyGridView) header_city_hot.findViewById(R.id.public_hotcity_list);
        mCityLit.addHeaderView(header_city_hot, null, false);
        HotCityGridAdapter adapter = new HotCityGridAdapter(this, Arrays.asList(hotcity));
        localGridView.setAdapter(adapter);
        localGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectCity = parent.getAdapter().getItem(position).toString();
                selectCity(selectCity);
            }
        });
    }

    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        overlay = (TextView) inflater.inflate(R.layout.overlay, null);
        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlay, lp);
    }

    private ArrayList<CityModel> getCityNames() {
        ArrayList<CityModel> names = new ArrayList<CityModel>();
        Cursor cursor = database.rawQuery("SELECT * FROM T_City ORDER BY NameSort", null);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            CityModel cityModel = new CityModel();
            cityModel.setCity_name(cursor.getString(cursor.getColumnIndex("CityName")));
            cityModel.setNameSort(cursor.getString(cursor.getColumnIndex("NameSort")));
            names.add(cityModel);
        }
        cursor.close();
        return names;
    }


    private void selectCity(String cityName) {
        CityModel currentCityApp = mMyApplication.getCurrentCity();
        if (!currentCityApp.getCity_name().equals(cityName)) {
            currentCityApp.setCity_name(cityName);
            currentCityApp.setCity_id(null);
        }
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(mMyApplication);
        sharedPreferencesManager.putString(GlobalParams.CITY, cityName);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.push_right_out);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.push_right_out);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_clear_search:
                clearSearchInput();
                break;
        }
    }

    private void clearSearchInput() {
        et_search.setText("");
    }

    private class OverlayThread implements Runnable {

        @Override
        public void run() {
            overlay.setVisibility(View.GONE);
        }
    }

    private class MyBDLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            mLocationClient.stop();
            if (location == null) {
                city_locating_progress.setVisibility(View.GONE);
                city_locating_state.setVisibility(View.GONE);
                city_locate_failed.setVisibility(View.VISIBLE);
                return;
            }
            mLocationCityName = location.getCity();
            if (TextUtils.isEmpty(mLocationCityName)) {
                city_locating_progress.setVisibility(View.GONE);
                city_locating_state.setVisibility(View.GONE);
                city_locate_failed.setVisibility(View.VISIBLE);
                return;
            }
            city_locate_failed.setVisibility(View.GONE);
            city_locate_state.setVisibility(View.VISIBLE);
            city_locating_progress.setVisibility(View.GONE);
            city_locate_success_img.setVisibility(View.VISIBLE);
            city_locate_state.setText(location.getCity());
        }
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable editable) {
            String curCharacter = editable.toString().trim();
            if (TextUtils.isEmpty(curCharacter)) {
                ib_clear_search.setVisibility(View.GONE);
                mSearchList.setVisibility(View.GONE);
                mCityLit.setVisibility(View.VISIBLE);
                letterListView.setVisibility(View.VISIBLE);
                ContactsHelper.getInstance(mMyApplication).parseQwertyInputSearchContacts(null);
            } else {
                ib_clear_search.setVisibility(View.VISIBLE);
                mSearchList.setVisibility(View.VISIBLE);
                mCityLit.setVisibility(View.GONE);
                letterListView.setVisibility(View.GONE);
                ContactsHelper.getInstance(mMyApplication).parseQwertyInputSearchContacts(curCharacter);
            }
            if (ContactsHelper.mSearchContacts.size() == 0) {
                List<Contacts> contacts = new ArrayList<Contacts>();
                contacts.add(new Contacts("抱歉，没有找到相关城市"));
                if (mCitySearchAdapter == null) {
                    mCitySearchAdapter = new CitySearchAdapter(contacts, SelectCityActivity.this);
                    mSearchList.setAdapter(mCitySearchAdapter);
                } else {
                    mCitySearchAdapter.setData(contacts);
                    mCitySearchAdapter.notifyDataSetChanged();
                }
                mSearchList.setEnabled(false);
            } else {
                if (mCitySearchAdapter == null) {
                    mCitySearchAdapter = new CitySearchAdapter(ContactsHelper.mSearchContacts, SelectCityActivity.this);
                    mSearchList.setAdapter(mCitySearchAdapter);
                } else {
                    mCitySearchAdapter.setData(ContactsHelper.mSearchContacts);
                    mCitySearchAdapter.notifyDataSetChanged();
                }
                mSearchList.setEnabled(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

    }

    private class CityListOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CityModel cityModel = (CityModel) mCityLit.getAdapter().getItem(position);
            String cityName = cityModel.getCity_name();
            selectCity(cityName);
        }

    }

    private class SearchListOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Contacts cityModel = (Contacts) mSearchList.getAdapter().getItem(position);
            String cityName = cityModel.getName();
            selectCity(cityName);
        }
    }

    private class LetterListViewListener implements MyLetterListView.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            if (alphaIndexer.get(s) != null) {
                int position = alphaIndexer.get(s);
                mCityLit.setSelection(position);
                overlay.setText(sections[position]);
                overlay.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                handler.postDelayed(overlayThread, 1500);
            }
        }
    }
}
