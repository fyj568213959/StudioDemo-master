package cy.studiodemo.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.http.RequestParams;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cy.studiodemo.R;
import cy.studiodemo.adapter.HomeViewFlowAdapter;
import cy.studiodemo.adapter.TabListAdapter;
import cy.studiodemo.base.BaseFragment;
import cy.studiodemo.bean.CityModel;
import cy.studiodemo.bean.Citys;
import cy.studiodemo.bean.Deal;
import cy.studiodemo.bean.DealData;
import cy.studiodemo.engine.GlobalParams;
import cy.studiodemo.engine.ObserverManager;
import cy.studiodemo.util.GsonUtil;
import cy.studiodemo.util.ToastUtil;
import cy.studiodemo.view.RectFlowIndicator;
import cy.studiodemo.view.ViewFlow;
import cy.studiodemo.view.XListView;

/**
 * Created by cuiyue on 15/7/27.
 */
public class TabFragment extends BaseFragment {

    private XListView home_Xlistview;
    private ViewFlow mViewFlow;
    private LinearLayout mViewflowindiclay;
    private ArrayList<Integer> listHeaderData;
    private ArrayList<Deal> mAllDeals = new ArrayList<Deal>();
    private TabListAdapter newsListAdapter;
    private static final int page_size = 20;//默认一页显示20条

    private String mCityUrl;
    private String mDealDataUrl;
    private String keyword;//查询条件关键词

    private int mHeaderPosition;
    private int page = 1;
    private boolean isRefresh; //是否是下拉刷新
    private boolean isCityChanged = false;

