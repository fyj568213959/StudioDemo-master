package cy.studiodemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

import cy.studiodemo.base.BaseFragment;
import cy.studiodemo.bean.NewsTab;

/**
 * Created by cuiyue on 15/8/7.
 */
public class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<NewsTab> mTitles;
    private ArrayList<BaseFragment> mFragments;
    private FragmentManager mFragmentManager;

    public MyFragmentStatePagerAdapter(FragmentManager supportFragmentManager,
                                       ArrayList<NewsTab> titles, ArrayList<BaseFragment> fragments) {
        super(supportFragmentManager);
        this.mFragmentManager = supportFragmentManager;
        this.mFragments = fragments;
        this.mTitles = titles;
    }

    public ArrayList<NewsTab> getTitles() {
        return this.mTitles;
    }

    public void setTitles(ArrayList<NewsTab> titles) {
        this.mTitles = titles;
    }

    public void setFragments(ArrayList<BaseFragment> fragments) {
        this.mFragments = fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles != null && mTitles.size() > 0) {
            return mTitles.get(position).getName();
        }
        return null;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
