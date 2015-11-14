package cy.studiodemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import cy.studiodemo.R;
import cy.studiodemo.bean.CityModel;

/**
 * Created by cuiyue on 15/8/21.
 */
public class CityListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<CityModel> list;


    public CityListAdapter(Context context, List<CityModel> list) {

        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.public_cityhot_item, null);
            holder = new ViewHolder();
            holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
            holder.name = (TextView) convertView.findViewById(R.id.public_cityhot_item_textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(list.get(position).getCity_name());
        String currentStr = list.get(position).getNameSort();
        String previewStr = (position - 1) >= 0 ? list.get(position - 1).getNameSort() : " ";
        if (!previewStr.equals(currentStr)) {
            holder.alpha.setVisibility(View.VISIBLE);
            holder.alpha.setText(currentStr);
        } else {
            holder.alpha.setVisibility(View.GONE);
        }
        return convertView;
    }

    private class ViewHolder {
        TextView alpha;
        TextView name;
    }
}
