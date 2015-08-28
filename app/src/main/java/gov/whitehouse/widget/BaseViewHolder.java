package gov.whitehouse.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public
class BaseViewHolder extends RecyclerView.ViewHolder
{

    public
    BaseViewHolder(View itemView)
    {
        super(itemView);
        ViewGroup.LayoutParams lps = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.setLayoutParams(lps);
    }

    public
    void setOnItemClickListener(BaseAdapter.OnItemClickListener listener, int position)
    {
        itemView.setOnClickListener(v -> listener.onItemClick(itemView, position));
    }
}
