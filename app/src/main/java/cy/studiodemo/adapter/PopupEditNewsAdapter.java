package cy.studiodemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cy.studiodemo.R;
import cy.studiodemo.bean.NewsTab;


public class PopupEditNewsAdapter extends BaseAdapter {

    private ArrayList<NewsTab> mData;
    private Context mContext;
    private ViewHolder mViewHolder;
    private int mCurrentSelectPostion;

    public PopupEditNewsAdapter(Context mContext, ArrayList<NewsTab> data) {
        this.mContext = mContext;
        this.mData = data;
    }

    public void setData(ArrayList<NewsTab> data) {
        this.mData = data;
    }

    public void setSelectPostion(int currentSelectPostion) {
        this.mCurrentSelectPostion = currentSelectPostion;
    }

    @Override
    public int getCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

    @Override
    public Object getItem(int postion) {
        return mData.get(postion);
    }

    @Override
    public long getItemId(int postion) {
        return postion;
    }

    @Override
    public View getView(int postion, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.popup_edit_news_item, null);
            mViewHolder = new ViewHolder();
            mViewHolder.rl_popup_lv_root = (RelativeLayout) convertView.findViewById(R.id.rl_popup_lv_root);
            mViewHolder.cb_popup_item = (CheckBox) convertView.findViewById(R.id.cb_popup_item);
            mViewHolder.tv_popup_item = (TextView) convertView.findViewById(R.id.tv_popup_item);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        NewsTab newsTab = mData.get(postion);
        mViewHolder.tv_popup_item.setText(newsTab.getName());
        mViewHolder.cb_popup_item.setChecked(newsTab.isChecked());
        return convertView;
    }

    public class ViewHolder {
        public RelativeLayout rl_popup_lv_root;
        public TextView tv_popup_item;
        public CheckBox cb_popup_item;
    }
}
