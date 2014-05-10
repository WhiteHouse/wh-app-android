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

package gov.whitehouse.ui.fragments.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.ui.activities.app.PhotoGalleryActivity;
import gov.whitehouse.ui.adapters.PhotosListAdapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static gov.whitehouse.ui.activities.app.PhotoGalleryActivity.BasePhotoFragment;

public class
        PhotosFragment extends BasePhotoFragment
        implements AdapterView.OnItemClickListener, PhotoGalleryActivity.IPhotosCallbacks {

    private ProgressBar mProgress;

    private GridView mGridView;

    private PhotosListAdapter mAdapter;

    public PhotosFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.photos_fragment, container, false);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        mGridView = (GridView) v.findViewById(R.id.gv_photos);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mGridView.setVisibility(GONE);
        mProgress.setVisibility(VISIBLE);

        mGridView.setOnItemClickListener(this);
        mGridView.setSelector(R.drawable.feed_item_selector);

        if (getPhotoActivity().getPhotoItems() == null) {
            getPhotoActivity().getSupportLoaderManager().initLoader(0, null, getPhotoActivity());
        } else {
            onNewPhotos(getPhotoActivity().getPhotoItems());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getPhotoActivity().getDrawerGarment().setDrawerEnabled(true);
        getPhotoActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getPhotoActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onNewPhotos(List<FeedItem> photos) {
        mAdapter = new PhotosListAdapter(getActivity());
        mAdapter.fillWithItems(photos);
        mGridView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mProgress.setVisibility(GONE);
        mGridView.setVisibility(VISIBLE);
    }

    public GridView getGridView() {
        return mGridView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        getPhotoActivity().showDetailWithSelection(i);
    }
}