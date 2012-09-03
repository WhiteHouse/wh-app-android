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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * I represent sections/feeds in our app. This class is typically instantiated by parsing a remote
 * JSON configuration file. DO NOT remove "unused" setters in this class, as they are used when
 * reading the serialized representation.
 */
public class DashboardItem implements Serializable {
    // feed type values
    public static final String VIEW_TYPE_ARTICLE_LIST = "article-list";
    public static final String VIEW_TYPE_PHOTO_GALLERY = "photo-gallery";
    public static final String VIEW_TYPE_VIDEO_GALLERY = "video-gallery";
    public static final String VIEW_TYPE_LIVE = "live";

    private String title;

    // these annotations are for JSON reading
    @SerializedName("view-type")
    private String viewType;

    @SerializedName("feed-url")
    private String feedUrl;

    @SerializedName("feed-id")
    private String mFeedId;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getFeedId() {
        if (mFeedId == null) {
            return getTitle();
        }

        return mFeedId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFeedId(String feedId) {
        mFeedId = feedId;
    }

    public String getViewType() {
        return viewType;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setViewType(final String viewType) {
        this.viewType = viewType;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFeedUrl(final String feedUrl) {
        this.feedUrl = feedUrl;
    }
}
