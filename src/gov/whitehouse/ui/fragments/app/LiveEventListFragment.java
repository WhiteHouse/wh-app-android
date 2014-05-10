/*
 * This project constitutes a work of the United States Government and is
 * not subject to domestic copyright protection under 17 USC § 105.
 * 
 * However, because the project utilizes code licensed from contributors
 * and other third parties, it therefore is licensed under the MIT
 * License.  http://opensource.org/licenses/mit-license.php.  Under that
 * license, permission is granted free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the conditions that any appropriate copyright notices and this
 * permission notice are included in all copies or substantial portions
 * of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package gov.whitehouse.ui.fragments.app;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static gov.whitehouse.services.LiveService.EXTRA_JSON;
import static gov.whitehouse.ui.activities.app.VideoPlayerActivity.ARG_IS_LIVE;
import static gov.whitehouse.ui.activities.app.VideoPlayerActivity.ARG_ITEM_JSON;
import static gov.whitehouse.ui.activities.app.VideoPlayerActivity.ARG_UP_TITLE;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.services.LiveService;
import gov.whitehouse.ui.activities.app.VideoPlayerActivity;
import gov.whitehouse.ui.adapters.FeedItemsListAdapter;
import gov.whitehouse.ui.fragments.BaseListFragment;
import gov.whitehouse.utils.GsonUtils;

import java.util.List;

import gov.whitehouse.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;

import com.google.gson.reflect.TypeToken;

public class LiveEventListFragment extends BaseListFragment {

    private FeedItemsListAdapter mAdapter;

    private RefreshLiveBroadcastReceiver mRefreshLiveReceiver;

    private class RefreshLiveBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /* Don't do anything if we can't get to our parent activity */
            if (isDetached() || isRemoving()) {
                return;
            }

            setEmptyText(getString(R.string.nothing_is_live));

            mAdapter = new FeedItemsListAdapter(getBaseActivity());

            final String eventsJson = intent.getStringExtra(EXTRA_JSON);
            if (eventsJson != null) {
                TypeToken<List<FeedItem>> typeToken = new TypeToken<List<FeedItem>>() {
                };
                List<FeedItem> fetched = (List<FeedItem>) GsonUtils
                        .fromJson(eventsJson, typeToken.getType());
                if (fetched != null) {
                    mAdapter.fillWithItems(fetched);
                    mAdapter.notifyDataSetChanged();
                }
            }

            setListAdapter(mAdapter);

            if (mAdapter.getCount() > 0) {
                getView().setPadding(0, 0, 0, 0);
            } else {
                getView().setPadding(10, 10, 10, 10);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((ActionBarActivity)getActivity()).getSupportActionBar()
                .setTitle(getString(R.string.live).toUpperCase());
    }

    @Override
    public void onStart() {
        super.onStart();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final FeedItem item = mAdapter.getItem(i);
                if (item != null) {
                    final Intent playVideoIntent = new Intent(getBaseActivity(),
                            VideoPlayerActivity.class);
                    playVideoIntent.putExtra(ARG_IS_LIVE, true);
                    playVideoIntent.putExtra(ARG_ITEM_JSON, GsonUtils.toJson(item));
                    playVideoIntent.putExtra(ARG_UP_TITLE, getString(R.string.live));
                    getBaseActivity().startActivity(playVideoIntent);
                }
            }
        });

        final int padding = (int) (applyDimension(COMPLEX_UNIT_DIP, 6.0f,
                getResources().getDisplayMetrics()) + 0.5f);
        getListView().setPadding(padding, padding, padding, padding);
        getListView().setDividerHeight(0);

        /*
         * Register the broadcast receiver with the parent activity
         */
        if (mRefreshLiveReceiver == null) {
            mRefreshLiveReceiver = new RefreshLiveBroadcastReceiver();
        }

        IntentFilter refreshFilter = new IntentFilter(LiveService.REFRESH_LIVE_UI_INTENT);
        getActivity().registerReceiver(mRefreshLiveReceiver, refreshFilter);

        final Intent startService = new Intent(getActivity(), LiveService.class);
        startService.setAction(LiveService.GET_LIVE_DATA_INTENT);
        getActivity().startService(startService);
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (mRefreshLiveReceiver != null) {
                /* Unregister the receiver when we pause the fragment */
                getActivity().unregisterReceiver(mRefreshLiveReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
