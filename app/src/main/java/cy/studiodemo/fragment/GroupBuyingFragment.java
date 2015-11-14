package cy.studiodemo.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;

import cy.studiodemo.R;
import cy.studiodemo.adapter.MyFragmentStatePagerAdapter;
import cy.studiodemo.adapter.PopupEditNewsAdapter;
import cy.studiodemo.base.BaseFragment;
import cy.studiodemo.bean.NewsTab;
import cy.studiodemo.engine.ObserverManager;
import cy.studiodemo.util.Utils;
import cy.studiodemo.view.SmartTabLayout;
import cy.studiodemo.view.XViewPager;

/**
 * Created by cuiyue on 15/7/27.
 */
public class GroupBuyingFragment extends BaseFragment implements View.OnClickListener {

    private ViewGroup tab;
    private XViewPager viewPager;
    private PopupWindow mEditNewsPopupWindow;
    private SmartTabLayout viewPagerTab;
    private ImageView iv_news_arrow_down;
    private TextView tv_popup_footer;
    private TextView tv_popup_footer_ok;
    private RelativeLayout rl_news_top;
    private RelativeLayout rl_news_arrow_down;
    private ArrayList<NewsTab> mAllNewsTabs;
    private ArrayList<NewsTab> mNewsTabs;
    private ArrayList<BaseFragment> mFragments;
    private MyFragmentStatePagerAdapter myFragmentStatePagerAdapter;
    private PopupEditNewsAdapter mLeftPopupAdapter;
    private int mEditTabSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initVariable() {
        isShowAnim(false);
        initTitles();
        initFragment();
        initEditNewsPopupWindow();
        myFragmentStatePagerAdapter = new MyFragmentStatePagerAdapter(getChildFragmentManager(), mNewsTabs, mFragments);
        viewPager.setAdapter(myFragmentStatePagerAdapter);
        viewPagerTab.setViewPager(viewPager);
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_news;
    }

    @Override
    protected void findViews(View rootView) {
        tab = (ViewGroup) rootView.findViewById(R.id.tab);
        rl_news_top = (RelativeLayout) rootView.findViewById(R.id.rl_news_top);
        rl_news_arrow_down = (RelativeLayout) rootView.findViewById(R.id.rl_news_arrow_down);
        iv_news_arrow_down = (ImageView) rootView.findViewById(R.id.iv_news_arrow_down);
        viewPager = (XViewPager) rootView.findViewById(R.id.viewpager);
        View tabView = mInflater.inflate(R.layout.demo_indicator_trick1, tab, false);
        tab.addView(tabView);
        viewPagerTab = (SmartTabLayout) tab.findViewById(R.id.viewpagertab);
        viewPagerTab.setDefaultTabTextColor(getActivity().getResources().getColor(R.color.uu_black));
    }

    @Override
    protected void setListensers() {
        rl_news_arrow_down.setOnClickListener(this);
        viewPagerTab.setCustomTabColorizer(new MyTabColorizer());
    }

    /**
     * 初始化titles
     */
    private void initTitles() {
        mAllNewsTabs = findAllTitles();
        if (mAllNewsTabs != null) {
            mNewsTabs = new ArrayList<NewsTab>();
            for (int i = 0; i < mAllNewsTabs.size(); i++) {
                if (mAllNewsTabs.get(i).isChecked()) {
                    mNewsTabs.add(mAllNewsTabs.get(i));
                }
            }
            return;
        }
        ArrayList<String> titles = new ArrayList<String>();
        titles.add("美食");
        titles.add("蛋糕");
        titles.add("自助餐");
        titles.add("酒店");
        titles.add("KTV");
        titles.add("足疗");
        titles.add("台球");
        titles.add("密室逃脱");
        titles.add("美发");
        titles.add("其它");
        mAllNewsTabs = new ArrayList<NewsTab>();
        mNewsTabs = new ArrayList<NewsTab>();
        for (int i = 0; i < titles.size(); i++) {
            NewsTab newsTab = new NewsTab();
            newsTab.setName(titles.get(i));
            newsTab.setIsChecked(true);
            mAllNewsTabs.add(newsTab);
            mNewsTabs.add(newsTab);
        }
        saveTitles(mAllNewsTabs);
    }

