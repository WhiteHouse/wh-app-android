package gov.whitehouse.widget.wh;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.HeaderViewHolder;
import gov.whitehouse.widget.ViewBinder;

public
class DrawerItemAdapter extends BaseAdapter<MenuItem> {

    private
    void setBackgroundDrawableRetainPadding(View v, Drawable d)
    {
        int top = v.getPaddingTop();
        int right = v.getPaddingRight();
        int bottom = v.getPaddingBottom();
        int left = v.getPaddingLeft();
        v.setBackground(d);
        v.setPadding(left, top, right, bottom);
    }

    private
    void setBackgroundRetainPadding(View v, int res)
    {
        int top = v.getPaddingTop();
        int right = v.getPaddingRight();
        int bottom = v.getPaddingBottom();
        int left = v.getPaddingLeft();
        v.setBackgroundResource(res);
        v.setPadding(left, top, right, bottom);
    }

    @Override
    public
    ViewBinder<MenuItem, DrawerItemView> onCreateViewBinder(ViewGroup parent)
    {
        return new ViewBinder<>(new DrawerItemView(parent.getContext()));
    }

    @Override
    public
    HeaderViewHolder<DrawerHeaderView> onCreateHeaderHolder(ViewGroup parent)
    {
        return new HeaderViewHolder<>(new DrawerHeaderView(parent.getContext()));
    }
}
