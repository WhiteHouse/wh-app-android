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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.androidquery.AQuery;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.ui.activities.app.PhotoGalleryActivity;
import gov.whitehouse.utils.FavoritesUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.View.inflate;
import static com.actionbarsherlock.widget.ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME;
import static gov.whitehouse.ui.activities.app.PhotoGalleryActivity.BasePhotoFragment;
import static gov.whitehouse.ui.activities.app.PhotoGalleryActivity.NO_SELECTION;
import static gov.whitehouse.utils.FavoritesUtils.FAVORITE_PHOTOS;

public class PhotoDetailFragment extends BasePhotoFragment
        implements PhotoGalleryActivity.IPhotosCallbacks {

    private boolean mFavorited;

    private ProgressBar mProgress;

    private RelativeLayout mPagerHolder;

    private ViewPager mViewPager;

    private CirclePageIndicator mCirclePageIndicator;

    private PhotoPagerAdapter mPagerAdapter;

    private MenuItem mFavoriteItem;

    private MenuItem mShareItem;

    private ShareActionProvider mShareActionProvider;

    private Intent mShareIntent;

    public static class PhotoPagerAdapter extends PagerAdapter {

        private ArrayList<FeedItem> mItems;

        private Context mContext;

        public PhotoPagerAdapter(final Context context, final List<FeedItem> items) {
            super();
            mContext = context;
            mItems = new ArrayList<FeedItem>();
            if (items != null) {
                mItems.addAll(items);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final FeedItem item = mItems.get(position);

            if (item == null) {
                return null;
            }

            View v = inflate(mContext, R.layout.detailed_photo_item, null);

            /*
             * Do some magic to get the actual description out of the feed item.
             */
            String desc = item.getDescription();
            desc = desc.substring(desc.lastIndexOf("<p>") + 3, desc.lastIndexOf("</p>"));

            ((TextView) v.findViewById(R.id.tv_photo_description)).setText(Html.fromHtml(desc));
            final AQuery aq = new AQuery(mContext);
            try {
                aq.id((ImageView) v.findViewById(R.id.iv_thumbnail))
                        .image(item.getThumbnail(720).toString(), true, true, 0, 0, null,
                                AQuery.FADE_IN_NETWORK);
            } catch (NullPointerException e) {
                aq.id((ImageView) v.findViewById(R.id.iv_thumbnail)).image(android.R.color.white);
                Toast.makeText(mContext,
                        "An error occurred while loading the image.",
                        Toast.LENGTH_SHORT).show();
            }

            /*
             * Add basic touch-to-hide-caption functionality
             */
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewGroup photoMeta = (ViewGroup) view.findViewById(R.id.ll_photo_meta);
                    if (photoMeta.getVisibility() == View.VISIBLE) {
                        photoMeta.setVisibility(View.GONE);
                    } else {
                        photoMeta.setVisibility(View.VISIBLE);
                    }
                }
            });

            container.addView(v, 0);

            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            if (view != null) {
                return view.equals(o);
            } else {
                return o == null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.photo_detail_fragment, container, false);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        mPagerHolder = (RelativeLayout) v.findViewById(R.id.rl_pager_holder);
        mViewPager = (ViewPager) v.findViewById(R.id.vp_photos);
        mCirclePageIndicator = (CirclePageIndicator) v.findViewById(R.id.circle_indicator);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPagerHolder.setVisibility(GONE);
        mProgress.setVisibility(VISIBLE);

        if (getPhotoActivity().getPhotoItems() != null && getPhotoActivity()
                .getCurrentSelection() != NO_SELECTION) {
            mFavorited = FavoritesUtils.isFavorited(getPhotoActivity(),
                    getPhotoActivity().getPhotoItems()
                            .get(getPhotoActivity()
                                    .getCurrentSelection()));
        }

        onNewPhotos(getPhotoActivity().getPhotoItems());
    }

    @Override
    public void onNewPhotos(List<FeedItem> photos) {
        mPagerAdapter = new PhotoPagerAdapter(getSherlockActivity(), photos);
        mViewPager.setAdapter(mPagerAdapter);
        mCirclePageIndicator.setViewPager(mViewPager);

        mCirclePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                getPhotoActivity().setCurrentSelection(i);

                if (mShareActionProvider == null || mShareIntent == null) {
                    getSherlockActivity().supportInvalidateOptionsMenu();
                } else {
                    mShareIntent.putExtra(Intent.EXTRA_TEXT,
                            getPhotoActivity().getPhotoItems().get(i)
                                    .getLink().toString());
                    mShareActionProvider.setShareIntent(mShareIntent);
                }

                if (mFavoriteItem == null) {
                    getSherlockActivity().supportInvalidateOptionsMenu();
                } else {
                    mFavorited = FavoritesUtils.isFavorited(getSherlockActivity(),
                            getPhotoActivity().getPhotoItems()
                                    .get(i));
                    if (mFavorited) {
                        mFavoriteItem.setTitle(R.string.unfavorite);
                        mFavoriteItem.setIcon(R.drawable.ic_favorite);
                    } else {
                        mFavoriteItem.setTitle(R.string.favorite);
                        mFavoriteItem.setIcon(R.drawable.ic_unfavorite);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mProgress.setVisibility(GONE);
        mPagerHolder.setVisibility(VISIBLE);
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    @Override
    public void onStart() {
        super.onStart();

        getPhotoActivity().getDrawerGarment().setDrawerEnabled(false);
        getPhotoActivity().supportInvalidateOptionsMenu();

        if (getPhotoActivity().getCurrentSelection() != NO_SELECTION) {
            mViewPager.setCurrentItem(getPhotoActivity().getCurrentSelection());
        } else {
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (!getPhotoActivity().isMultipaned()) {
            getPhotoActivity().getSupportActionBar().setHomeButtonEnabled(true);
            getPhotoActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getPhotoActivity().getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        mShareItem = menu.findItem(R.id.menu_share);
        mShareItem.setVisible(true);
        if (mShareIntent == null) {
            mShareIntent = new Intent(Intent.ACTION_SEND);
        }
        try {
            mShareIntent.setType("text/plain");
            mShareActionProvider = (ShareActionProvider) mShareItem.getActionProvider();
            mShareActionProvider.setShareHistoryFileName(DEFAULT_SHARE_HISTORY_FILE_NAME);
            mShareActionProvider.setShareIntent(mShareIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mFavoriteItem = menu.findItem(R.id.menu_favorite);
        mFavoriteItem.setVisible(true);
        if (mFavorited) {
            mFavoriteItem.setTitle(R.string.unfavorite);
            mFavoriteItem.setIcon(R.drawable.ic_favorite);
        } else {
            mFavoriteItem.setTitle(R.string.favorite);
            mFavoriteItem.setIcon(R.drawable.ic_unfavorite);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return super.onOptionsItemSelected(item);
        } else if (item.getItemId() == R.id.menu_favorite) {
            if (mFavorited) {
                FavoritesUtils.removeFromFavorites(getSherlockActivity(), FAVORITE_PHOTOS,
                        getPhotoActivity().getPhotoItems()
                                .get(mViewPager.getCurrentItem()));
                item.setTitle(R.string.favorite);
                item.setIcon(R.drawable.ic_unfavorite);
            } else {
                FavoritesUtils.addToFavorites(getSherlockActivity(), FAVORITE_PHOTOS,
                        getPhotoActivity().getPhotoItems()
                                .get(mViewPager.getCurrentItem()));
                item.setTitle(R.string.unfavorite);
                item.setIcon(R.drawable.ic_favorite);
            }
            mFavorited = !mFavorited;
        }
        return super.onOptionsItemSelected(item);
    }
}
