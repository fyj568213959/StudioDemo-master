package cy.studiodemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;

import java.util.List;

import cy.studiodemo.R;
import cy.studiodemo.bean.Deal;

/**
 * Created by cuiyue on 15/7/27.
 */
public class TabListAdapter extends BaseAdapter {

    private Context mContext;
    private BitmapUtils mBitmapUtils;
    private List<Deal> mDeals;
    private ViewHolder mViewHolder;

    public TabListAdapter(Context context, BitmapUtils bitmapUtils, List<Deal> deals) {
        this.mContext = context;
        this.mDeals = deals;
        this.mBitmapUtils = bitmapUtils;
    }

    @Override
    public int getCount() {
        return mDeals.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void setData(List<Deal> deal) {
        this.mDeals = deal;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_news, null);
            mViewHolder = new ViewHolder();
            mViewHolder.iv_item_main = (ImageView) convertView.findViewById(R.id.iv_item_main);
            mViewHolder.tv_item_name = (TextView) convertView.findViewById(R.id.tv_item_name);
            mViewHolder.tv_item_description = (TextView) convertView.findViewById(R.id.tv_item_description);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        Deal deal = mDeals.get(position);
        String title = deal.getTitle();
        String description = deal.getDescription();
        String image = deal.getImage();
        mViewHolder.tv_item_name.setText(title);
        mViewHolder.tv_item_description.setText(description);
        mBitmapUtils.display(mViewHolder.iv_item_main, image);
        return convertView;
    }

    public class ViewHolder {
        public ImageView iv_item_main;
        public TextView tv_item_name;
        public TextView tv_item_description;
    }

}
