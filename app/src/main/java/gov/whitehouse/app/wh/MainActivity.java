package gov.whitehouse.app.wh;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.urbanairship.google.PlayServicesUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gov.whitehouse.R;
import gov.whitehouse.app.BaseActivity;
import gov.whitehouse.app.BaseFragment;
import gov.whitehouse.core.FeedItemData;
import gov.whitehouse.core.NoNetworkException;
import gov.whitehouse.core.manager.FeedCategoryManager;
import gov.whitehouse.core.manager.FeedManager;
import gov.whitehouse.data.model.FeedCategoryConfig;
import gov.whitehouse.data.model.FeedCategoryItem;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.data.model.FeedType;
import gov.whitehouse.util.DateUtils;
import gov.whitehouse.util.NetworkUtils;
import gov.whitehouse.util.ThemeUtils;
import icepick.Icicle;
import rx.Observable;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;

import static android.support.v4.view.GravityCompat.START;


public class MainActivity extends BaseActivity
                          implements DrawerInteractions.ActivityCallbacks,
                                     FeedItemListInteractions.ActivityCallbacks
{

    @Icicle
    boolean mDrawerVisible = false;

    @Icicle
    boolean mSearchVisible = false;

    @Icicle
    boolean mSubmittedSearch = false;

    @Icicle
    boolean mShowingLive = false;

    @Icicle
    int mSelectedMenuItem = 0;

    @Icicle
    String mSearchQuery;

    private
    boolean mHasLiveEvents = false;

    private
    boolean mTogglingSearch = false;

    private
    ActionBarDrawerToggle mActionBarDrawerToggle;

    private
    DrawerFragment mDrawerFragment;

    private
    DrawerInteractions.FragmentCallbacks mDrawerLayoutCallbacks;

    private
    MenuItem mSearchItem;

    private
    SearchFragment mSearchFragment;

    @InjectView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.live_bar)
    TextView mLiveBar;

    @InjectView(R.id.left_drawer)
    ViewGroup mLeftDrawer;

    private void
    decideLiveBarAndSearchVisibility()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 || mShowingLive || mSearchVisible) {
            mLiveBar.setVisibility(View.GONE);
            mSearchItem.setVisible(false);
        } else {
            mLiveBar.setVisibility(View.VISIBLE);
            mSearchItem.setVisible(true);
        }
        if (!mHasLiveEvents) {
            mLiveBar.setVisibility(View.GONE);
        }
    }

    private void
    createDrawerToggle()
    {
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
            @Override
            public
            void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                mDrawerVisible = true;
                if (mDrawerLayoutCallbacks != null) {
                    mDrawerLayoutCallbacks.onDrawerOpened();
                }
            }

            @Override
            public
            void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
                mDrawerVisible = false;
                if (mDrawerLayoutCallbacks != null) {
                    mDrawerLayoutCallbacks.onDrawerClosed();
                }
            }
        };
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    private void
    addDrawerFragment()
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;
        mDrawerFragment = (DrawerFragment) fm.findFragmentByTag(DrawerFragment.class.getName());
        if (mDrawerFragment == null) {
            ft = fm.beginTransaction();
            mDrawerFragment = new DrawerFragment();
            ft.add(R.id.left_drawer, mDrawerFragment, DrawerFragment.class.getName())
                    .commitAllowingStateLoss();
        }
        mDrawerFragment.setDrawerCallbacks(this);
        mDrawerLayoutCallbacks = mDrawerFragment;
    }

    private void
    checkForGooglePlayServices()
    {
        if (PlayServicesUtils.isGooglePlayStoreAvailable()) {
            PlayServicesUtils.handleAnyPlayServicesError(this);
        }
    }

    private void
    configureLeftDrawer()
    {
        ViewGroup.LayoutParams lps = mLeftDrawer.getLayoutParams();
        lps.width = ThemeUtils.getLeftDrawerWidth(this);
        mLeftDrawer.setLayoutParams(lps);
    }

    private void
    createDrawer()
    {
        createDrawerToggle();
        addDrawerFragment();
        configureLeftDrawer();
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.wh_blue));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        }
    }

    private void
    configureSearchView()
    {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public
            boolean onQueryTextSubmit(String s)
            {
                mSearchFragment.submitQuery(s);
                mSubmittedSearch = true;
                return false;
            }

            @Override
            public
            boolean onQueryTextChange(String s)
            {
                mSearchQuery = s;
                mSubmittedSearch = false;
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener()
        {
            @Override
            public
            boolean onMenuItemActionExpand(MenuItem item)
            {
                if (!mSearchVisible) {
                    toggleSearch();
                }
                if (mSearchQuery != null) {
                    searchView.setQuery(mSearchQuery, false);
                }
                return mSearchVisible;
            }

            @Override
            public
            boolean onMenuItemActionCollapse(MenuItem item)
            {
                if (mSearchVisible) {
                    toggleSearch();
                }
                return true;
            }
        });
    }

    private void
    dispatchCreateToolbarToFragments()
    {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f != null && f.isAdded() && f.isVisible() && f instanceof BaseFragment) {
                    ((BaseFragment) f).onCreateToolbar(mToolbar);
                }
            }
        }
    }

    private void
    createToolbarMenu()
    {
        if (mToolbar.getMenu() != null) {
            mToolbar.getMenu().clear();
        }
        mToolbar.inflateMenu(R.menu.main);
        dispatchCreateToolbarToFragments();
        mToolbar.setOnMenuItemClickListener(menuItem -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    if (f != null && f.isAdded() && f instanceof BaseFragment) {
                        if (((BaseFragment) f).onToolbarMenuItemClicked(menuItem)) {
                            return true;
                        }
                    }
                }
            }
            if (mActionBarDrawerToggle.onOptionsItemSelected(menuItem)) {
                return true;
            }
            return onToolbarMenuItemSelected(menuItem);
        });
        mSearchItem = mToolbar.getMenu().findItem(R.id.action_search);
        mSearchFragment = findOrCreateSearchFragment();
        mSearchFragment.setSearchResultClickedListener((result, position) -> {
            Intent goIntent = new Intent(Intent.ACTION_VIEW);
            goIntent.setData(Uri.parse(result.unescapedUrl()));
            startActivity(goIntent);
        });
        configureSearchView();
    }

    private void
    createActivity(Bundle icicle)
    {
        checkForGooglePlayServices();
        createDrawer();
        createToolbarMenu();
        TextView titleView = (TextView) mToolbar.findViewById(R.id.title);
        if (titleView != null) {
            titleView.setTypeface(Typeface.SERIF);
        }
        mLiveBar.setTypeface(Typeface.SERIF);
    }

    @Override
    public
    void closeDrawer()
    {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void
    onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void
    onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState, R.layout.activity_main);
        ButterKnife.inject(this);
        checkNetworkElseFail();
        createActivity(savedInstanceState);
        getSupportFragmentManager().addOnBackStackChangedListener(this::decideLiveBarAndSearchVisibility);
    }

    @Override
    public
    void onBackPressed()
    {
        if (MenuItemCompat.isActionViewExpanded(mSearchItem)) {
            MenuItemCompat.collapseActionView(mSearchItem);
        } else {
            super.onBackPressed();
        }
    }

    private
    void showContainerToolbarActions(boolean visible)
    {
        mToolbar.getMenu().setGroupVisible(Menu.CATEGORY_CONTAINER, visible);
    }

    private
    void doToggleSearch()
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;
        SearchFragment sf;

        mSearchFragment = findOrCreateSearchFragment();
        sf = mSearchFragment;
        if (mSearchVisible) {
            ft = fm.beginTransaction()
                    .hide(sf);
        } else {
            ft = fm.beginTransaction()
                    .show(sf);
        }
        ft.setCustomAnimations(android.R.animator.fade_in,
                               android.R.animator.fade_out,
                               android.R.animator.fade_in,
                               android.R.animator.fade_out)
                .commit();
        showContainerToolbarActions(mSearchVisible);
        mSearchVisible = !mSearchVisible;
        updateLiveEvents();
    }

    private ValueAnimator mSearchHamburgerAnimator;

    private
    void toggleSearch()
    {
        if (!mTogglingSearch) {
            float start;
            float end;
            mTogglingSearch = true;
            if (mSearchHamburgerAnimator != null) {
                mSearchHamburgerAnimator.cancel();
                start = (float) mSearchHamburgerAnimator.getAnimatedValue();
            } else {
                start = (mSearchVisible) ? 1f : 0f;
            }
            end = (mSearchVisible) ? 0f : 1f;
            mSearchHamburgerAnimator = ValueAnimator.ofFloat(start, end);
            if (mSearchVisible) {
                mTogglingSearch = false;
                doToggleSearch();
                mSearchHamburgerAnimator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    mActionBarDrawerToggle.onDrawerSlide(mDrawerLayout, value);
                });
                mSearchHamburgerAnimator.setInterpolator(new DecelerateInterpolator());
                mSearchHamburgerAnimator.setDuration(300);
                mSearchHamburgerAnimator.start();
            } else {
                mSearchHamburgerAnimator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    mActionBarDrawerToggle.onDrawerSlide(mDrawerLayout, value);
                    if (value == 1f) {
                        mTogglingSearch = false;
                        doToggleSearch();
                        MenuItemCompat.expandActionView(mSearchItem);
                    }
                });
                mSearchHamburgerAnimator.setInterpolator(new DecelerateInterpolator());
                mSearchHamburgerAnimator.setDuration(300);
                mSearchHamburgerAnimator.start();
            }
        }
    }

    public
    boolean onToolbarMenuItemSelected(MenuItem item)
    {
        return false;
    }

    @Override
    protected void
    onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
        mToolbar.setNavigationOnClickListener(v -> {
            if (mActionBarDrawerToggle.isDrawerIndicatorEnabled()) {
                if (mDrawerVisible) {
                    mDrawerLayout.closeDrawer(START);
                } else {
                    mDrawerLayout.openDrawer(START);
                }
            } else {
                onBackPressed();
            }
        });
    }

    @Override
    protected
    void onResume()
    {
        super.onResume();
        if (mSearchQuery != null) {
            ((SearchView) MenuItemCompat.getActionView(mSearchItem)).setQuery(mSearchQuery, mSubmittedSearch);
        }
        decideLiveBarAndSearchVisibility();
    }

    @Override
    public
    void onDrawerItemSelected(MenuItem item)
    {
        if (mSearchVisible && !mTogglingSearch) {
            toggleSearch();
        }
        chooseFragmentFromDrawerItem(item);
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    @Override
    public
    void onRestoreSelectedItem(MenuItem item)
    {
        chooseFragmentFromDrawerItem(item);
    }

    public
    void chooseFragmentFromDrawerItem(MenuItem item)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment f = null;
        Intent iIntent = item.getIntent();
        String feedUrl;

        mShowingLive = false;
        if (item.getGroupId() == DrawerFragment.GROUP_FAVORITES) {
            f = FeedItemListFragment.newInstance(item.getTitle(),
                                                 FeedItemListFragment.TYPE_FAVORITES,
                                                 null);
        } else if (iIntent != null && iIntent.getData() != null) {
            feedUrl = iIntent.getData().toString();
            switch (item.getGroupId()) {
                case DrawerFragment.GROUP_ARTICLES:
                    f = FeedItemListFragment.newInstance(item.getTitle(),
                                                         FeedItemListFragment.TYPE_ARTICLE,
                                                         feedUrl);
                    break;
                case DrawerFragment.GROUP_PHOTOS:
                    f = FeedItemListFragment.newInstance(item.getTitle(),
                                                         FeedItemListFragment.TYPE_PHOTOS,
                                                         feedUrl);
                    break;
                case DrawerFragment.GROUP_VIDEOS:
                    f = FeedItemListFragment.newInstance(item.getTitle(),
                                                         FeedItemListFragment.TYPE_VIDEOS,
                                                         feedUrl);
                    break;
                case DrawerFragment.GROUP_LIVE:
                    f = FeedItemListFragment.newInstance(item.getTitle(),
                                                         FeedItemListFragment.TYPE_LIVE,
                                                         feedUrl);
                    mShowingLive = true;
                    break;
            }
        }
        if (f == null) {
            f = new Fragment();
        }
        Fragment old = fm.findFragmentByTag("contentFragment");
        if (old != null) {
            ft.remove(old);
        }
        ft.add(R.id.fragment_container, f, "contentFragment")
          .commitAllowingStateLoss();
        decideLiveBarAndSearchVisibility();
    }

    @Override
    public
    void onFeedItemSelected(List<FeedItemData> items, int selectedPosition)
    {
        FeedItem item = items.get(selectedPosition).item;
        FragmentManager fm;
        FragmentTransaction ft;
        Fragment f;
        Intent intent;
        final List<FeedItem> feedItems;

        if (item.type().equals(FeedType.TYPE_LIVE)) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.link()));
            startActivity(intent);
            return;
        }
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        if (item.type().equals(FeedType.TYPE_PHOTO)) {
            feedItems = new ArrayList<>(items.size());
            for (FeedItemData data : items) {
                feedItems.add(data.item);
            }
            f = GalleryFragment.newInstance(feedItems, selectedPosition);
        } else if (item.type().equals(FeedType.TYPE_VIDEO)) {
            if (item.isYouTubeVideo()) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(item.videoLink()));
                startActivity(intent);
                return;
            } else {
                f = VideoPlayerFragment.newInstance(item);
            }
        } else {
            f = ArticleViewFragment.newInstance(item);
        }
        if (mSearchVisible && !mTogglingSearch) {
            toggleSearch();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }
        Fragment old = fm.findFragmentByTag("contentFragment");
        if (old != null) {
            ft.remove(old);
        }
        ft.add(R.id.fragment_container, f, "contentFragment")
          .addToBackStack(null)
          .commit();
    }

    @Override
    public
    void onRestoreSelectedItem(FeedItem item)
    {
    }

    private FeedCategoryItem mLiveItem = null;

    private
    Observable<List<FeedItem>> observeLiveEvents()
    {
        return AndroidObservable.bindActivity(this, Observable.<List<FeedItem>>create(sub -> {
            final FeedCategoryManager fcm = FeedCategoryManager.get();
            final FeedCategoryConfig config = fcm.getFeedCategoryConfig()
                    .toBlocking()
                    .first();
            final List<FeedItem> feedItems;
            final List<FeedItem> validItems = new ArrayList<>();
            FeedCategoryItem liveItem = null;

            if (!NetworkUtils.checkNetworkAvailable(this)) {
                sub.onError(new NoNetworkException());
                return;
            }

            for(FeedCategoryItem c : config.feeds()) {
                if (FeedCategoryItem.VIEW_TYPE_LIVE.equals(c.viewType())) {
                    liveItem = c;
                    break;
                }
            }
            if (liveItem == null) {
                sub.onError(new IllegalStateException("no live item"));
                return;
            }
            FeedManager.updateFeedFromServer(liveItem.feedUrl(), liveItem.title(), liveItem.viewType());
            feedItems = FeedManager.observeFeedItems(liveItem.feedUrl())
                    .toBlocking()
                    .first();
            for (FeedItem item : feedItems) {
                if (item != null && item.pubDate() != null) {
                    validItems.add(item);
                }
            }

            mLiveItem = liveItem;

            sub.onNext(validItems);
            sub.onCompleted();
        })).onErrorReturn(throwable -> new ArrayList<>())
           .subscribeOn(Schedulers.newThread());
    }

    private
    void updateLiveEvents()
    {
        observeLiveEvents()
                .map(feedItems -> {
                    final List<FeedItem> nowItems = new ArrayList<>();
                    for (FeedItem item : feedItems) {
                        if (DateUtils.within30MinutesBeforeNow(item.pubDate())) {
                            nowItems.add(item);
                        }
                    }
                    return nowItems;
                }).subscribe(feedItems -> {
            if (feedItems.size() < 1) {
                mHasLiveEvents = false;
                mLiveBar.setVisibility(View.GONE);
            } else {
                mHasLiveEvents = true;
                if (feedItems.size() == 1) {
                    mLiveBar.setText(String.format(getString(R.string.live_bar_text_single),
                            feedItems.get(0).displayTitle()));
                } else {
                    mLiveBar.setText(String.format(getString(R.string.live_bar_text_multiple),
                            Integer.toString(feedItems.size())));
                }
                mLiveBar.setOnClickListener(v -> {
                    mDrawerLayoutCallbacks.clickLive();
                });
                decideLiveBarAndSearchVisibility();
            }
        });
    }

    @Override
    protected
    void onStart()
    {
        super.onStart();
        updateLiveEvents();
    }

    private
    SearchFragment findSearchFragment()
    {
        FragmentManager fm = getSupportFragmentManager();
        return (SearchFragment) fm.findFragmentByTag("searchFragment");
    }

    private
    SearchFragment findOrCreateSearchFragment()
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;
        SearchFragment sf = findSearchFragment();

        ft = fm.beginTransaction();
        if (sf == null) {
            sf = new SearchFragment();
            ft = fm.beginTransaction()
                   .replace(R.id.search_container, sf, "searchFragment");
        }
        if (!mSearchVisible) {
            ft.hide(sf);
        }
        ft.commit();

        return sf;
    }

    public
    void setNavigationIconToUp()
    {
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(false);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
    }

    public
    void setNavigationIconToDrawer()
    {
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    public
    void disableDrawer()
    {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.START);
        if (mToolbar.getWrapper() != null) {
            mToolbar.getWrapper().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
        }
        setNavigationIconToUp();
    }

    public
    void enableDrawer()
    {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.START);
        if (mToolbar.getWrapper() != null) {
            mToolbar.getWrapper().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
        }
        setNavigationIconToDrawer();
    }

    public
    boolean shouldAddActionItems()
    {
        return !mSearchVisible;
    }

    public
    Toolbar getToolbar()
    {
        return mToolbar;
    }
}
