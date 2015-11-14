package cy.studiodemo.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.TextViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cy.studiodemo.R;
import cy.studiodemo.bean.MenuItem;

/**
 * Created by cuiyue on 15/7/27.
 */
public class LeftMenuAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<MenuItem> mMenuItems;
    private final int mIconSize;


    public LeftMenuAdapter(Context context, List<MenuItem> menuItems) {
        this.mContext = context;
        this.mMenuItems = menuItems;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mIconSize = context.getResources().getDimensionPixelSize(R.dimen.drawer_icon_size);
    }


    @Override
    public int getCount() {
        return mMenuItems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(R.layout.design_drawer_item, parent,
                false);
        MenuItem menuItem = mMenuItems.get(position);
        TextView itemView = (TextView) convertView;
        itemView.setText(menuItem.getName());
        Drawable icon = mContext.getResources().getDrawable(menuItem.getIcon());
        setIconColor(icon);
        if (icon != null) {
            icon.setBounds(0, 0, mIconSize, mIconSize);
            TextViewCompat.setCompoundDrawablesRelative(itemView, icon, null, null, null);
        }
        return convertView;
    }

    public void setIconColor(Drawable icon) {
        int textColorSecondary = android.R.attr.textColorSecondary;
        TypedValue value = new TypedValue();
        if (!mContext.getTheme().resolveAttribute(textColorSecondary, value, true)) {
            return;
        }
        int baseColor = mContext.getResources().getColor(value.resourceId);
        icon.setColorFilter(baseColor, PorterDuff.Mode.MULTIPLY);
    }
}
