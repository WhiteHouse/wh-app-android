package gov.whitehouse.widget;

import android.view.View;

public
class ViewBinder<T, V extends View & Bindable<T>> extends BaseViewHolder
{

    public
    ViewBinder(V itemView)
    {
        super(itemView);
    }

    public
    void dispatchOnBindData(T data, int position)
    {
        ((V)itemView).onBindWith(data, position);
    }

    @Override
    public
    void setOnItemClickListener(BaseAdapter.OnItemClickListener listener, int position)
    {
        ((V) itemView).onSetViewClickListener(listener, position);
    }
}
