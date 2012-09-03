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

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.ui.activities.BaseActivity;
import gov.whitehouse.ui.activities.app.VideoGalleryActivity;
import gov.whitehouse.utils.DateUtils;

import static android.graphics.Color.TRANSPARENT;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class FeedItemsListAdapter extends BaseAdapter {

    private static class ViewHolder {

        TextView date;

        ImageView thumbnail;

        LinearLayout metaholder;

        TextView title;

        TextView time;
    }

    private BaseActivity mContext;

    private ArrayList<FeedItem> mItems;

    public FeedItemsListAdapter(final BaseActivity context) {
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

    public int determineDateVisibility(final int position, final DisplayMetrics dm,
            final float widthThreshold) {
        if (position == 0) {
            return VISIBLE;
        } else if (position > 0 && position < getCount()) {
            final FeedItem item = getItem(position);
            final FeedItem lastItem = getItem(position - 1);
            final boolean sameAsLast = DateUtils
                    .isSameDay(item.getPubDate(), lastItem.getPubDate());
            boolean sameAsNext = false;
            if (position + 1 < getCount()) {
                sameAsNext = DateUtils
                        .isSameDay(item.getPubDate(), getItem(position + 1).getPubDate());
            }
            if (!sameAsLast) {
                return VISIBLE;
            } else if (mContext instanceof VideoGalleryActivity &&
                    (mContext.isMultipaned() || dm.widthPixels >= widthThreshold)) {
                final boolean rightSide = (position + 1) % 2 == 0;
                if (rightSide
                        && determineDateVisibility(position - 1, dm, widthThreshold) == VISIBLE) {
                    return INVISIBLE;
                } else if (!rightSide && !sameAsNext) {
                    return INVISIBLE;
                }
            }
        }

        return GONE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FeedItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.feedrow, null);
            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.tv_date);
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.iv_thumbnail);
            holder.metaholder = (LinearLayout) convertView.findViewById(R.id.ll_metaholder);
            holder.title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.time = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SimpleDateFormat date = new SimpleDateFormat("MMM d, yyyy");
        final SimpleDateFormat time = new SimpleDateFormat("h:mm a");

        holder.date.setText(date.format(item.getPubDate()));
        holder.time.setText(time.format(item.getPubDate()));

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        float widthThreshold = TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500.0f, dm);

        holder.date.setVisibility(determineDateVisibility(position, dm, widthThreshold));

        if (holder.date.getVisibility() != GONE) {
            if (DateUtils.isToday(item.getPubDate())) {
                holder.date.setText(R.string.today);
            } else if (DateUtils.isYesterday(item.getPubDate())) {
                holder.date.setText(R.string.yesterday);
            }
        }

        final int widthPixels = dm.widthPixels;
        final URL thumbnailURL = item.getThumbnail(widthPixels);
        if (thumbnailURL != null) {
            /*
             * Load the thumbnail asynchronously. We get a thumbnail of 320px in width because that
             * translates exactly to the 160dp we need on XHDPI devices.
             */
            final AQuery aq = new AQuery(mContext);
            aq.id(holder.thumbnail).image(thumbnailURL.toString(), true, true, widthPixels, 0, null,
                    AQuery.FADE_IN_NETWORK);

            holder.thumbnail.setVisibility(VISIBLE);
            holder.metaholder.setBackgroundColor(
                    mContext.getResources().getColor(R.color.feed_meta_dark_background));
            holder.title.setTextColor(mContext.getResources().getColor(R.color.wh_blue_lighter));
        } else {
            holder.thumbnail.setVisibility(GONE);
            holder.metaholder.setBackgroundColor(TRANSPARENT);
            holder.title.setTextColor(mContext.getResources().getColor(R.color.wh_blue));
        }

        holder.title.setText(item.getTitle());

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