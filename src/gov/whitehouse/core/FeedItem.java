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

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedItem implements Serializable {

    public static final int TYPE_ARTICLE = 1;

    public static final int TYPE_PHOTO = 2;

    public static final int TYPE_VIDEO = 3;

    private int type;

    // not necessarily a guid, but the ID from the CMS system
    private String guid;

    private String feedTitle;

    private String title;

    private String creator;

    private String description;

    private Date pubDate;

    private URL link;

    private URL videoLink;

    // map sizes to URLs, so that we can pick the best size later
    private Map<Integer, URL> thumbnails = new HashMap<Integer, URL>();

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public URL getLink() {
        return link;
    }

    public void setLink(URL link) {
        this.link = link;
    }

    public URL getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(URL videoLink) {
        this.videoLink = videoLink;
    }

    /**
     * Set a thumbnail URL for the given width
     *
     * @param url   the URL of the thumbnail image
     * @param width the width of the image
     */
    public void addThumbnail(URL url, int width) {
        thumbnails.put(width, url);
    }

    /**
     * @param idealWidth the preferred width of the image
     * @return a URL for an image with a width closest to @param width
     */
    public URL getThumbnail(int idealWidth) {
        int closest = Integer.MAX_VALUE;
        for (Map.Entry<Integer, URL> pair : thumbnails.entrySet()) {
            int thisWidth = pair.getKey();
            int thisDiff = Math.abs(idealWidth - thisWidth);
            int closestDiff = Math.abs(closest - thisWidth);
            if (thisDiff < closestDiff) {
                closest = thisWidth;
            }
        }

        return thumbnails.get(closest);
    }

    public Boolean isYouTubeVideo() {
        if (getVideoLink() != null) {
            Pattern p = Pattern.compile("(www\\.)?((youtube(-nocookie)?)\\.com|youtu\\.be)");
            Matcher m = p.matcher(getVideoLink().getHost());
            return m.matches();
        }

        return false;
    }
}
