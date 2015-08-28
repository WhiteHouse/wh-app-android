package gov.whitehouse.app;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract
class BaseListFragment<T> extends BaseRecyclerFragment<T>
{

    @Override
    public
    RecyclerView.LayoutManager onCreateLayoutManager()
    {
        return new LinearLayoutManager(getActivity());
    }
}
