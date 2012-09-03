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
import java.util.List;

public class SearchResult implements Serializable {

    private String title;

    @SerializedName("deepLinks")
    private List<DeepLink> deepLinks;

    @SerializedName("cacheUrl")
    private String cacheUrl;

    private String content;

    @SerializedName("unescapedUrl")
    private String unescapedUrl;

    @SerializedName("publishedAt")
    private String publishedAt;

    public static class DeepLink implements Serializable {

        @SerializedName("Title")
        private String title;

        @SerializedName("Url")
        private String url;

        /**
         * @return title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @return this DeepLink
         */
        public DeepLink setTitle(final String title) {
            this.title = title;
            return this;
        }

        /**
         * @return url
         */
        public String getUrl() {
            return url;
        }

        /**
         * @return this DeepLink
         */
        public DeepLink setUrl(final String url) {
            this.url = url;
            return this;
        }
    }

    /**
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return this SearchResult
     */
    public SearchResult setTitle(final String title) {
        this.title = title;
        return this;
    }

    /**
     * @return deepLinks
     */
    public List<DeepLink> getDeepLinks() {
        return deepLinks;
    }

    /**
     * @return this SearchResult
     */
    public SearchResult setDeepLinks(final List<DeepLink> deepLinks) {
        this.deepLinks = deepLinks;
        return this;
    }

    /**
     * @return cacheUrl;
     */
    public String getCacheUrl() {
        return cacheUrl;
    }

    /**
     * @return this SearchResult
     */
    public SearchResult setCacheUrl(final String cacheUrl) {
        this.cacheUrl = cacheUrl;
        return this;
    }

    /**
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * @return this SearchResult
     */
    public SearchResult setContent(final String content) {
        this.content = content;
        return this;
    }

    /**
     * @return unescapedUrl
     */
    public String getUnescapedUrl() {
        return unescapedUrl;
    }

    /**
     * @return this SearchResult
     */
    public SearchResult setUnescapedUrl(final String unescapedUrl) {
        this.unescapedUrl = unescapedUrl;
        return this;
    }

    /**
     * @return publishedAt
     */
    public String getPublishedAt() {
        return publishedAt;
    }

    /**
     * @return this SearchResult
     */
    public SearchResult setPublishedAt(final String publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }
}
