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

package gov.whitehouse.ui.activities.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.ui.activities.BaseDashboardActivity;
import gov.whitehouse.ui.fragments.BaseFragment;
import gov.whitehouse.ui.fragments.app.PhotoDetailFragment;
import gov.whitehouse.ui.fragments.app.PhotosFragment;
import gov.whitehouse.ui.loaders.FeedReaderLoader;
import gov.whitehouse.utils.GATrackingManager;

public class PhotoGalleryActivity extends BaseDashboardActivity
        implements LoaderManager.LoaderCallbacks<List<FeedItem>> {

    public static final int NO_SELECTION = -1;

    public static final String ARG_GALLERY_TITLE = "gallery_title";

    public static final String ARG_PHOTO_FEED_URL = "photo_feed_url";

    private static final int MAX_IMAGE_COUNT = 32;

    private int mCurrentSelection = NO_SELECTION;

    private ArrayList<FeedItem> mPhotoItems;

    private String mFeedUrl;

    private String mGalleryTitle;

    private PhotoDataFragment mDataFragment;

    private PhotosFragment mPhotosFragment;

    private PhotoDetailFragment mPhotoDetailFragment;

    public interface IPhotosCallbacks {

        public void onNewPhotos(List<FeedItem> photos);
    }

    public abstract static class BasePhotoFragment extends BaseFragment
            implements IPhotosCallbacks {

        public PhotoGalleryActivity getPhotoActivity() {
            return (PhotoGalleryActivity) getActivity();
        }
    }

    public static class PhotoDataFragment extends BaseFragment {

        public ArrayList<FeedItem> photos;

        public PhotoDataFragment() {
            setRetainInstance(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("whitehouse", ">> onCreate()");

        if (savedInstanceState != null) {
            setCurrentSelection(savedInstanceState.getInt("current", NO_SELECTION));
        }

        mDataFragment = (PhotoDataFragment) getSupportFragmentManager()
                .findFragmentByTag(PhotoDataFragment.class.getName());
        if (mDataFragment == null) {
            mDataFragment = new PhotoDataFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(mDataFragment, PhotoDataFragment.class.getName()).commit();
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mFeedUrl = bundle.getString(ARG_PHOTO_FEED_URL);
            mGalleryTitle = bundle.getString(ARG_GALLERY_TITLE);
        }

        if (mFeedUrl == null || mFeedUrl.equals("")) {
            mFeedUrl = getString(R.string.photo_feed_url);
        }
        if (mGalleryTitle == null || mGalleryTitle.equals("")) {
            mGalleryTitle = getString(R.string.photos);
        }

        String displayTitle = mGalleryTitle.toUpperCase();
        getSupportActionBar().setTitle(displayTitle);
        setTitle(displayTitle);

        setOnBackPressedHandler(new OnBackPressedHandler() {
            @Override
            public boolean handleBackPress() {
                if (mCurrentSelection != NO_SELECTION) {
                    mCurrentSelection = NO_SELECTION;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (mPhotosFragment == null) {
            mPhotosFragment = new PhotosFragment();
        }

        ft.replace(R.id.main_container, mPhotosFragment, PhotosFragment.class.getName());

        ft.commit();

        if (mCurrentSelection > NO_SELECTION) {
            showDetailWithSelection(mCurrentSelection);
        }
    }

    @Override
    public Loader<List<FeedItem>> onCreateLoader(int i, Bundle bundle) {
        return new FeedReaderLoader(this, URI.create(mFeedUrl), mGalleryTitle);
    }

    @Override
    public void onLoadFinished(Loader<List<FeedItem>> listLoader, List<FeedItem> feedItems) {
        mDataFragment.photos = new ArrayList<FeedItem>();
        final int len = feedItems.size();
        for (int i = 0; i < MAX_IMAGE_COUNT; i++) {
            if (i >= len) {
                break;
            }
            mDataFragment.photos.add(feedItems.get(i));
        }

        mPhotosFragment.onNewPhotos(mDataFragment.photos);
    }

    @Override
    public void onLoaderReset(Loader<List<FeedItem>> listLoader) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putInt("current", getCurrentSelection());
        }
    }

    public PhotoDataFragment getDataFragment() {
        return mDataFragment;
    }

    public ArrayList<FeedItem> getPhotoItems() {
        return getDataFragment().photos;
    }

    public void setCurrentSelection(final int i) {
        mCurrentSelection = i;

        FeedItem feedItem = getPhotoItems().get(i);
        if (feedItem != null) {
            String photoTitle = feedItem.getTitle();
            GATrackingManager.getInstance().track(getTrackingPathComponent(), photoTitle);
        }
    }

    public int getCurrentSelection() {
        return mCurrentSelection;
    }

    public void showDetailWithSelection(final int selection) {
        setCurrentSelection(selection);

        mPhotoDetailFragment = new PhotoDetailFragment();
        if (isMultipaned()) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.details_container, mPhotoDetailFragment,
                            PhotoDetailFragment.class.getName()).commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, mPhotoDetailFragment,
                            PhotoDetailFragment.class.getName()).addToBackStack(null).commit();
        }
    }
}
