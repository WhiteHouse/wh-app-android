package gov.whitehouse.app.wh;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gov.whitehouse.R;
import gov.whitehouse.app.BaseFragment;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.util.FavoritesUtils;
import gov.whitehouse.widget.FixedFragmentStatePagerAdapter;
import icepick.Icicle;
import rx.Observable;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static gov.whitehouse.util.FavoritesUtils.FAVORITE_PHOTOS;

public
class GalleryFragment extends BaseFragment
{

    public static final String EXTRA_FEED = "extra:feed";

    public static final String EXTRA_INDEX = "extra:index";

    @Icicle
    int mIndex = 0;

    private
    List<FeedItem> mFeedItems = new ArrayList<>();

    private
    Menu mMenu;

    private
    PagerAdapter mPagerAdapter;

    @InjectView(R.id.progress)
    ProgressBar mProgress;

    @InjectView(R.id.page_count)
    TextView mPageCount;

    @InjectView(R.id.pager)
    ViewPager mPager;

    public static
    GalleryFragment newInstance(List<FeedItem> feed, int index)
    {
        GalleryFragment f = new GalleryFragment();
        Bundle b = new Bundle(2);
        b.putParcelableArray(EXTRA_FEED, feed.toArray(new FeedItem[feed.size()]));
        b.putInt(EXTRA_INDEX, index);
        f.setArguments(b);
        return f;
    }

