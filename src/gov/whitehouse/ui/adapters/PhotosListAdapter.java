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

package gov.whitehouse.ui.adapters;

import com.androidquery.AQuery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedItem;

public class PhotosListAdapter extends BaseAdapter {

    private static class ViewHolder {

        ImageView image;
    }

    private Context mContext;

    private ArrayList<FeedItem> mItems;

    public PhotosListAdapter(final Context context) {
        super();

        mContext = context;
        mItems = new ArrayList<FeedItem>();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public FeedItem getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FeedItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.photo_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.iv_thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AQuery aq = new AQuery(mContext);
        try {
            /*
             * Load the thumbnail asynchronously. We get a thumbnail of 280px in width because that
             * translates exactly to the 140dp we need on XHDPI devices.
             */
            aq.id(holder.image).image(item.getThumbnail(280).toString(), true, true, 280, 0, null,
                    AQuery.FADE_IN_NETWORK);
        } catch (NullPointerException e) {
            aq.id(holder.image).image(android.R.color.white);
        }

        return convertView;
    }

    /**
     * Replaces the existing items in the adapter with a given collection
     */
    public void fillWithItems(Collection<FeedItem> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    /**
     * Appends a given list of FeedItems to the adapter
     */
    public void appendWithItems(Collection<FeedItem> items) {
        mItems.addAll(items);
    }
}