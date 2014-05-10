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
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static gov.whitehouse.ui.activities.app.VideoPlayerActivity.ARG_ITEM_JSON;
import static gov.whitehouse.ui.activities.app.VideoPlayerActivity.ARG_UP_TITLE;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.ui.activities.app.VideoPlayerActivity;
import gov.whitehouse.ui.adapters.FeedItemsListAdapter;
import gov.whitehouse.ui.fragments.BaseFragment;
import gov.whitehouse.ui.loaders.FeedReaderLoader;
import gov.whitehouse.utils.GsonUtils;

import java.net.URI;
import java.util.List;

import gov.whitehouse.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

public class VideoListFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<List<FeedItem>> {

    public static final String ARG_VIDEO_LIST_TITLE = "video_list_title";

    public static final String ARG_VIDEO_FEED_URL = "video_feed_url";

    private ProgressBar mProgressBar;

    private GridView mGridView;

    private FeedItemsListAdapter mAdapter;

    private String mTitle;

    private String mVideosUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.videos_fragment, container, false);

        mGridView = (GridView) v.findViewById(R.id.gv_videos);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getActivity().getIntent().getExtras();
        if (args != null) {
            mTitle = args.getString(ARG_VIDEO_LIST_TITLE);
            mVideosUrl = args.getString(ARG_VIDEO_FEED_URL);
        }

        if (mTitle == null) {
            mTitle = getString(R.string.videos);
        }
        if (mVideosUrl == null) {
            mVideosUrl = getString(R.string.video_feed_url);
        }

        ActionBarActivity activity = (ActionBarActivity)getActivity();
        String title = mTitle.toUpperCase();
        activity.getSupportActionBar().setTitle(title);
        activity.setTitle(title);

        mGridView.setVisibility(GONE);
        mProgressBar.setVisibility(VISIBLE);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        final int padding = (int) (applyDimension(COMPLEX_UNIT_DIP, 6.0f,
                getResources().getDisplayMetrics()) + 0.5f);
        mGridView.setPadding(padding, padding, padding, padding);
        mGridView.setSelector(android.R.color.transparent);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /*
                 * When a video item is clicked, send off an intent to play it.
                 */
                final FeedItem item = mAdapter.getItem(i);
                final Intent intent;

                if (item.isYouTubeVideo()) {
                    Uri uri = Uri.parse(item.getVideoLink().toString());
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                } else {
                    intent = new Intent(getActivity(), VideoPlayerActivity.class);
                    intent.putExtra(ARG_ITEM_JSON, GsonUtils.toJson(item));
                    intent.putExtra(ARG_UP_TITLE, mTitle);
                }

                getActivity().startActivity(intent);
            }
        });
    }

    @Override
    public Loader<List<FeedItem>> onCreateLoader(int i, Bundle bundle) {
        return new FeedReaderLoader(getActivity(), URI.create(mVideosUrl), mTitle);
    }

    @Override
    public void onLoadFinished(Loader<List<FeedItem>> listLoader, List<FeedItem> feedItems) {
        mAdapter = new FeedItemsListAdapter(getBaseActivity());
        mAdapter.fillWithItems(feedItems);
        mAdapter.notifyDataSetChanged();

        mGridView.setAdapter(mAdapter);

        mProgressBar.setVisibility(GONE);
        mGridView.setVisibility(VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<List<FeedItem>> listLoader) {
    }
}