    /**
     * 从数据库查找
     */
    private ArrayList<NewsTab> findAllTitles() {
        DbUtils db = DbUtils.create(getActivity());
        ArrayList<NewsTab> newsTabs = null;
        try {
            newsTabs = (ArrayList<NewsTab>) db.findAll(NewsTab.class);//通过类型查找
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return newsTabs;
    }

    /**
     * 保存
     */
    private void saveTitles(ArrayList<NewsTab> editNewsTabs) {
        DbUtils db = DbUtils.create(getActivity());
        try {
            db.saveAll(editNewsTabs);
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * 替换
     */
    private void replaceTitles(ArrayList<NewsTab> editNewsTabs) {
        DbUtils db = DbUtils.create(getActivity());
        try {
            db.deleteAll(editNewsTabs);
            db.saveAll(editNewsTabs);
        } catch (DbException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * 初始化Fragments
     */
    private void initFragment() {
        mFragments = new ArrayList<BaseFragment>();
        for (int i = 0; i < mNewsTabs.size(); i++) {
            Bundle bundle = new Bundle();
            String title = mNewsTabs.get(i).getName();
            bundle.putString("title", title);
            if ("其它".equals(title)) {
                BaseFragment tabOtherfragment = (BaseFragment) Fragment.instantiate(getActivity(), TabOtherFragment.class.getName(), bundle);
                mFragments.add(tabOtherfragment);
            } else {
                BaseFragment tabfragment = (BaseFragment) Fragment.instantiate(getActivity(), TabFragment.class.getName(), bundle);
                mFragments.add(tabfragment);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_news_arrow_down:
                showEditNewsPopupWindow();
                break;
        }
    }

    /**
     * 显示编辑PopupWindow
     */
    private void showEditNewsPopupWindow() {
        mAllNewsTabs = findAllTitles();
        mLeftPopupAdapter.setData(mAllNewsTabs);
        mLeftPopupAdapter.notifyDataSetChanged();
        mEditNewsPopupWindow.showAsDropDown(rl_news_top);
        tv_popup_footer.setText(getEditTabSize() + "/" + mAllNewsTabs.size());
    }

    /**
     * 隐藏编辑PopupWindow
     */
    private void hideTabPopupWindow() {
        if (mEditNewsPopupWindow != null && mEditNewsPopupWindow.isShowing()) {
            mEditNewsPopupWindow.dismiss();
        }
    }

    /**
     * 初始化TabPopupWindow
     */
    private void initEditNewsPopupWindow() {
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.popup_edit_news, null);
        tv_popup_footer = (TextView) contentView.findViewById(R.id.tv_popup_footer);
        tv_popup_footer.setText(getEditTabSize() + "/" + mAllNewsTabs.size());
        tv_popup_footer_ok = (TextView) contentView.findViewById(R.id.tv_popup_footer_ok);
        tv_popup_footer_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditNewsPopupWindow.dismiss();
                removeTitles();
            }
        });
        int widthPixels = Utils.getWidthPixels(mContext);
        int heightPixels = Utils.getHeightPixels(mContext);
        mEditNewsPopupWindow = new PopupWindow(contentView, widthPixels, heightPixels / 2, true);
        ColorDrawable colorDrawable = new ColorDrawable(mContext.getApplicationContext().getResources().getColor(R.color.white));
        mEditNewsPopupWindow.setBackgroundDrawable(colorDrawable);
        mEditNewsPopupWindow.setOutsideTouchable(true);
        ListView lv_left_tab_popup = (ListView) contentView.findViewById(R.id.lv_left_tab_popup);
        mLeftPopupAdapter = new PopupEditNewsAdapter(mContext, null);
        lv_left_tab_popup.setAdapter(mLeftPopupAdapter);
        lv_left_tab_popup.setOnItemClickListener(new MyOnEditNewsItemClickListener());
        mEditNewsPopupWindow.setOnDismissListener(new MyEditNewsDismissListener());
    }

    private void removeTitles() {

        ArrayList<NewsTab> mEditNewsTabs = new ArrayList<NewsTab>();
        ArrayList<BaseFragment> mEditFragments = new ArrayList<BaseFragment>();

        for (int i = 0; i < mAllNewsTabs.size(); i++) {
            if (mAllNewsTabs.get(i).isChecked()) {
                mEditNewsTabs.add(mAllNewsTabs.get(i));
            }
        }
        for (int i = 0; i < mEditNewsTabs.size(); i++) {
            String name = mEditNewsTabs.get(i).getName();
            boolean flag = false;
            for (int j = 0; j < mNewsTabs.size(); j++) {
                if (name.equals(mNewsTabs.get(j).getName())) {
                    mEditFragments.add(mFragments.get(j));
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                Bundle bundle = new Bundle();
                bundle.putString("title", name);
                BaseFragment tabfragment = (BaseFragment) Fragment.instantiate(getActivity(), TabFragment.class.getName(), bundle);
                mEditFragments.add(tabfragment);
            }
        }
        mEditTabSize = mEditNewsTabs.size();
        myFragmentStatePagerAdapter.setTitles(mEditNewsTabs);
        myFragmentStatePagerAdapter.setFragments(mEditFragments);
        myFragmentStatePagerAdapter.notifyDataSetChanged();
        viewPagerTab.setViewPager(viewPager);
        replaceTitles(mAllNewsTabs);
    }


    private int getEditTabSize() {
        int size = 0;
        for (int i = 0; i < mAllNewsTabs.size(); i++) {
            if (mAllNewsTabs.get(i).isChecked()) {
                size++;
            }
        }
        return size;
    }

    public void notifyCityChanged() {
        ObserverManager observerManager = ObserverManager.getInstance();
        observerManager.notifyCityChanged();
    }

    /**
     * 左边PopupWindow条目点击事件
     */
    private class MyOnEditNewsItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NewsTab newsTab = mAllNewsTabs.get(position);
            if (newsTab.isChecked()) {
                newsTab.setIsChecked(false);
            } else {
                newsTab.setIsChecked(true);
            }
            tv_popup_footer.setText(getEditTabSize() + "/" + mAllNewsTabs.size());
            mLeftPopupAdapter.setData(mAllNewsTabs);
            mLeftPopupAdapter.notifyDataSetChanged();
        }
    }

    private class MyEditNewsDismissListener implements PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {

        }

    }

    private class MyTabColorizer implements SmartTabLayout.TabColorizer {

        @Override
        public int getIndicatorColor(int position) {
            return getActivity().getResources().getColor(R.color.uu_bule);
        }

        @Override
        public int getDividerColor(int position) {
            return 0;
        }
    }

}
