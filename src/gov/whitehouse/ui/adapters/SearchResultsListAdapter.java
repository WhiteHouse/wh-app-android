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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import gov.whitehouse.R;
import gov.whitehouse.core.SearchResult;

import static android.graphics.Color.TRANSPARENT;
import static android.view.View.GONE;

public class SearchResultsListAdapter extends BaseAdapter {

    private static class ViewHolder {

        TextView date;

        ImageView thumbnail;

        LinearLayout metaholder;

        TextView title;

        TextView time;
    }

    private Context mContext;

    private ArrayList<SearchResult> mItems;

    public SearchResultsListAdapter(final Context context) {
        super();

        mContext = context;
        mItems = new ArrayList<SearchResult>();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public SearchResult getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SearchResult item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.search_row, null);
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

        /*
        final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        final SimpleDateFormat date = new SimpleDateFormat("MMM d, yyyy");
        final SimpleDateFormat time = new SimpleDateFormat("h:mm a");

        Date fetchedDate, lastDate;
        String publishDate, publishTime;
        try {
            fetchedDate = parser.parse(item.getPublishedAt());
            publishDate = date.format(fetchedDate);
            publishTime = time.format(fetchedDate);
            if (position > 0) {
                lastDate = parser.parse(getItem(position - 1).getPublishedAt());
            } else {
                lastDate = null;
            }
        } catch (ParseException e) {
            publishDate = new Date().toString();
            publishTime = new Date().toString();
            fetchedDate = new Date();
            lastDate = new Date();
            e.printStackTrace();
        }

        holder.date.setText(publishDate);
        holder.time.setText(publishTime);

        if (position > 0) {
            if (!DateUtils.isSameDay(fetchedDate, lastDate)) {
                holder.date.setVisibility(VISIBLE);
            } else {
                holder.date.setVisibility(GONE);
            }
        } else if (position == 0) {
            holder.date.setVisibility(VISIBLE);
        } else {
            holder.date.setVisibility(GONE);
        }

        if (holder.date.getVisibility() == VISIBLE) {
            if (DateUtils.isToday(fetchedDate)) {
                holder.date.setText(R.string.today);
            } else if (DateUtils.isYesterday(fetchedDate)) {
                holder.date.setText(R.string.yesterday);
            }
        }

        */

        holder.date.setVisibility(GONE);
        holder.time.setVisibility(GONE);

        holder.thumbnail.setVisibility(GONE);
        holder.metaholder.setBackgroundColor(TRANSPARENT);

        holder.title.setText(item.getTitle());

        return convertView;
    }

    /**
     * Replaces the existing items in the adapter with a given collection
     */
    public void fillWithItems(Collection<SearchResult> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    /**
     * Appends a given list of SearchResults to the adapter
     */
    public void appendWithItems(Collection<SearchResult> items) {
        mItems.addAll(items);
    }
}