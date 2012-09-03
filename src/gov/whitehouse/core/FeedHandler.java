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

package gov.whitehouse.core;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for parsing RSS feeds and extracting important parts into our app's
 * data model.
 */
public class FeedHandler extends DefaultHandler {

    private static String TAG = "FeedHandler";

    // not *that* NS...
    public static String NS_MEDIA = "http://search.yahoo.com/mrss/";

    public static String NS_DC = "http://purl.org/dc/elements/1.1/";

    public static String NS_FEEDBURNER = "http://rssnamespace.org/feedburner/ext/1.0";

    private FeedItem currentItem;

    private List<FeedItem> items;

    private StringBuilder text;

    @Override
    public void startDocument() throws SAXException {
        items = new ArrayList<FeedItem>();
    }

    @Override
    public void startElement(String ns, String localName, String qName,
            Attributes attributes) throws SAXException {
        //Log.v(TAG, String.format("start tag: %s; qName: %s; ns: %s", localName, qName, ns));

        text = new StringBuilder();

        if (localName.equals("item")) {
            currentItem = new FeedItem();
            items.add(currentItem);
        } else if (localName.equals("enclosure")) {
            final String videoUrlString = attributes.getValue("url");
            currentItem.setVideoLink(parseURL(videoUrlString));
        } else if (ns.equals(NS_MEDIA) && (localName.equals("content") || localName
                .equals("thumbnail"))) {
            String urlString = attributes.getValue("url");
            String widthString = attributes.getValue("width");
            /* Only add the thumbnail if it's URL and width are in-tact */
            if (!(urlString == null || widthString == null)) {
                currentItem.addThumbnail(parseURL(urlString), parseInt(widthString));
            }
        }
    }

    public int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public URL parseURL(String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            Log.d(TAG, "malformed URL: " + s);
            return null;
        }
    }

    @Override
    public void endElement(String ns, String localName, String qName) throws SAXException {
        if (currentItem == null) {
            return;
        }

        if (localName.equals("title")) {
            currentItem.setTitle(text.toString());
        } else if (localName.equals("description")) {
            currentItem.setDescription(text.toString());
        } else if (localName.equals("pubDate")) {
            // our RSS date format looks like "Tue, 10 Jul 2012 22:07:58 +0000"
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            try {
                currentItem.setPubDate(format.parse(text.toString()));
            } catch (ParseException e) {
                Log.d(TAG, "Could not parse feed item date");
            }
        } else if (localName.equals("link")) {
            currentItem.setLink(parseURL(text.toString()));
        } else if (ns.equals(NS_DC) && localName.equals("creator")) {
            currentItem.setCreator(text.toString());
        } else if (localName.equals("guid")) {
            currentItem.setGuid(text.toString());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        text.append(ch, start, length);
    }

    public List<FeedItem> getFeedItems() {
        return items;
    }
}
