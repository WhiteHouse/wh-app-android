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

import com.google.gson.reflect.TypeToken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.services.FeedService;
import gov.whitehouse.ui.activities.BaseActivity;
import gov.whitehouse.ui.activities.app.ArticleViewerActivity;
import gov.whitehouse.ui.activities.app.VideoPlayerActivity;
import gov.whitehouse.ui.adapters.FavoritesListAdapter;
import gov.whitehouse.ui.adapters.FeedItemsListAdapter;
import gov.whitehouse.ui.fragments.BaseListFragment;
import gov.whitehouse.ui.loaders.FavoritesLoader;
import gov.whitehouse.ui.loaders.FeedReaderLoader;
import gov.whitehouse.utils.GATrackingManager;
import gov.whitehouse.utils.GsonUtils;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static gov.whitehouse.core.FeedItem.TYPE_VIDEO;
import static gov.whitehouse.services.FeedService.EXTRA_CACHED;
import static gov.whitehouse.services.FeedService.EXTRA_SERVER_ERROR;
import static gov.whitehouse.ui.fragments.app.ArticleViewerFragment.ARG_UP_TITLE;

/**
 * A Fragment to display a list of text-based articles from a feed.
 */
public class ArticleListFragment extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<List<FeedItem>> {

    public static final String ARG_FEED_TITLE = "feed_title";

    public static final String ARG_FEED_TYPE = "feed_type";

    public static final String ARG_FEED_URL = "feed_url";

    public static final String ARG_ITEM_JSON = "item_json";

    public static final int ARTICLE_TYPE_FEED = 0;

    public static final int ARTICLE_TYPE_FAVORITES = 1;

    private static String TAG = "ArticleListFragment";

    private ArticleFeedReceiver mArticleFeedReceiver;

    private BaseAdapter mAdapter;

    private List<FeedItem> mFeedItems;

    private String mFeedURL;

    private String mFeedTitle;

    private int mFeedType;

    private class ArticleFeedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /* Don't do anything if we can't get to our parent activity */
            if (isDetached() || isRemoving()) {
                return;
            }
            /* Make sure we're not displaying Favorites for some reason */
            if (mFeedType == ARTICLE_TYPE_FAVORITES) {
                return;
            }

            List<FeedItem> fetched = FeedService.loadFeedFromCache(getBaseActivity(), mFeedTitle);
            if (fetched != null) {
                mAdapter = new FeedItemsListAdapter(getBaseActivity());
                TypeToken<List<FeedItem>> typeToken = new TypeToken<List<FeedItem>>() {
                };

                mFeedItems = new ArrayList<FeedItem>();
                mFeedItems.addAll(fetched);
                ((FeedItemsListAdapter) mAdapter).fillWithItems(mFeedItems);
                mAdapter.notifyDataSetChanged();

                setListAdapter(mAdapter);
            }

            final boolean isCachedData = intent.getBooleanExtra(EXTRA_CACHED, false);
            if (!isCachedData) {
            }

