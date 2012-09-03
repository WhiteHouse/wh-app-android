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

package gov.whitehouse.utils;

import android.content.Context;
import android.content.Intent;

import gov.whitehouse.R;
import gov.whitehouse.core.DashboardItem;
import gov.whitehouse.ui.activities.app.ArticleListActivity;
import gov.whitehouse.ui.activities.app.FavoritesActivity;
import gov.whitehouse.ui.activities.app.LiveFeedActivity;
import gov.whitehouse.ui.activities.app.PhotoGalleryActivity;
import gov.whitehouse.ui.activities.app.VideoGalleryActivity;
import gov.whitehouse.ui.fragments.app.ArticleListFragment;
import gov.whitehouse.ui.fragments.app.VideoListFragment;

public class DashboardItemUtils {

    public static Intent createIntent(Context context, DashboardItem item) {
        final Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (DashboardItem.VIEW_TYPE_ARTICLE_LIST.equals(item.getViewType())) {
            intent.setClass(context, ArticleListActivity.class);
            intent.putExtra(ArticleListFragment.ARG_FEED_TITLE, item.getTitle());
            intent.putExtra(ArticleListFragment.ARG_FEED_TYPE,
                    ArticleListFragment.ARTICLE_TYPE_FEED);
            intent.putExtra(ArticleListFragment.ARG_FEED_URL, item.getFeedUrl());
        } else if (DashboardItem.VIEW_TYPE_PHOTO_GALLERY.equals(item.getViewType())) {
            intent.setClass(context, PhotoGalleryActivity.class);
            intent.putExtra(PhotoGalleryActivity.ARG_GALLERY_TITLE, item.getTitle());
            intent.putExtra(PhotoGalleryActivity.ARG_PHOTO_FEED_URL, item.getFeedUrl());
        } else if (DashboardItem.VIEW_TYPE_VIDEO_GALLERY.equals(item.getViewType())) {
            intent.setClass(context, VideoGalleryActivity.class);
            intent.putExtra(VideoListFragment.ARG_VIDEO_FEED_URL, item.getFeedUrl());
            intent.putExtra(VideoListFragment.ARG_VIDEO_LIST_TITLE, item.getTitle());
        } else if (DashboardItem.VIEW_TYPE_LIVE.equals(item.getViewType())) {
            intent.setClass(context, LiveFeedActivity.class);
        } else if (context.getString(R.string.favorites).equals(item.getTitle())) {
            intent.setClass(context, FavoritesActivity.class);
            intent.putExtra(ArticleListFragment.ARG_FEED_TYPE,
                    ArticleListFragment.ARTICLE_TYPE_FAVORITES);
            intent.putExtra(ArticleListFragment.ARG_FEED_TITLE,
                    context.getString(R.string.favorites));
        }

        return intent;
    }
}