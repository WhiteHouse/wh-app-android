package gov.whitehouse.app.wh;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.app.BaseListFragment;
import gov.whitehouse.core.manager.FeedCategoryManager;
import gov.whitehouse.data.model.FeedCategoryItem;
import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.wh.DrawerItemAdapter;
import icepick.Icicle;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;

public
class DrawerFragment extends BaseListFragment<MenuItem>
                     implements DrawerInteractions.FragmentCallbacks,
                                BaseAdapter.OnItemClickListener
{
    public static final int GROUP_ARTICLES = 1;

    public static final int GROUP_PHOTOS = 2;

    public static final int GROUP_VIDEOS = 3;

    public static final int GROUP_LIVE = 4;

    public static final int GROUP_FAVORITES = 5;

    private
    boolean mDrawerOpened = false;

    private
    Menu mMenu;

    private
    int mLivePosition = -1;

    @Icicle
    int mCheckedPosition = 1;

    private
    DrawerInteractions.ActivityCallbacks mDrawerCallbacks = null;

    private
    Collection<MenuItem> getMenuItems()
    {
        final int sz = mMenu.size();
        final List<MenuItem> items = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            items.add(mMenu.getItem(i));
        }
        return items;
    }

    @Override
    public
    void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (!(activity instanceof DrawerInteractions.ActivityCallbacks)) {
            throw new IllegalStateException("DrawerFragment's activity must implement DrawerInteractions.ActivityCallbacks");
        }
        mDrawerCallbacks = (DrawerInteractions.ActivityCallbacks) activity;
        getAdapter().setOnItemClickListener(this);
    }

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        mMenu = new MenuBuilder(getActivity());
        bindSubscription(
                AndroidObservable.bindFragment(this,
                                               FeedCategoryManager.get().getFeedCategoryConfig()
                                                       .first()
                                                       .subscribeOn(Schedulers.newThread()))
                        .subscribe(feedCategoryConfig -> {
                            List<FeedCategoryItem> items = feedCategoryConfig.feeds();
                            int i = 0;
                            int group = 0;
                            for (FeedCategoryItem c : items) {
                                switch (c.viewType()) {
                                    case FeedCategoryItem.VIEW_TYPE_ARTICLE_LIST:
                                        group = GROUP_ARTICLES;
                                        break;
                                    case FeedCategoryItem.VIEW_TYPE_PHOTO_GALLERY:
                                        group = GROUP_PHOTOS;
                                        break;
                                    case FeedCategoryItem.VIEW_TYPE_VIDEO_GALLERY:
                                        group = GROUP_VIDEOS;
                                        break;
                                    case FeedCategoryItem.VIEW_TYPE_LIVE:
                                        mLivePosition = i + 1;
                                        group = GROUP_LIVE;
                                        break;
                                }
                                mMenu.add(group, Menu.NONE, i++, c.title())
                                        .setIntent(new Intent().setData(Uri.parse(c.feedUrl())));
                            }
                            mMenu.add(GROUP_FAVORITES, Menu.NONE, i++, R.string.title_favorites);

                            getAdapter().addHeader(new Object());
                            getAdapter().fillWith(getMenuItems());
                            getAdapter().selectOnly(mCheckedPosition);
                            getProgressBar().setVisibility(View.GONE);
                            getRecyclerView().setVisibility(View.VISIBLE);
                            if (mDrawerCallbacks != null) {
                                if (icicle == null) {
                                    mDrawerCallbacks.onRestoreSelectedItem(
                                            getAdapter().getItem(mCheckedPosition));
                                }
                                updateTitle();
                            }
                        }));
    }

    @Override
    public
    BaseAdapter<MenuItem> onCreateAdapter()
    {
        return new DrawerItemAdapter();
    }

    @Nullable
    @Override
    public
    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public
    void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public
    void onDetach()
    {
        super.onDetach();
        mDrawerCallbacks = null;
    }

    @Override
    public
    void onItemClick(View view, int position)
    {
        if (position < getAdapter().getHeaderCount()) {
            return;
        }
        if (getAdapter().isSelected(position)) {
            mDrawerCallbacks.closeDrawer();
            return;
        }
        if (mDrawerCallbacks != null) {
            mDrawerCallbacks.onDrawerItemSelected(getAdapter().getItem(position));
        }
        mCheckedPosition = position;
        getAdapter().selectOnly(position);
        updateTitle();
    }

    @Override
    public
    void onDrawerOpened()
    {
        mDrawerOpened = true;
    }

    @Override
    public
    void onDrawerClosed()
    {
        mDrawerOpened = false;
    }

    private
    void updateTitle()
    {
        Toolbar bar = getToolbar();
        if (bar != null) {
            TextView title = (TextView) bar.findViewById(R.id.title);
            if (title != null) {
                if (mCheckedPosition < 1 || mCheckedPosition > mMenu.size()) {
                    title.setText("");
                } else {
                    title.setText(getAdapter().getItem(mCheckedPosition).getTitle());
                }
            }
        }
    }

    public
    void setDrawerCallbacks(DrawerInteractions.ActivityCallbacks cb) {
        mDrawerCallbacks = cb;
    }

    @Override
    public
    void clickLive()
    {
        onItemClick(null, mLivePosition);
    }
}
