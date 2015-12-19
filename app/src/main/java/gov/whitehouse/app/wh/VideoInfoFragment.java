package gov.whitehouse.app.wh;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.common.base.Strings;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gov.whitehouse.R;
import gov.whitehouse.app.BaseFragment;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.util.FavoritesUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static gov.whitehouse.util.FavoritesUtils.FAVORITE_VIDEOS;

public
class VideoInfoFragment extends BaseFragment
{

    public static final String EXTRA_ITEM = "extra:item";

    @InjectView(R.id.play)
    Button mPlayButton;

    @InjectView(R.id.thumbnail)
    ImageView mThumbnail;

    @InjectView(R.id.title)
    TextView mTitle;

    private
    FeedItem mFeedItem;

    private
    Menu mMenu;

    private
    Picasso mPicasso;

    public static
    VideoInfoFragment newInstance(FeedItem item)
    {
        VideoInfoFragment f = new VideoInfoFragment();
        Bundle args = new Bundle(1);
        args.putParcelable(EXTRA_ITEM, item);
        f.setArguments(args);
        return f;
    }

    @Override
    public
    boolean isRootFragment()
    {
        return false;
    }

    @Override
    public
    void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mPicasso = Picasso.with(activity);
    }

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        mFeedItem = getArguments().getParcelable(EXTRA_ITEM);
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
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, mFeedItem.title());
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFeedItem.link());
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
            configureOverflow(mFeedItem.favorited());
        }
    }

    private
    void onFavoriteItemSelected(boolean favorited)
    {
        Observable<Boolean> obsAction;

        if (favorited) {
            obsAction = Observable.create((Subscriber<? super Boolean> subscriber) -> {
                try {
                    FavoritesUtils.addToFavorites(getActivity(), FAVORITE_VIDEOS, mFeedItem);
                    mFeedItem = new FeedItem.Builder(mFeedItem).setFavorited(true).build();
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
                    FavoritesUtils.removeFromFavorites(getActivity(), FAVORITE_VIDEOS, mFeedItem);
                    mFeedItem = new FeedItem.Builder(mFeedItem).setFavorited(false).build();
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
        View v = inflater.inflate(R.layout.fragment_video_info, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public
    void onDestroy()
    {
        if (getArguments() != null) {
            getArguments().putParcelable(EXTRA_ITEM, mFeedItem);
        }
        super.onDestroy();
    }

    @Override
    public
    void onDestroyView()
    {
        if (mPicasso != null) {
            mPicasso.cancelRequest(mThumbnail);
        }
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override
    public
    void onStart()
    {
        super.onStart();
        if (mMenu == null) {
            onCreateToolbar(getToolbar());
        }
        mTitle.setText(mFeedItem.displayTitle());
        mTitle.setTypeface(Typeface.SERIF);
        mPicasso.load(mFeedItem.getBestThumbnailUrl(1080))
                .error(R.drawable.error_thumbnail)
                .fit()
                .centerCrop()
                .into(mThumbnail);
        if (!Strings.isNullOrEmpty(mFeedItem.videoLink())) {
            if (mFeedItem.isYouTubeVideo()) {
                mPlayButton.setText("Play on YouTube");
            } else {
                mPlayButton.setText("Play video");
            }
            mPlayButton.setOnClickListener(v -> {
                if (getTracker() != null) {
                    getTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("VideoInfo")
                    .setAction("playClick")
                    .setLabel(mFeedItem.videoLink())
                    .build());
                }
                Intent playIntent = new Intent(Intent.ACTION_VIEW);
                playIntent.setData(Uri.parse(mFeedItem.videoLink()));
                startActivity(playIntent);
            });
            if (getTracker() != null) {
                getTracker().setScreenName("VideoInfo");
                getTracker().setPage(mFeedItem.videoLink());
            }
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
}
