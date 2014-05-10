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

import static gov.whitehouse.utils.FavoritesUtils.FAVORITE_VIDEOS;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.ui.activities.BaseActivity;
import gov.whitehouse.utils.FavoritesUtils;
import gov.whitehouse.utils.GATrackingManager;
import gov.whitehouse.utils.GsonUtils;

import java.util.Date;

import gov.whitehouse.R;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoPlayerActivity extends BaseActivity {

    public static final String ARG_IS_LIVE = "is_live";

    public static final String ARG_ITEM_JSON = "item_json";

    public static final String ARG_UP_TITLE = "up_title";

    private static String TAG = "VideoPlayerActivity";

    private boolean mFavorited;

    private boolean mIsLive;

    private FeedItem mFeedItem;

    private String mUpTitle;

    private MediaController mMediaController;

    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setTheme(R.style.Theme_WhiteHouse_Dark);

        setContentView(R.layout.video_player);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mIsLive = extras.getBoolean(ARG_IS_LIVE, false);
            mUpTitle = extras.getString(ARG_UP_TITLE);

            final String json = extras.getString(ARG_ITEM_JSON);
            if (json != null) {
                mFeedItem = GsonUtils.fromJson(json, FeedItem.class);
            }
        }

        getSupportActionBar().setTitle(mUpTitle.toUpperCase());

        mFavorited = FavoritesUtils.isFavorited(this, mFeedItem);

        mVideoView = (VideoView) findViewById(R.id.video);

        mMediaController = new MediaController(this);
        mMediaController.setMediaPlayer(mVideoView);

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                if (mIsLive) {
                    if (mFeedItem.getPubDate().after(new Date())) {
                        Toast.makeText(VideoPlayerActivity.this, R.string.live_toast_not_started,
                                Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            Toast.makeText(VideoPlayerActivity.this,
                                    R.string.live_toast_error_pre_honeycomb,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(VideoPlayerActivity.this,
                                    R.string.live_toast_error_post_honeycomb,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                } else {
                    Toast.makeText(VideoPlayerActivity.this,
                            R.string.video_toast_play_error,
                            Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            }
        });
        mVideoView.setMediaController(mMediaController);
        mVideoView.setVideoURI(Uri.parse(mFeedItem.getVideoLink().toString()));
        mVideoView.requestFocus();
        mVideoView.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = this.getSupportActionBar();

        getMenuInflater().inflate(R.menu.article, menu);

        actionBar.setLogo(R.drawable.logo_wh);
        actionBar.setIcon(R.drawable.ic_launcher);

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);

        MenuItem shareItem = menu.findItem(R.id.menu_share);
        Intent shareIntent;

        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFeedItem.getLink().toString());
        ShareActionProvider sap = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        sap.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        sap.setShareIntent(shareIntent);

        MenuItem favoriteItem = menu.findItem(R.id.menu_favorite);

        // hide favorite controls for live items
        if (mIsLive) {
            favoriteItem.setVisible(false);
        }

        if (mFavorited) {
            favoriteItem.setTitle(R.string.unfavorite);
            favoriteItem.setIcon(R.drawable.ic_favorite);
        } else {
            favoriteItem.setTitle(R.string.favorite);
            favoriteItem.setIcon(R.drawable.ic_unfavorite);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_favorite) {
            if (mFavorited) {
                FavoritesUtils.removeFromFavorites(this, FAVORITE_VIDEOS, mFeedItem);
                item.setTitle(R.string.favorite);
                item.setIcon(R.drawable.ic_unfavorite);
            } else {
                FavoritesUtils.addToFavorites(this, FAVORITE_VIDEOS, mFeedItem);
                item.setTitle(R.string.unfavorite);
                item.setIcon(R.drawable.ic_favorite);
            }
            mFavorited = !mFavorited;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void trackPageView() {
        GATrackingManager.getInstance().track(getTrackingPathComponent(),
                mFeedItem.getTitle());
    }
}
