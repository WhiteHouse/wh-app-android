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

import org.xml.sax.SAXException;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedHandler;
import gov.whitehouse.core.FeedItem;

public class FeedReaderLoader extends AsyncLoader<List<FeedItem>> {

    private static final String TAG = "FeedReaderLoader";

    private Activity mActivity;

    private URI mFeedUri;

    private String mFeedTitle;

    public FeedReaderLoader(Activity context, URI feedUri, String feedTitle) {
        super(context);
        mActivity = context;
        mFeedUri = feedUri;
        mFeedTitle = feedTitle;
    }

    @Override
    public List<FeedItem> loadInBackground() {
        Log.d(TAG, "loading URI: " + mFeedUri);
        try {
            HttpURLConnection conn = (HttpURLConnection) mFeedUri.toURL().openConnection();
            InputStream in;
            int status;

            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", getContext().getString(R.string.user_agent_string));

            in = conn.getInputStream();
            status = conn.getResponseCode();

            if (status < 400) {
                /* We should be good to go */
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                FeedHandler handler = new FeedHandler();

                parser.parse(in, handler);
                final ArrayList<FeedItem> items = new ArrayList<FeedItem>();
                items.addAll(handler.getFeedItems());

                for (FeedItem item : items) {
                    if (item != null) {
                        item.setFeedTitle(mFeedTitle);
                    }
                }

                return handler.getFeedItems();
            }

            conn.disconnect();
        } catch (UnknownHostException e) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "No network connection.", Toast.LENGTH_SHORT).show();
                }
            });
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

        /* If anything, return an empty list */
        return new ArrayList<FeedItem>();
    }
}
