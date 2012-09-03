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
import java.util.List;

public class FavoritesMap implements Serializable {

    private List<FeedItem> articles;

    private List<FeedItem> photos;

    private List<FeedItem> videos;

    /**
     * @return articles
     */
    public List<FeedItem> getArticles() {
        return articles;
    }

    /**
     * @return this FavoritesMap
     */
    public FavoritesMap setArticles(final List<FeedItem> articles) {
        this.articles = articles;
        return this;
    }

    /**
     * @return photos
     */
    public List<FeedItem> getPhotos() {
        return photos;
    }

    /**
     * @return this FavoritesMap
     */
    public FavoritesMap setPhotos(final List<FeedItem> photos) {
        this.photos = photos;
        return this;
    }

    /**
     * @return videos
     */
    public List<FeedItem> getVideos() {
        return videos;
    }

    /**
     * @return this FavoritesMap
     */
    public FavoritesMap setVideos(final List<FeedItem> videos) {
        this.videos = videos;
        return this;
    }
}
