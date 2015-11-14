package cy.studiodemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import cy.studiodemo.R;
import cy.studiodemo.view.FilletLayout;

public class HomeViewFlowAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ViewHolder mViewHolder;
    private ArrayList<Integer> mDatas;
    private Context context;

    public HomeViewFlowAdapter(Context context, ArrayList<Integer> datas) {
        this.mDatas = datas;
        this.context = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(ArrayList<Integer> datas) {
        this.mDatas = datas;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position % (mDatas.size()));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int fPosition = position % mDatas.size();
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.home_xgirdview_header_item, null);
            mViewHolder = new ViewHolder();
            mViewHolder.image = (FilletLayout) convertView.findViewById(R.id.gallery_img);
            mViewHolder.linlang_logo_iv = (ImageView) convertView.findViewById(R.id.linlang_logo_iv);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.image.setBackgroundResource(mDatas.get(fPosition));
        mViewHolder.image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "点击--" + fPosition, Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    public class ViewHolder {
        public FilletLayout image;
        public ImageView linlang_logo_iv;
    }
}