            if (intent.getBooleanExtra(EXTRA_SERVER_ERROR, false)) {
                Toast.makeText(getBaseActivity(), "No network connection.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        setRetainInstance(true);

        final Bundle args = getActivity().getIntent().getExtras();
        if (args != null) {
            mFeedType = args.getInt(ARG_FEED_TYPE, ARTICLE_TYPE_FAVORITES);
            mFeedURL = args.getString(ARG_FEED_URL);
            mFeedTitle = args.getString(ARG_FEED_TITLE);
        } else {
            mFeedType = ARTICLE_TYPE_FAVORITES;
        }

        if (mFeedTitle == null) {
            mFeedTitle = getString(R.string.favorites);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mFeedTitle == null) {
            getActivity().setTitle("");
        } else {
            getActivity().setTitle(mFeedTitle.toUpperCase());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mFeedType == ARTICLE_TYPE_FAVORITES) {
            getLoaderManager().restartLoader(mFeedType, null, this);
        } else {
            /*
             * Register the broadcast receiver with the parent activity
             */
            if (mArticleFeedReceiver == null) {
                mArticleFeedReceiver = new ArticleFeedReceiver();
            }

            IntentFilter refreshFilter = new IntentFilter(FeedService.REFRESH_FEED_UI_INTENT);
            getActivity().registerReceiver(mArticleFeedReceiver, refreshFilter);

            final Intent startService = new Intent(getActivity(), FeedService.class);
            startService.putExtra(FeedService.ARG_FEED_TITLE, mFeedTitle);
            startService.putExtra(FeedService.ARG_FEED_URL, mFeedURL);
            startService.setAction(FeedService.GET_FEED_DATA_INTENT);
            getActivity().startService(startService);
        }

        final int padding = (int) (applyDimension(COMPLEX_UNIT_DIP, 6.0f,
                getResources().getDisplayMetrics()) + 0.5f);
        getListView().setPadding(padding, padding, padding, padding);
        getListView().setDividerHeight(0);
        getListView().setSelector(android.R.color.transparent);

        // show the scroll indicator on the outside...
        getListView().setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (mArticleFeedReceiver != null) {
                /* Unregister the receiver when we pause the fragment */
                getActivity().unregisterReceiver(mArticleFeedReceiver);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FeedItem item = mFeedItems.get(position);

        Bundle args = new Bundle();
        if (mFeedType == ARTICLE_TYPE_FAVORITES && item.getType() == TYPE_VIDEO) {
            args.putString(VideoPlayerActivity.ARG_ITEM_JSON, GsonUtils.toJson(item));
            args.putString(VideoPlayerActivity.ARG_UP_TITLE, getString(R.string.favorites));
            final Intent nextIntent = new Intent(getActivity(), VideoPlayerActivity.class);
            nextIntent.putExtras(args);
            startActivity(nextIntent);
            return;
        } else {
            args.putInt(ARG_FEED_TYPE, mFeedType);
            args.putString(ARG_UP_TITLE, mFeedTitle);
            args.putString(ARG_ITEM_JSON, GsonUtils.toJson(item));
            args.putString("title", item.getTitle());
            DateFormat formatter = DateFormat.getDateTimeInstance();
            args.putString("date", formatter.format(item.getPubDate()));
            args.putString("creator", item.getCreator());
            args.putString("description", item.getDescription());
            args.putString("url", item.getLink().toString());
        }

        final BaseActivity activity = (BaseActivity) getActivity();
        if (activity.isMultipaned()) {
            // track multi-pane article views here
            GATrackingManager.getInstance().track(activity.getTrackingPathComponent(), item.getTitle());

            ArticleViewerFragment detailsFragment = new ArticleViewerFragment();
            detailsFragment.setArguments(args);
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_container, detailsFragment).commit();
        } else {
            final Intent nextIntent = new Intent(getActivity(),
                    ArticleViewerActivity.class);
            nextIntent.putExtras(args);
            startActivity(nextIntent);
        }
    }

    @Override
    public Loader<List<FeedItem>> onCreateLoader(int i, Bundle bundle) {
        if (mFeedType == ARTICLE_TYPE_FAVORITES) {
            return new FavoritesLoader(getActivity());
        } else {
            return new FeedReaderLoader(getActivity(), URI.create(mFeedURL), mFeedTitle);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<FeedItem>> listLoader, List<FeedItem> feedItems) {
        mFeedItems = new ArrayList<FeedItem>();
        mFeedItems.addAll(feedItems);

        if (mFeedType == ARTICLE_TYPE_FAVORITES) {
            mAdapter = new FavoritesListAdapter(getBaseActivity());
            ((FavoritesListAdapter) mAdapter).fillWithItems(mFeedItems);
        } else {
            mAdapter = new FeedItemsListAdapter(getBaseActivity());
            ((FeedItemsListAdapter) mAdapter).fillWithItems(mFeedItems);
        }
        mAdapter.notifyDataSetChanged();

        setListAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<FeedItem>> listLoader) {
        mFeedItems.clear();
    }
}