    @Override
    public
    void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (mPagerAdapter == null) {
            mPagerAdapter = new GalleryPagerAdapter(getChildFragmentManager());
        }
    }

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        Bundle args = getArguments();
        Collections.addAll(mFeedItems, (FeedItem[])args.getParcelableArray(EXTRA_FEED));
        mIndex = args.getInt(EXTRA_INDEX, 0);
    }

    @Override
    public
    void onDestroy()
    {
        super.onDestroy();
        if (getArguments() != null) {
            /*
             * Overwrite original feed items passed in as arguments, as their properties
             * may have changed as a result of favoriting/unfavoriting.
             */
            getArguments().putParcelableArray(EXTRA_FEED,
                                              mFeedItems.toArray(new FeedItem[mFeedItems.size()]));
            getArguments().putInt(EXTRA_INDEX, mIndex);
        }
    }

    private
    void configureOverflow(boolean favorited)
    {
        MenuItem favoriteItem, unfavoriteItem, shareItem;
        ShareActionProvider shareAction;
        Intent shareIntent;

        favoriteItem = mMenu.findItem(R.id.action_favorite);
        unfavoriteItem = mMenu.findItem(R.id.action_unfavorite);
        shareItem = mMenu.findItem(R.id.action_share);
        if (favoriteItem == null || unfavoriteItem == null || shareItem == null) {
            return;
        }
        favoriteItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        favoriteItem.setVisible(!favorited);
        unfavoriteItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        unfavoriteItem.setVisible(favorited);
        shareAction = ((ShareActionProvider) MenuItemCompat.getActionProvider(shareItem));
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mFeedItems.get(mIndex).title());
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFeedItems.get(mIndex).link());
        shareIntent.setType("text/plain");
        shareAction.setShareIntent(shareIntent);
    }

    @Override
    public
    void onCreateToolbar(Toolbar toolbar)
    {
        super.onCreateToolbar(toolbar);
        if (shouldAddActionItems()) {
            toolbar.inflateMenu(R.menu.feeditem_overflow);
            mMenu = toolbar.getMenu();
            configureOverflow(mFeedItems.get(mIndex).favorited());
        }
    }

    private
    void onFavoriteItemSelected(boolean favorited)
    {
        Observable<Boolean> obsAction;
        FeedItem item = mFeedItems.get(mIndex);

        if (favorited) {
            obsAction = Observable.create((Subscriber<? super Boolean> subscriber) -> {
                try {
                    FavoritesUtils.addToFavorites(getActivity(), FAVORITE_PHOTOS, item);
                    mFeedItems.set(mIndex, new FeedItem.Builder(item).setFavorited(true).build());
                    subscriber.onNext(true);
                } catch (Exception e) {
                    Timber.w(e, "Unable to add FeedItem to favorites");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            });
            obsAction = AndroidObservable.bindFragment(this, obsAction)
                    .subscribeOn(Schedulers.io());
            bindSubscription(obsAction.subscribe(result -> {
                if (result) {
                    Toast.makeText(getActivity(), R.string.added_to_favorites, Toast.LENGTH_SHORT)
                            .show();
                }
                configureOverflow(result);
            }));
        } else {
            obsAction = Observable.create((Subscriber<? super Boolean> subscriber) -> {
                try {
                    FavoritesUtils.removeFromFavorites(getActivity(), FAVORITE_PHOTOS, item);
                    mFeedItems.set(mIndex, new FeedItem.Builder(item).setFavorited(false).build());
                    subscriber.onNext(false);
                } catch (Exception e) {
                    Timber.w(e, "Unable to remove FeedItem from favorites");
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            });
            obsAction = AndroidObservable.bindFragment(this, obsAction)
                    .subscribeOn(Schedulers.io());
            bindSubscription(obsAction.subscribe(result -> {
                if (!result) {
                    Toast.makeText(getActivity(), R.string.removed_from_favorites, Toast.LENGTH_SHORT)
                            .show();
                }
                configureOverflow(result);
            }));
        }
    }

    @Override
    public
    boolean onToolbarMenuItemClicked(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                onFavoriteItemSelected(true);
                return true;
            case R.id.action_unfavorite:
                onFavoriteItemSelected(false);
                return true;
        }
        return super.onToolbarMenuItemClicked(item);
    }

    @Override
    public
    View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    private
    void fadeOutPageCount()
    {
        mPageCount.animate()
                  .alpha(0.0f)
                  .setStartDelay(3000)
                  .start();
    }

    private
    void fadeInPageCount()
    {
        mPageCount.animate()
                  .alpha(1.0f)
                  .setStartDelay(0)
                  .start();
    }

    @Override
    public
    void onStart()
    {
        super.onStart();
        if (mMenu == null) {
            onCreateToolbar(getToolbar());
        }
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {

            @Override
            public
            void onPageSelected(int position)
            {
                super.onPageSelected(position);
                mIndex = position;
                configureOverflow(mFeedItems.get(position).favorited());
                mPageCount.setText(String.valueOf(position + 1) + " / " + String.valueOf(mPagerAdapter.getCount()));
                if (getTracker() != null) {
                    getTracker().setPage(mFeedItems.get(position).link());
                }
            }

            @Override
            public
            void onPageScrollStateChanged(int state)
            {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    fadeOutPageCount();
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    fadeInPageCount();
                }
            }
        });
        mPageCount.setText(String.valueOf(mIndex + 1) + " / " + String.valueOf(mPagerAdapter.getCount()));
        fadeOutPageCount();
        mPager.setCurrentItem(mIndex);
        mProgress.setVisibility(View.GONE);
        mPager.setVisibility(View.VISIBLE);
        if (getTracker() != null) {
            getTracker().setScreenName("Gallery");
        }
    }

    @Override
    public
    void onStop()
    {
        super.onStop();
        if (mMenu != null) {
            mMenu.removeItem(R.id.action_share);
            mMenu.removeItem(R.id.action_favorite);
            mMenu.removeItem(R.id.action_unfavorite);
            mMenu = null;
        }
    }

    @Override
    public
    boolean isRootFragment()
    {
        return false;
    }

    private
    class GalleryPagerAdapter extends FixedFragmentStatePagerAdapter
    {

        public
        GalleryPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public
        Fragment getItem(int position)
        {
            return GalleryPhotoFragment.newInstance(mFeedItems.get(position), position);
        }

        @Override
        public
        int getCount()
        {
            return mFeedItems.size();
        }
    }
}
