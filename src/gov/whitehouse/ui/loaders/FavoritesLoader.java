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

package gov.whitehouse.ui.loaders;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.whitehouse.core.FavoritesMap;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.utils.FavoritesUtils;
import gov.whitehouse.utils.GsonUtils;

import static gov.whitehouse.core.FeedItem.TYPE_ARTICLE;
import static gov.whitehouse.core.FeedItem.TYPE_PHOTO;
import static gov.whitehouse.core.FeedItem.TYPE_VIDEO;

public class FavoritesLoader extends AsyncLoader<List<FeedItem>> {

    private static final String TAG = "FavoritesLoader";

    public FavoritesLoader(Context context) {
        super(context);
    }

    @Override
    public List<FeedItem> loadInBackground() {
        try {
            final InputStreamReader isr = new InputStreamReader(
                    FavoritesUtils.getInputStream(getContext()));
            FavoritesMap map = GsonUtils.fromJson(isr, FavoritesMap.class);
            ArrayList<FeedItem> list = new ArrayList<FeedItem>();

            if (map.getArticles() != null) {
                final ArrayList<FeedItem> articles = new ArrayList<FeedItem>();
                articles.addAll(map.getArticles());
                final int len = articles.size();
                for (int i = 0; i < len; i++) {
                    final FeedItem item = articles.get(i);
                    item.setType(TYPE_ARTICLE);
                    list.add(item);
                }
            }
            if (map.getPhotos() != null) {
                final ArrayList<FeedItem> photos = new ArrayList<FeedItem>();
                photos.addAll(map.getPhotos());
                final int len = photos.size();
                for (int i = 0; i < len; i++) {
                    final FeedItem item = photos.get(i);
                    item.setType(TYPE_PHOTO);
                    list.add(item);
                }
            }
            if (map.getVideos() != null) {
                final ArrayList<FeedItem> videos = new ArrayList<FeedItem>();
                videos.addAll(map.getVideos());
                final int len = videos.size();
                for (int i = 0; i < len; i++) {
                    final FeedItem item = videos.get(i);
                    item.setType(TYPE_VIDEO);
                    list.add(item);
                }
            }

            Collections.sort(list, new Comparator<FeedItem>() {
                @Override
                public int compare(FeedItem feedItem, FeedItem feedItem1) {
                    if (feedItem.getFeedTitle() != null && feedItem1.getFeedTitle() != null) {
                        return feedItem.getFeedTitle().compareTo(feedItem1.getFeedTitle());
                    } else {
                        return 1;
                    }
                }
            });

            return list;
        } catch (NullPointerException e) {
            return new ArrayList<FeedItem>();
        } catch (FileNotFoundException e) {
            return new ArrayList<FeedItem>();
        }
    }
}
