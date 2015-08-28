package gov.whitehouse.app.wh;

import java.util.List;

import gov.whitehouse.core.FeedItemData;
import gov.whitehouse.data.model.FeedItem;

public
class FeedItemListInteractions
{

    public static
    interface ActivityCallbacks
    {

        public
        void onFeedItemSelected(List<FeedItemData> items, int selectedPosition);

        public
        void onRestoreSelectedItem(FeedItem item);
    }

    public static
    interface FragmentCallbacks
    {
    }
}
