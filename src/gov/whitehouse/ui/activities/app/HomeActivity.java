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

package gov.whitehouse.ui.activities.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.core.DashboardItem;
import gov.whitehouse.ui.activities.BaseActivity;
import gov.whitehouse.ui.loaders.DashboardLoader;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gov.whitehouse.core.DashboardItem.VIEW_TYPE_ARTICLE_LIST;
import static gov.whitehouse.core.DashboardItem.VIEW_TYPE_LIVE;
import static gov.whitehouse.core.DashboardItem.VIEW_TYPE_PHOTO_GALLERY;
import static gov.whitehouse.core.DashboardItem.VIEW_TYPE_VIDEO_GALLERY;
import static gov.whitehouse.ui.activities.app.PhotoGalleryActivity.ARG_GALLERY_TITLE;
import static gov.whitehouse.ui.activities.app.PhotoGalleryActivity.ARG_PHOTO_FEED_URL;
import static gov.whitehouse.ui.fragments.app.ArticleListFragment.ARG_FEED_TITLE;
import static gov.whitehouse.ui.fragments.app.ArticleListFragment.ARG_FEED_TYPE;
import static gov.whitehouse.ui.fragments.app.ArticleListFragment.ARG_FEED_URL;
import static gov.whitehouse.ui.fragments.app.ArticleListFragment.ARTICLE_TYPE_FAVORITES;
import static gov.whitehouse.ui.fragments.app.ArticleListFragment.ARTICLE_TYPE_FEED;
import static gov.whitehouse.ui.fragments.app.VideoListFragment.ARG_VIDEO_FEED_URL;
import static gov.whitehouse.ui.fragments.app.VideoListFragment.ARG_VIDEO_LIST_TITLE;

public class HomeActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<List<DashboardItem>> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_home);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<List<DashboardItem>> onCreateLoader(int i, Bundle bundle) {
        return new DashboardLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<DashboardItem>> listLoader,
            List<DashboardItem> dashboardItems) {
        ArrayList<DashboardItem> items = new ArrayList<DashboardItem>();
        if (dashboardItems != null) {
            items.addAll(dashboardItems);
        }

        if (items.size() > 0) {
            final DashboardItem first = items.get(0);
            if (first.getViewType().equals(VIEW_TYPE_ARTICLE_LIST)) {
                final Intent intent = new Intent(this, ArticleListActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ARG_FEED_TYPE, ARTICLE_TYPE_FEED);
                intent.putExtra(ARG_FEED_URL, first.getFeedUrl());
                intent.putExtra(ARG_FEED_TITLE, first.getTitle());
                startActivity(intent);
                finish();
            } else if (first.getViewType().equals(VIEW_TYPE_PHOTO_GALLERY)) {
                final Intent intent = new Intent(this, PhotoGalleryActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ARG_GALLERY_TITLE, first.getTitle());
                intent.putExtra(ARG_PHOTO_FEED_URL, first.getFeedUrl());
                startActivity(intent);
                finish();
            } else if (first.getViewType().equals(VIEW_TYPE_VIDEO_GALLERY)) {
                final Intent intent = new Intent(this, VideoGalleryActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ARG_VIDEO_LIST_TITLE, first.getTitle());
                intent.putExtra(ARG_VIDEO_FEED_URL, first.getFeedUrl());
                startActivity(intent);
                finish();
            } else if (first.getViewType().equals(VIEW_TYPE_LIVE)) {
                final Intent intent = new Intent(this, LiveFeedActivity.class);
                startActivity(intent);
                finish();
            } else if (getString(R.string.favorites).equals(first.getTitle())) {
                final Intent intent = new Intent(this, FavoritesActivity.class);
                intent.setClass(this, FavoritesActivity.class);
                intent.putExtra(ARG_FEED_TYPE, ARTICLE_TYPE_FAVORITES);
                intent.putExtra(ARG_FEED_TITLE, getString(R.string.favorites));
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<DashboardItem>> listLoader) {
    }

    @Override
    public String getTrackingPathComponent() {
        return "LAUNCH";
    }
}
