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

import static gov.whitehouse.core.FeedItem.TYPE_ARTICLE;
import static gov.whitehouse.core.FeedItem.TYPE_PHOTO;
import static gov.whitehouse.ui.fragments.app.ArticleListFragment.ARG_FEED_TYPE;
import static gov.whitehouse.ui.fragments.app.ArticleListFragment.ARTICLE_TYPE_FAVORITES;
import static gov.whitehouse.ui.fragments.app.ArticleListFragment.ARTICLE_TYPE_FEED;
import static gov.whitehouse.utils.FavoritesUtils.FAVORITE_ARTICLES;
import static gov.whitehouse.utils.FavoritesUtils.FAVORITE_PHOTOS;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.ui.activities.BaseActivity;
import gov.whitehouse.ui.fragments.BaseFragment;
import gov.whitehouse.utils.FavoritesUtils;
import gov.whitehouse.utils.GsonUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import gov.whitehouse.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class ArticleViewerFragment extends BaseFragment {

    public static final String ARG_ITEM_JSON = "item_json";

    public static final String ARG_UP_TITLE = "up_title";

    private static String TAG = "ArticleViewerActivity";

    private static String PAGE_TEMPLATE_URL = "file:///android_asset/post.html";

    private int mArticleType;

    private boolean mFavorited;

    private JSONObject mPageInfo;

    private FeedItem mFeedItem;

    private String mUpTitle;

    private ProgressBar mProgressBar;

    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.article_view, container, false);

        mWebView = (WebView) v.findViewById(R.id.webview);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress);

        mWebView.getSettings().setJavaScriptEnabled(true);
        // enabling plugins may yield a better result for YouTube videos
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG,
                        cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
                return true;
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                    String failingUrl) {
                Log.e(TAG, "Error in WebView: " + failingUrl + "; " + description);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mWebView.setVisibility(View.VISIBLE);
                Log.d(TAG, "page finished: " + url);

                if (url.equals(PAGE_TEMPLATE_URL)) {
                    view.loadUrl(String.format("javascript:WhiteHouse.loadPage(%s);",
                            mPageInfo.toString()));
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("youtube.com/")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();

        mPageInfo = new JSONObject();
        try {
            mPageInfo.put("title", args.getString("title"));
            final SimpleDateFormat date = new SimpleDateFormat("MMM d, yyyy h:mm a");
            final DateFormat parse = DateFormat.getDateTimeInstance();
            mPageInfo.put("date", date.format(parse.parse(args.getString("date"))));
            mPageInfo.put("creator", args.getString("creator"));
            mPageInfo.put("description", args.getString("description"));
            mPageInfo.put("url", args.getString("url"));
            mWebView.loadUrl(PAGE_TEMPLATE_URL);
        } catch (JSONException e) {
            Log.e(TAG, "error creating JSON object for post", e);
        } catch (ParseException e) {
        }

        mArticleType = args.getInt(ARG_FEED_TYPE, ARTICLE_TYPE_FEED);
        mUpTitle = args.getString(ARG_UP_TITLE);
        if (!((BaseActivity) getActivity()).isMultipaned()) {
            switch (mArticleType) {
                case ARTICLE_TYPE_FEED:
                    ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(mUpTitle.toUpperCase());
                    break;
                case ARTICLE_TYPE_FAVORITES:
                    ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(R.string.favorites);
                    break;
            }
        }

        final String json = args.getString(ARG_ITEM_JSON);
        if (json != null) {
            mFeedItem = GsonUtils.fromJson(json, FeedItem.class);
        }

        mFavorited = FavoritesUtils.isFavorited(getActivity(), mFeedItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        if (!((BaseActivity) getActivity()).isMultipaned()) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
        }

        MenuItem shareItem = menu.findItem(R.id.menu_share);
        shareItem.setVisible(true);
        Intent shareIntent;
        try {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mPageInfo.getString("url"));
            ShareActionProvider sap = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
            sap.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
            sap.setShareIntent(shareIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MenuItem favoriteItem = menu.findItem(R.id.menu_favorite);
        favoriteItem.setVisible(true);
        if (mFavorited) {
            favoriteItem.setTitle(R.string.unfavorite);
            favoriteItem.setIcon(R.drawable.ic_favorite);
        } else {
            favoriteItem.setTitle(R.string.favorite);
            favoriteItem.setIcon(R.drawable.ic_unfavorite);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_favorite) {
            if (mFavorited) {
                switch (mArticleType) {
                    case ARTICLE_TYPE_FEED:
                        FavoritesUtils.removeFromFavorites(getActivity(), FAVORITE_ARTICLES,
                                mFeedItem);
                        break;
                    case ARTICLE_TYPE_FAVORITES:
                        if (mFeedItem.getType() == TYPE_ARTICLE) {
                            FavoritesUtils
                                    .removeFromFavorites(getActivity(), FAVORITE_ARTICLES,
                                            mFeedItem);
                        } else if (mFeedItem.getType() == TYPE_PHOTO) {
                            FavoritesUtils
                                    .removeFromFavorites(getActivity(), FAVORITE_PHOTOS,
                                            mFeedItem);
                        }
                        break;
                }
                item.setTitle(R.string.favorite);
                item.setIcon(R.drawable.ic_unfavorite);
            } else {
                switch (mArticleType) {
                    case ARTICLE_TYPE_FEED:
                        FavoritesUtils.addToFavorites(getActivity(), FAVORITE_ARTICLES,
                                mFeedItem);
                        break;
                    case ARTICLE_TYPE_FAVORITES:
                        if (mFeedItem.getType() == TYPE_ARTICLE) {
                            FavoritesUtils.addToFavorites(getActivity(), FAVORITE_ARTICLES,
                                    mFeedItem);
                        } else if (mFeedItem.getType() == TYPE_PHOTO) {
                            FavoritesUtils.addToFavorites(getActivity(), FAVORITE_PHOTOS,
                                    mFeedItem);
                        }
                        break;
                }
                item.setTitle(R.string.unfavorite);
                item.setIcon(R.drawable.ic_favorite);
            }
            mFavorited = !mFavorited;
        }
        return super.onOptionsItemSelected(item);
    }
}
