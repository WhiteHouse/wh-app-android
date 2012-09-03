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

package gov.whitehouse.services;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.xml.sax.SAXException;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedHandler;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.utils.GsonUtils;

public class FeedService extends IntentService {

    public static final String GET_FEED_DATA_INTENT = "whitehouse:get_feed_data_intent";

    public static final String REFRESH_FEED_UI_INTENT = "whitehouse:refresh_feed_ui_intent";

    public static final String EXTRA_CACHED = "whitehouse:feed_data_cached";

    public static final String EXTRA_JSON = "whitehouse:feed_data_json";

    public static final String EXTRA_SERVER_ERROR = "whitehouse:server_error";

    public static final String ARG_FEED_URL = "whitehouse:feed_url";

    public static final String ARG_FEED_TITLE = "whitehouse:feed_title";

    private static final String TAG = "whitehouse:FeedService";

    public FeedService() {
        super("FeedService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, ">> FeedService :: onHandleIntent()");
        if (intent.getAction().equals(GET_FEED_DATA_INTENT)) {
            String feedUrl = intent.getStringExtra(ARG_FEED_URL);
            String feedTitle = intent.getStringExtra(ARG_FEED_TITLE);

            if (feedUrl != null) {
                ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();

                /* Send any cached feed first */
                ArrayList<FeedItem> cachedItems = loadFeedFromCache(this, feedTitle);
                if (cachedItems != null) {
                    final Intent refreshIntent = new Intent(REFRESH_FEED_UI_INTENT);
                    refreshIntent.putExtra(EXTRA_CACHED, true);
                    sendBroadcast(refreshIntent);
                }

                /* ... then update the feed from the server */
                if (updateFeedFromServer(feedItems, feedUrl, feedTitle)) {
                    /*
                    * Send off a refresh broadcast to anything listening. Also
                    * includes the actual refreshed list of live events so
                    * BroadcastReceivers can handle the data straight away.
                    */
                    final Intent refreshIntent = new Intent(REFRESH_FEED_UI_INTENT);
                    refreshIntent.putExtra(EXTRA_CACHED, false);
                    sendBroadcast(refreshIntent);
                } else {
                    final Intent refreshIntent = new Intent(REFRESH_FEED_UI_INTENT);
                    refreshIntent.putExtra(EXTRA_SERVER_ERROR, true);
                    sendBroadcast(refreshIntent);
                }
            }
        }
    }

    public static ArrayList<FeedItem> loadFeedFromCache(final Context context,
            final String feedTitle) {
        File cacheFile = new File(context.getCacheDir(), "cached_" + feedTitle + "_feed.json");

        try {
            if (cacheFile == null || !cacheFile.exists() || !cacheFile.isFile()) {
                return null;
            } else {
                final ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();
                final FileInputStream fileIn = new FileInputStream(cacheFile);
                final InputStreamReader isr = new InputStreamReader(fileIn);
                TypeToken<ArrayList<FeedItem>> token = new TypeToken<ArrayList<FeedItem>>() {
                };
                ArrayList<FeedItem> list = GsonUtils.fromJson(isr, token.getType());
                if (list != null) {
                    feedItems.addAll(list);
                }
                return feedItems;
            }
        } catch (IOException e) {
            Log.d(TAG, "error reading feed");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (IllegalStateException e) {
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (JsonSyntaxException e) {
            Log.d(TAG, "whoops, bad json");
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    private boolean updateFeedFromServer(final ArrayList<FeedItem> feedItems, final String url,
            final String title) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            InputStream in;
            int status;

            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", getString(R.string.user_agent_string));

            in = conn.getInputStream();
            status = conn.getResponseCode();

            if (status < 400) {
                /* We should be good to go */
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                FeedHandler handler = new FeedHandler();

                parser.parse(in, handler);
                feedItems.addAll(handler.getFeedItems());

                for (FeedItem item : feedItems) {
                    if (item != null) {
                        item.setFeedTitle(title);
                    }
                }

                /* Now write the feed to cache */
                File cacheFile = new File(getCacheDir(), "cached_" + title + "_feed.json");
                if (cacheFile.exists() && cacheFile.isFile()) {
                    cacheFile.delete();
                }
                cacheFile.createNewFile();
                FileOutputStream cacheOutStream = new FileOutputStream(cacheFile);
                OutputStreamWriter streamWriter = new OutputStreamWriter(cacheOutStream);

                streamWriter.write(GsonUtils.toJson(feedItems));
                streamWriter.close();

                conn.disconnect();

                return true;
            }

            conn.disconnect();
        } catch (UnknownHostException e) {
        } catch (SAXException e) {
            Log.d(TAG, "failed to parse XML");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (IOException e) {
            Log.d(TAG, "error reading feed");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (IllegalStateException e) {
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (ParserConfigurationException e) {
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return false;
    }
}