    private static final int MSG_STOP_REFRESH = 0;
    //    private static final int MSG_GET_DATA = 1;
    private static final int MSG_GET_DATA_OVER = 2;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_STOP_REFRESH:
                    stopRefresh();
                    break;
//                case MSG_GET_DATA:
//                    initData();
//                    break;
                case MSG_GET_DATA_OVER:
                    ToastUtil.showToast(mContext, "亲,没有更多数据了哦.");
                    stopRefresh();
                    home_Xlistview.setPullLoadEnable(false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initVariable() {
        keyword = getArguments().getString("title");
        showBaseLoading();
        initXListViewHeader();
        initData();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    protected int setContentView() {
        return R.layout.fragment_tab;
    }

    @Override
    protected void findViews(View rootView) {
        home_Xlistview = (XListView) rootView.findViewById(R.id.home_Xlistview);

    }

    @Override
    protected void setListensers() {
        home_Xlistview.setPullLoadEnable(true);
        home_Xlistview.setSelector(android.R.color.transparent);
        home_Xlistview.setOnItemClickListener(new MyOnItemClickListener());
        home_Xlistview.setXListViewListener(new MyIXListViewListener());
        ObserverManager mObserverManager = ObserverManager.getInstance();
        mObserverManager.addCityChangedObserver(new ObserverManager.OnCityChangedListener() {
            @Override
            public void onCityChanged() {
                isCityChanged = true;
                home_Xlistview.setVisibility(View.GONE);
                isShowAnim(true);
                showBaseLoading();
                getCityDealData();
            }
        });
    }


    private void setHeaderData() {
        listHeaderData = getListHeaderData();
        mViewflowindiclay.removeAllViews();
        HomeViewFlowAdapter mHomeViewFlowAdapter = new HomeViewFlowAdapter(mContext, listHeaderData);
        mViewFlow.setAdapter(mHomeViewFlowAdapter);
        mViewFlow.setmSideBuffer(listHeaderData.size());
        mViewFlow.setTimeSpan(5000);
        mViewFlow.setSelection(listHeaderData.size() * 1000);
        mViewFlow.stopAutoFlowTimer();
        mViewFlow.startAutoFlowTimer();
        RectFlowIndicator viewflowindic = new RectFlowIndicator(mContext);
        viewflowindic.setFillColor(mContext.getResources().getColor(R.color.white));
        viewflowindic.setProperty(mContext.getResources().getDimension(R.dimen.radius), mContext.getResources().getDimension(R.dimen.circleSeparation),
                mContext.getResources().getDimension(R.dimen.activeRadius), 0, false);
        mViewflowindiclay.addView(viewflowindic);
        mViewFlow.setFlowIndicator(viewflowindic);
        mViewFlow.setOnViewSwitchListener(new MyViewSwitchListener());
    }

    private void initData() {
        setHeaderData();
        getCityDealData();
    }

    /**
     * 得到数据
     */
    private void getCityDealData() {
        CityModel currentCity = mMyApplication.getCurrentCity();
        String city_name = currentCity.getCity_name();
        String cityId = currentCity.getCity_id();
        if (TextUtils.isEmpty(cityId)) { //先从内存找city_id
            DbUtils db = DbUtils.create(mContext);
            try {
                CityModel cityModel = db.findFirst(Selector.from(CityModel.class).where(WhereBuilder.b("city_name", "=", city_name))); //再从数据库找city_id
                if (cityModel == null) {
                    getCityData();//最后从网络找
                } else {
                    currentCity.setCity_id(cityModel.getCity_id());
                    getDealData();
                }
            } catch (DbException e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
        } else {
            getDealData();
        }
    }

    /**
     * 得到城市信息
     */
    private void getCityData() {
        mCityUrl = GlobalParams.NUO_MI_CITIES;
        RequestParams requestParams = new RequestParams();
        requestParams.addHeader("apikey", GlobalParams.API_KEY);
        initDataByGet(mCityUrl, requestParams);
    }

    /**
     * 得到该城市的数据
     */
    private void getDealData() {
        CityModel currentCity = mMyApplication.getCurrentCity();
        mDealDataUrl = GlobalParams.NUO_MI_DEALS + "?city_id=" + currentCity.getCity_id() + "&page_size=" + page_size + "&page=" +
                page + "&keyword=" + keyword;
        RequestParams requestParams = new RequestParams();
        requestParams.addHeader("apikey", GlobalParams.API_KEY);
        initDataByGet(mDealDataUrl, requestParams);
    }


    @Override
    protected void initDataOnSucess(String result, String url) {
        super.initDataOnSucess(result, url);
        if (url.equals(mCityUrl)) {
            onCitySucess(result);
        } else if (url.equals(mDealDataUrl)) {
            fillData(result);
        }
        //以下代码模拟数据加载失败
        //        if (checkDataIsOK()) {
//            fillData(result);
//        } else {
//            if (mAllDeals.size() == 0 && !isRefresh) {
//                showLoadingError();
//            } else {
//                home_Xlistview.setPullLoadEnable(false);
//                mHandler.sendEmptyMessage(MSG_STOP_REFRESH);
//                ToastUtil.showToast(mContext, "亲,加载错误啦，请重试.");
//            }
//        }
    }

    private void onCitySucess(String result) {
        Citys citys = GsonUtil.json2bean(result, Citys.class);
        List<CityModel> cityModels = citys.getCities();

        if (cityModels == null || cityModels.size() == 0) {
            //TODO 得到城市信息失败
            return;
        }
        CityModel cityModel = mMyApplication.getCurrentCity();
        String cityId = "";
        for (int i = 0; i < cityModels.size(); i++) {
            if (cityModel.getCity_name().equals(cityModels.get(i).getCity_name())) {
                cityId = cityModels.get(i).getCity_id();
                break;
            }
        }
        cityModel.setCity_id(cityId); //保存到内存
        CityModel cm = new CityModel();
        cm.setCity_name(cityModel.getCity_name());
        cm.setCity_id(cityId);
        DbUtils db = DbUtils.create(mContext); //保存到数据库
        try {
            db.save(cm);
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        getDealData();
    }

    private void fillData(String result) {
        DealData dealData = null;
        try {
            dealData = GsonUtil.json2bean(result, DealData.class);
            ArrayList<Deal> deals = dealData.getData().getDeals();
            if (isRefresh || isCityChanged) {
                mAllDeals.clear();
            }
            mAllDeals.addAll(deals);
            if (newsListAdapter == null) {
                newsListAdapter = new TabListAdapter(mContext, mBitmapUtils, mAllDeals);
                home_Xlistview.setAdapter(newsListAdapter);
            } else {
                newsListAdapter.setData(mAllDeals);
                newsListAdapter.notifyDataSetChanged();
            }
            mHandler.sendEmptyMessage(MSG_STOP_REFRESH);
            home_Xlistview.setVisibility(View.VISIBLE);
            hideBaseLoading();
        } catch (Exception e) {
            hideBaseLoading();
            if (mAllDeals.size() == 0 && !isRefresh) {
                showLoadingError();
            } else {
                mHandler.sendEmptyMessage(MSG_STOP_REFRESH);
                ToastUtil.showToast(mContext, "亲,加载错误啦，请重试.");
            }
            e.printStackTrace();
        }
        isCityChanged = false;
    }

    @Override
    protected void initDataOnFailure(String url) {
        super.initDataOnFailure(url);
    }

    /**
     * 模拟数据是否正确
     */
    private boolean checkDataIsOK() {
        Random random = new Random();
        int num = random.nextInt(10);
        return num > 2;
    }


    private ArrayList<Integer> getListHeaderData() {
        ArrayList<Integer> datas = new ArrayList<Integer>();
        datas.add(R.mipmap.a);
        datas.add(R.mipmap.b);
        datas.add(R.mipmap.c);
        datas.add(R.mipmap.d);
        return datas;
    }


    private void initXListViewHeader() {
        View headView = LayoutInflater.from(mContext).inflate(R.layout.home_xgridview_header, null);
        mViewFlow = (ViewFlow) headView.findViewById(R.id.vfHomeGallery);
        mViewflowindiclay = (LinearLayout) headView.findViewById(R.id.viewflowindiclay);
        home_Xlistview.addHeaderView(headView);
    }

    /**
     * 停止刷新
     */
    private void stopRefresh() {
        home_Xlistview.stopLoadMore();
        home_Xlistview.stopRefresh();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        home_Xlistview.setRefreshTime(simpleDateFormat.format(new Date(System.currentTimeMillis())));
    }

    @Override
    protected void onClickBaseLoading() {
        super.onClickBaseLoading();
        showBaseLoading();
        getDealData();
    }


    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        }
    }

    private class MyIXListViewListener implements XListView.IXListViewListener {

        @Override
        public void onRefresh() {
            isRefresh = true;
            home_Xlistview.setPullLoadEnable(true);
            page++;
            getDealData();
        }

        @Override
        public void onLoadMore() {
            isRefresh = false;
            if (mAllDeals.size() >= 100) {
                mHandler.sendEmptyMessage(MSG_GET_DATA_OVER);
            } else {
                page++;
                getDealData();
            }
        }

        @Override
        public void onRightSlip() {

        }

        @Override
        public void onLeftSlip() {

        }
    }


    private class MyViewSwitchListener implements ViewFlow.ViewSwitchListener {

        @Override
        public void onSwitched(View view, int position) {
            try {
                mHeaderPosition = position % listHeaderData.size();
            } catch (Exception e) {
            }
        }
    }
}
