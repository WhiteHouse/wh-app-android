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

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import gov.whitehouse.R;
import gov.whitehouse.core.FavoritesMap;
import gov.whitehouse.core.FeedItem;

import static android.widget.Toast.LENGTH_SHORT;

public class FavoritesUtils {

    public static final String FAVORITE_ARTICLES = "articles";

    public static final String FAVORITE_PHOTOS = "photos";

    public static final String FAVORITE_VIDEOS = "videos";

    public static final String FAVORITES_FILE = "favorites_map.json";

    public static FileInputStream getInputStream(final Context context) throws
            FileNotFoundException {
        return context.openFileInput(FAVORITES_FILE);
    }

    public static FileOutputStream getOutputStream(final Context context) {
        try {
            return context.openFileOutput(FAVORITES_FILE, 0);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static File getFileDescriptor(final Context context) {
        return context.getFileStreamPath(FAVORITES_FILE);
    }

    public static boolean isFavorited(final Context context, final FeedItem item) {
        try {
            FavoritesMap map = GsonUtils
                    .fromJson(new InputStreamReader(getInputStream(context)), FavoritesMap.class);
            if (map == null) {
                return false;
            }

            if (map.getArticles() != null) {
                final ArrayList<FeedItem> articles = new ArrayList<FeedItem>();
                articles.addAll(map.getArticles());
                for (FeedItem i : articles) {
                    if (i.getPubDate().equals(item.getPubDate()) && i.getLink()
                            .equals(item.getLink())) {
                        return true;
                    }
                }
            }
            if (map.getPhotos() != null) {
                final ArrayList<FeedItem> photos = new ArrayList<FeedItem>();
                photos.addAll(map.getPhotos());
                for (FeedItem i : photos) {
                    if (i.getPubDate().equals(item.getPubDate()) && i.getLink()
                            .equals(item.getLink())) {
                        return true;
                    }
                }
            }
            if (map.getVideos() != null) {
                final ArrayList<FeedItem> videos = new ArrayList<FeedItem>();
                videos.addAll(map.getVideos());
                for (FeedItem i : videos) {
                    if (i.getPubDate().equals(item.getPubDate()) && i.getLink()
                            .equals(item.getLink())) {
                        return true;
                    }
                }
            }

        } catch (FileNotFoundException e) {
            return false;
        }

        return false;
    }

    public static void addToFavorites(final Activity context, final String type,
            final FeedItem item) {
        FavoritesMap map = null;
        try {
            map = GsonUtils
                    .fromJson(new InputStreamReader(getInputStream(context)), FavoritesMap.class);
        } catch (FileNotFoundException e) {
            map = null;
        }

        if (map == null) {
            map = new FavoritesMap();
        }

        if (FAVORITE_ARTICLES.equals(type)) {
            if (map.getArticles() == null) {
                map.setArticles(new ArrayList<FeedItem>());
            }
            map.getArticles().add(item);
        } else if (FAVORITE_PHOTOS.equals(type)) {
            if (map.getPhotos() == null) {
                map.setPhotos(new ArrayList<FeedItem>());
            }
            map.getPhotos().add(item);
        } else if (FAVORITE_VIDEOS.equals(type)) {
            if (map.getVideos() == null) {
                map.setVideos(new ArrayList<FeedItem>());
            }
            map.getVideos().add(item);
        }

        OutputStreamWriter out = new OutputStreamWriter(getOutputStream(context));
        try {
            out.write(GsonUtils.toJson(map));
            out.close();

            Toast.makeText(context, R.string.favorites_added, LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeFromFavorites(final Activity context, final String type,
            final FeedItem item) {
        FavoritesMap map = null;
        try {
            map = GsonUtils
                    .fromJson(new InputStreamReader(getInputStream(context)), FavoritesMap.class);
        } catch (FileNotFoundException e) {
            map = null;
        }

        if (map == null) {
            return;
        }

        if (FAVORITE_ARTICLES.equals(type) && (map.getArticles() == null || map.getArticles()
                .size() < 1)) {
            return;
        } else if (FAVORITE_PHOTOS.equals(type) && (map.getPhotos() == null || map.getPhotos()
                .size() < 1)) {
            return;
        } else if (FAVORITE_VIDEOS.equals(type) && (map.getVideos() == null || map.getVideos()
                .size() < 1)) {
            return;
        }

        final LinkedList llist;
        if (FAVORITE_ARTICLES.equals(type)) {
            llist = new LinkedList(map.getArticles());
        } else if (FAVORITE_PHOTOS.equals(type)) {
            llist = new LinkedList(map.getPhotos());
        } else if (FAVORITE_VIDEOS.equals(type)) {
            llist = new LinkedList(map.getVideos());
        } else {
            llist = new LinkedList();
        }

        final int len = llist.size();

        for (Iterator itr = llist.iterator(); itr.hasNext(); ) {
            FeedItem i = (FeedItem) itr.next();
            if (i.getPubDate().equals(item.getPubDate()) && i.getLink().equals(item.getLink())) {
                itr.remove();
            }
        }

        if (FAVORITE_ARTICLES.equals(type)) {
            map.getArticles().clear();
            map.getArticles().addAll(llist);
        } else if (FAVORITE_PHOTOS.equals(type)) {
            map.getPhotos().clear();
            map.getPhotos().addAll(llist);
        } else if (FAVORITE_VIDEOS.equals(type)) {
            map.getVideos().clear();
            map.getVideos().addAll(llist);
        }

        OutputStreamWriter out = new OutputStreamWriter(getOutputStream(context));
        try {
            out.write(GsonUtils.toJson(map));
            out.close();

            Toast.makeText(context, R.string.favorites_removed, LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
