package gov.whitehouse.widget.wh;

import android.view.ViewGroup;

import gov.whitehouse.core.FeedItemData;
import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.ViewBinder;

public
class FeedItemAdapter extends BaseAdapter<FeedItemData> {

    @Override
    public
    ViewBinder<FeedItemData, FeedItemView> onCreateViewBinder(ViewGroup viewGroup)
    {
        return new ViewBinder<>(new FeedItemView(viewGroup.getContext()));
    }
}
