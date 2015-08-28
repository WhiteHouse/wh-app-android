package gov.whitehouse.widget;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract
class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    public static final int VIEW_TYPE_NORMAL = 0;

    public static final int VIEW_TYPE_HEADER = 1;

    private
    boolean mNotifyOnChange = true;

    private
    List<Object> mHeaders = new ArrayList<>();

    private
    List<T> mData;

    private
    OnItemClickListener mItemClickListener;

    private
    SparseBooleanArray mSelectedItems = new SparseBooleanArray();

    public
    BaseAdapter(Collection<T> data)
    {
        mData = new ArrayList<>((data != null)?data.size():10);
        if (data != null) {
            fillWith(data);
        }
    }

    public
    BaseAdapter()
    {
        this(null);
    }

    public abstract
    ViewBinder<T, ?> onCreateViewBinder(ViewGroup parent);

    public
    HeaderViewHolder onCreateHeaderHolder(ViewGroup parent)
    {
        return null;
    }

    @Override
    public
    int getItemViewType(int position)
    {
        if (position < mHeaders.size()) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_NORMAL;
    }

    public
    void onBindHeader(HeaderViewHolder binder, int position)
    {
    }

    public
    void onBindView(ViewBinder<T, ?> binder, int position)
    {
        binder.dispatchOnBindData(getItem(position), position);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final
    void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if (holder instanceof BaseViewHolder) {
            ((BaseViewHolder) holder).setOnItemClickListener(mItemClickListener, position);
        }
        holder.itemView.setActivated(isSelected(position));
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                onBindHeader((HeaderViewHolder) holder, position);
                break;
            case VIEW_TYPE_NORMAL:
            default:
                onBindView((ViewBinder) holder, position);
                break;
        }
    }

    @Override
    public final
    RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType == VIEW_TYPE_HEADER) {
            return onCreateHeaderHolder(parent);
        } else if (viewType == VIEW_TYPE_NORMAL) {
            return onCreateViewBinder(parent);
        }
        return onCreateViewBinder(parent);
    }

    public
    void addHeader(Object header)
    {
        mHeaders.add(header);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public
    void removeHeader(int position)
    {
        mHeaders.remove(position);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public
    void addAll(Collection<T> items)
    {
        mData.addAll(items);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public
    void clear()
    {
        mData.clear();
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public
    void fillWith(Collection<T> items)
    {
        mData.clear();
        mData.addAll(items);
        if (mNotifyOnChange) {
            notifyDataSetChanged();
        }
    }

    public
    List<T> getData()
    {
        return mData;
    }

    public
    T getItem(int position)
    {
        return mData.get(position - mHeaders.size());
    }

    public
    void setNotifyOnChange(final boolean notifyOnChange)
    {
        mNotifyOnChange = notifyOnChange;
    }

    public
    int getDataCount()
    {
        return mData.size();
    }

    public
    int getHeaderCount()
    {
        return mHeaders.size();
    }

    @Override
    public
    int getItemCount()
    {
        return mHeaders.size() + mData.size();
    }

    public
    void toggleSelection(int pos)
    {
        if (mSelectedItems.get(pos, false)) {
            mSelectedItems.delete(pos);
        } else {
            mSelectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public
    void selectOnly(int pos)
    {
        mSelectedItems.clear();
        mSelectedItems.put(pos, true);
        notifyDataSetChanged();
    }

    public
    void clearSelections()
    {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public
    boolean isSelected(int pos)
    {
        return mSelectedItems.get(pos, false);
    }

    public
    int getSelectedItemCount()
    {
        return mSelectedItems.size();
    }

    public
    List<Integer> getSelectedItems()
    {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        int i;
        int sz = mSelectedItems.size();
        for (i = 0; i < sz; i++) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }

    public
    void setOnItemClickListener(OnItemClickListener listener)
    {
        mItemClickListener = listener;
    }

    public static
    interface OnItemClickListener
    {
        public
        void onItemClick(View itemView, int position);
    }
}
