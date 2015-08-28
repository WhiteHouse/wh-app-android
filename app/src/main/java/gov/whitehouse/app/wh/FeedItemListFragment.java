package gov.whitehouse.app.wh;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.app.BaseListFragment;
import gov.whitehouse.core.FeedItemData;
import gov.whitehouse.core.manager.FeedManager;
import gov.whitehouse.data.model.FeedCategoryItem;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.data.model.FeedType;
import gov.whitehouse.util.DateUtils;
import gov.whitehouse.util.FavoritesUtils;
import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.wh.FeedItemAdapter;
import rx.Observer;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;

import static android.support.v7.widget.StaggeredGridLayoutManager.VERTICAL;

public
class FeedItemListFragment extends BaseListFragment<FeedItemData>
                           implements Observer<List<FeedItemData>>,
                                      BaseAdapter.OnItemClickListener
{

    public static final String EXTRA_TITLE = "extra:title";

    public static final String EXTRA_TYPE = "extra:type";

    public static final String EXTRA_URL = "extra:url";

    public static final int TYPE_ARTICLE = 0;

    public static final int TYPE_PHOTOS = 1;

    public static final int TYPE_VIDEOS = 2;

    public static final int TYPE_LIVE = 3;

    public static final int TYPE_FAVORITES = 4;

    private
    int mFeedType;

    private
    boolean mLoaded;

    private
    FeedItemListInteractions.ActivityCallbacks mCallbacks = null;

    private
    String mFeedUrl;

    private
    String mFeedTitle;

    public static
    FeedItemListFragment newInstance(CharSequence title, int type, String url)
    {
        FeedItemListFragment f = new FeedItemListFragment();
        Bundle b = new Bundle(3);
        b.putString(EXTRA_TITLE, String.valueOf(title));
        b.putInt(EXTRA_TYPE, type);
        b.putString(EXTRA_URL, url);
        f.setArguments(b);
        return f;
    }

    @Override
    public
    void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (!(activity instanceof FeedItemListInteractions.ActivityCallbacks)) {
            throw new IllegalStateException("FeedItemListFragment's activity must implement FeedItemListInteractions.ActivityCallbacks");
        }
        mCallbacks = (FeedItemListInteractions.ActivityCallbacks) activity;
        getAdapter().setOnItemClickListener(this);
    }

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        Bundle args = getArguments();
        mFeedUrl = args.getString(EXTRA_URL);
        mFeedType = args.getInt(EXTRA_TYPE);
        mFeedTitle = args.getString(EXTRA_TITLE);
    }

    @Override
    public
    RecyclerView.LayoutManager onCreateLayoutManager()
    {
        Resources res = getResources();
        int feedGridCols = res.getInteger(R.integer.feed_grid_columns);
        int photoGridCols = res.getInteger(R.integer.photo_grid_columns);
        switch (mFeedType) {
            case TYPE_PHOTOS:
                return new StaggeredGridLayoutManager(photoGridCols, VERTICAL);
            case TYPE_VIDEOS:
                return new GridLayoutManager(getActivity(), feedGridCols);
            case TYPE_LIVE:
                return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            case TYPE_ARTICLE:
            default:
                return new StaggeredGridLayoutManager(feedGridCols, VERTICAL);
        }
    }

    @Override
    public
    void onDetach()
    {
        super.onDetach();
        mCallbacks = null;
    }

    @SuppressWarnings("unchecked")
    private
    Subscription fetchFeed()
    {
        return AndroidObservable.bindFragment(this,
                                              FeedManager.observeFeedItems(mFeedUrl)
                                                      .map(feedItems -> {
                                                          List<FeedItemData> processedItems = new ArrayList<>(feedItems.size());
                                                          FeedItemData data;
                                                          final FeedType feedType;
                                                          Date lastDate = null;
                                                          int todayMarker = -1;
                                                          int futureMarker = -1;
                                                          int index = 0;

                                                          switch (mFeedType) {
                                                              case TYPE_PHOTOS:
                                                                  feedType = FeedType.TYPE_PHOTO;
                                                                  break;
                                                              case TYPE_VIDEOS:
                                                                  feedType = FeedType.TYPE_VIDEO;
                                                                  break;
                                                              case TYPE_LIVE:
                                                                  feedType = FeedType.TYPE_LIVE;
                                                                  break;
                                                              default:
                                                                  feedType = FeedType.TYPE_ARTICLE;
                                                                  break;
                                                          }
                                                          for (FeedItem item : feedItems) {
                                                              data = new FeedItemData();
                                                              data.item = new FeedItem.Builder(item)
                                                                      .setFavorited(FavoritesUtils.isFavorited(getActivity(), item))
                                                                      .setType(feedType).build();
                                                              if (lastDate == null) {
                                                                  lastDate = item.pubDate();
                                                                  data.shouldShowDate = true;
                                                                  if (DateUtils.isToday(lastDate)) {
                                                                      data.todayMarker = true;
                                                                      todayMarker = index;
                                                                  } else if (DateUtils.isFutureDay(lastDate)) {
                                                                      data.futureMarker = true;
                                                                      futureMarker = index;
                                                                  } else {
                                                                      data.historicMarker = true;
                                                                  }
                                                              } else {
                                                                  data.shouldShowDate = !DateUtils.isSameDay(lastDate, item.pubDate());
                                                                  if (todayMarker < 0) {
                                                                      if (DateUtils.isToday(item.pubDate())) {
                                                                          data.todayMarker = true;
                                                                          todayMarker = index;
                                                                      }
                                                                  }
                                                                  if (futureMarker < 0) {
                                                                      if (DateUtils.isFutureDay(item.pubDate())) {
                                                                          data.futureMarker = true;
                                                                          futureMarker = index;
                                                                      }
                                                                  }
                                                              }
                                                              index += 1;
                                                              processedItems.add(data);
                                                              lastDate = item.pubDate();
                                                          }

                                                          return processedItems;
                                                      }))
                .subscribeOn(Schedulers.newThread())
                .subscribe(this);
    }

    private
    Subscription fetchData()
    {
        return fetchFeed();
    }

    @Override
    public
    void onStart()
    {
        super.onStart();
        showList(false);
        showProgress(true);
        if (mFeedType == TYPE_FAVORITES) {
            bindSubscription(AndroidObservable.bindFragment(this, FeedManager.observeFavorites())
                                     .map(feedItems -> {
                                         List<FeedItemData> processed = new ArrayList<>(feedItems.size());
                                         FeedItemData data;
                                         for (FeedItem item : feedItems) {
                                             data = new FeedItemData();
                                             data.item = item;
                                             processed.add(data);
                                         }
                                         return processed;
                                     }).subscribe(this));
            FeedManager.updateFavorites(getActivity());
        } else {
            bindSubscription(fetchData());
            String viewType;
            switch (mFeedType) {
                default:
                case TYPE_ARTICLE:
                    viewType = FeedCategoryItem.VIEW_TYPE_ARTICLE_LIST;
                    break;
                case TYPE_LIVE:
                    viewType = FeedCategoryItem.VIEW_TYPE_LIVE;
                    break;
                case TYPE_PHOTOS:
                    viewType = FeedCategoryItem.VIEW_TYPE_PHOTO_GALLERY;
                    break;
                case TYPE_VIDEOS:
                    viewType = FeedCategoryItem.VIEW_TYPE_VIDEO_GALLERY;
                    break;
            }
            FeedManager.updateFeedFromServer(mFeedUrl, mFeedTitle, viewType);
        }
        if (mFeedType == TYPE_FAVORITES) {
            Drawable d = getResources().getDrawable(R.drawable.ic_action_bookmark_outline);
            String s = getString(R.string.empty_favorites_list);
            SpannableString ss = new SpannableString(s);
            ImageSpan is = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
            int index = s.indexOf("{icon}");
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            d.setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            ss.setSpan(is, index, index + 6, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            getEmptyView().setText(ss);
        } else if (mFeedType == TYPE_LIVE) {
            getEmptyView().setText(R.string.empty_live_list);
        }
        if (getTracker() != null) {
            getTracker().setScreenName("FeedItemList");
            if (mFeedType == TYPE_FAVORITES) {
                getTracker().setPage("Favorites");
            } else {
                getTracker().setPage(mFeedUrl);
            }
        }
    }

    @Override
    public
    void onStop()
    {
        super.onStop();
    }

    @Override
    public
    BaseAdapter<FeedItemData> onCreateAdapter()
    {
        return new FeedItemAdapter();
    }

    @Override
    public
    void onItemClick(View view, int position)
    {
        List<FeedItemData> data;
        if (mFeedType == TYPE_FAVORITES) {
            data = Collections.singletonList(getAdapter().getData().get(position));
            position = 0;
        } else {
            data = getAdapter().getData();
        }
        mCallbacks.onFeedItemSelected(data, position);
    }

    @Override
    public
    void onCompleted()
    {
    }

    @Override
    public
    void onError(Throwable e)
    {
        e.printStackTrace();
    }

    @Override
    public
    void onNext(List<FeedItemData> feedItems)
    {
        getAdapter().fillWith(feedItems);
        crossfadeIntoListOrEmpty();
        mLoaded = true;
    }

}
