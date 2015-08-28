package gov.whitehouse.app.wh;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gov.whitehouse.R;
import gov.whitehouse.app.BaseFragment;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.util.FavoritesUtils;
import gov.whitehouse.util.GsonUtils;
import icepick.Icicle;
import rx.Observable;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public
class ArticleViewFragment extends BaseFragment
{

    public static final String EXTRA_FEED_ITEM = "extra:feed_item";

    private static final String PAGE_TEMPLATE_URL = "file:///android_asset/post.html";

    private
    FeedItem mFeedItem;

    private
    Menu mMenu;

    private
    String mPageJson;

    @Icicle
    boolean mFavorited;

    @InjectView(R.id.webview)
    WebView mWebView;

    public static
    ArticleViewFragment newInstance(FeedItem feed_item)
    {
        ArticleViewFragment f = new ArticleViewFragment();
        Bundle b = new Bundle(1);
        b.putParcelable(EXTRA_FEED_ITEM, feed_item);
        f.setArguments(b);
        return f;
    }

    private
    String getPageJson(FeedItem item)
    {
        PageInfo info = new PageInfo();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a");

        info.title = item.title();
        info.date = dateFormat.format(item.pubDate());
        info.creator = item.creator();
        info.description = item.description();
        info.url = item.link();

        return GsonUtils.toJson(info);
    }

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        Bundle args = getArguments();
        mFeedItem = args.getParcelable(EXTRA_FEED_ITEM);
        mPageJson = getPageJson(mFeedItem);
        mFavorited = mFeedItem.favorited();
        setHasOptionsMenu(true);
    }

    @Override
    public
    void onDestroy()
    {
        super.onDestroy();
        if (getArguments() != null) {
            getArguments().putParcelable(EXTRA_FEED_ITEM, mFeedItem);
        }
    }

    @Override
    public
    View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_article, container, false);
        ButterKnife.inject(this, v);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public
            boolean onConsoleMessage(@NonNull ConsoleMessage cm)
            {
                Timber.d("%s -- From line %d of %s", cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public
            void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                if (PAGE_TEMPLATE_URL.equals(url)) {
                    view.loadUrl(String.format("javascript:WhiteHouse.loadPage(%s);", mPageJson));
                }
            }

            @Override
            public
            boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if (url.contains("youtube.com/")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        return v;
    }

    @Override
    public
    void onDestroyView()
    {
        if (mWebView != null) {
            mWebView.destroy();
        }
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    private
    void configureOverflow(boolean favorited)
    {
        MenuItem favoriteItem, unfavoriteItem, shareItem;
        ShareActionProvider shareAction;
        Intent shareIntent;

        mFavorited = favorited;
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
            configureOverflow(mFavorited);
        }
    }

    private
    void onFavoriteItemSelected(boolean favorited)
    {
        Observable<Boolean> obsAction;
        final String favoritesType;

        switch (mFeedItem.type()) {
            default:
            case TYPE_ARTICLE:
                favoritesType = FavoritesUtils.FAVORITE_ARTICLES;
                break;
            case TYPE_PHOTO:
                favoritesType = FavoritesUtils.FAVORITE_PHOTOS;
                break;
            case TYPE_VIDEO:
                favoritesType = FavoritesUtils.FAVORITE_VIDEOS;
                break;
        }
        if (favorited) {
            obsAction = Observable.create((Subscriber<? super Boolean> subscriber) -> {
                try {
                    FavoritesUtils.addToFavorites(getActivity(), favoritesType, mFeedItem);
                    mFavorited = true;
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
                    FavoritesUtils.removeFromFavorites(getActivity(), favoritesType, mFeedItem);
                    mFavorited = false;
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
    void onPause()
    {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    @Override
    public
    void onResume()
    {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
        if (getTracker() != null) {
            getTracker().setScreenName("ArticleView");
            getTracker().setPage(mFeedItem.link());
        }
    }

    @Override
    public
    void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    public
    void onStart()
    {
        super.onStart();
        mWebView.loadUrl(PAGE_TEMPLATE_URL);
        if (mMenu == null) {
            onCreateToolbar(getToolbar());
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
    void onViewStateRestored(@Nullable Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }

    @Override
    public
    boolean isRootFragment()
    {
        return false;
    }

    private static
    class PageInfo
    {
        public
        String title;

        public
        String date;

        public
        String creator;

        public
        String description;

        public
        String url;
    }
}
