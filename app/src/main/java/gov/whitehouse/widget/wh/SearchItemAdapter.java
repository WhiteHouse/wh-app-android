package gov.whitehouse.widget.wh;

import android.view.ViewGroup;

import gov.whitehouse.data.model.SearchResult;
import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.ViewBinder;

public
class SearchItemAdapter extends BaseAdapter<SearchResult> {

    @Override
    public
    ViewBinder<SearchResult, SearchItemView> onCreateViewBinder(ViewGroup viewGroup)
    {
        return new ViewBinder<>(new SearchItemView(viewGroup.getContext()));
    }
}
