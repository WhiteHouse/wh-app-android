package gov.whitehouse.app.wh;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.util.PlayerControl;

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

import static android.media.MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT;
import static com.google.android.exoplayer.MediaCodecVideoTrackRenderer.MSG_SET_SURFACE;
import static gov.whitehouse.util.FavoritesUtils.FAVORITE_VIDEOS;

public
class VideoPlayerFragment extends BaseFragment
                          implements SurfaceHolder.Callback,
                                     ExoPlayer.Listener,
                                     MediaCodecVideoTrackRenderer.EventListener
{

    public static final String EXTRA_ITEM = "extra:item";

    public
    interface RendererBuilder
    {

        void buildRenderers(RendererBuilderCallback callback);
    }

    @InjectView(R.id.surface_view)
    VideoSurfaceView mSurfaceView;

    @InjectView(R.id.shutter)
    View mShutter;

    private
    FeedItem mFeedItem;

    private
    Menu mMenu;

    private
    MediaController mMediaController;

    private
    Handler mHandler;

    private
    ExoPlayer mPlayer;

    private
    RendererBuilder mBuilder;

    private
    RendererBuilderCallback mCallback;

    private
    MediaCodecVideoTrackRenderer mVideoRenderer;

    private
    boolean mAutoPlay = true;

    private
    long mPlayerPosition;

    public static
    VideoPlayerFragment newInstance(FeedItem item)
    {
        VideoPlayerFragment f = new VideoPlayerFragment();
        Bundle args = new Bundle(1);
        args.putParcelable(EXTRA_ITEM, item);
        f.setArguments(args);
        return f;
    }

    private
    Handler getMainHandler()
    {
        return mHandler;
    }

    private
    RendererBuilder getRendererBuilder()
    {
        return new DefaultRendererBuilder();
    }

    private
    void maybeStartPlayback()
    {
        Surface surface = mSurfaceView.getHolder().getSurface();
        if (mVideoRenderer == null || surface == null || !surface.isValid()) {
            return;
        }
        mPlayer.sendMessage(mVideoRenderer, MSG_SET_SURFACE, surface);
        if (mAutoPlay) {
            mPlayer.setPlayWhenReady(true);
            mAutoPlay = false;
        }
    }

    private
    void onError(Exception e)
    {
        Timber.e(e, "Playback failed");
        Toast.makeText(getActivity(), "Video playback failed", Toast.LENGTH_SHORT).show();
    }

    private
    void onRenderers(RendererBuilderCallback callback, MediaCodecVideoTrackRenderer videoRenderer,
                     MediaCodecAudioTrackRenderer audioRenderer)
    {
        if (mCallback != callback) {
            return;
        }
        mCallback = null;
        mVideoRenderer = videoRenderer;
        mPlayer.prepare(videoRenderer, audioRenderer);
        maybeStartPlayback();
    }

    private
    void onRenderersError(RendererBuilderCallback callback, Exception e)
    {
        if (mCallback != callback) {
            return;
        }
        mCallback = null;
        onError(e);
    }

    private
    void toggleControlsVisibility()
    {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show(0);
        }
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
    }

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        mFeedItem = getArguments().getParcelable(EXTRA_ITEM);
        mHandler = new Handler(Looper.getMainLooper());
        mBuilder = getRendererBuilder();
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
        View v = inflater.inflate(R.layout.fragment_video_player, container, false);
        ButterKnife.inject(this, v);
        v.setOnTouchListener((v1, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                toggleControlsVisibility();
            }
            return true;
        });
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
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    @Override
    public
    void onPause()
    {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayerPosition = mPlayer.getCurrentPosition();
            mPlayer.release();
            mPlayer = null;
        }
        mCallback = null;
        mVideoRenderer = null;
        mShutter.setVisibility(View.VISIBLE);
    }

    @Override
    public
    void onResume()
    {
        super.onResume();
        mPlayer = ExoPlayer.Factory.newInstance(2, 1000, 5000);
        mPlayer.addListener(this);
        mPlayer.seekTo(mPlayerPosition);
        mMediaController.setMediaPlayer(new PlayerControl(mPlayer));
        mMediaController.setEnabled(true);
        mCallback = new RendererBuilderCallback();
        mBuilder.buildRenderers(mCallback);
    }

    @Override
    public
    void onStart()
    {
        super.onStart();
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
        if (mMediaController != null && mMediaController.isShowing()) {
            mMediaController.hide();
        }
    }

    @Override
    public
    void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mMediaController = new MediaController(view.getContext());
        mMediaController.setAnchorView(view);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public
    void surfaceCreated(SurfaceHolder holder)
    {
        maybeStartPlayback();
    }

    @Override
    public
    void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public
    void surfaceDestroyed(SurfaceHolder holder)
    {
        if (mVideoRenderer != null) {
            mPlayer.blockingSendMessage(mVideoRenderer, MSG_SET_SURFACE, null);
        }
    }

    @Override
    public
    void onDroppedFrames(int count, long elapsed)
    {
        Timber.d("Dropped frames: %d", count);
    }

    @Override
    public
    void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio)
    {
        mSurfaceView.setVideoWidthHeightRatio(
                height == 0 ? 1 : (pixelWidthHeightRatio * width) / height);
    }

    @Override
    public
    void onDrawnToSurface(Surface surface)
    {
        mShutter.setVisibility(View.GONE);
    }

    @Override
    public
    void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e)
    {

    }

    @Override
    public
    void onCryptoError(MediaCodec.CryptoException e)
    {

    }

    @Override
    public
    void onPlayerStateChanged(boolean playWhenReady, int playbackState)
    {
    }

    @Override
    public
    void onPlayWhenReadyCommitted()
    {
    }

    @Override
    public
    void onPlayerError(ExoPlaybackException error)
    {
        onError(error);
    }

    final
    class DefaultRendererBuilder implements RendererBuilder
    {

        @Override
        public
        void buildRenderers(RendererBuilderCallback callback)
        {
            FrameworkSampleSource ss = new FrameworkSampleSource(getActivity(),
                                                                 Uri.parse(mFeedItem.videoLink()),
                                                                 null,
                                                                 2);
            MediaCodecVideoTrackRenderer videoRenderer =
                    new MediaCodecVideoTrackRenderer(ss, VIDEO_SCALING_MODE_SCALE_TO_FIT,
                                                     0, getMainHandler(),
                                                     VideoPlayerFragment.this, 50);
            MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(ss);
            callback.onRenderers(videoRenderer, audioRenderer);
        }
    }

    final
    class RendererBuilderCallback
    {
        public
        void onRenderers(MediaCodecVideoTrackRenderer videoRenderer,
                         MediaCodecAudioTrackRenderer audioRenderer)
        {
            VideoPlayerFragment.this.onRenderers(this, videoRenderer, audioRenderer);
        }

        public
        void onRenderersError(Exception e)
        {
            VideoPlayerFragment.this.onRenderersError(this, e);
        }
    }
}
