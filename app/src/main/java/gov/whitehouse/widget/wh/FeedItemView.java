package gov.whitehouse.widget.wh;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gov.whitehouse.R;
import gov.whitehouse.core.FeedItemData;
import gov.whitehouse.data.model.FeedType;
import gov.whitehouse.util.DateUtils;
import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.Bindable;

public
class FeedItemView extends FrameLayout implements Bindable<FeedItemData>
{

    public static final
    SimpleDateFormat ITEM_DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy");

    public static final
    SimpleDateFormat ITEM_TIME_FORMAT = new SimpleDateFormat("h:mm a");

    public static
    int sVideosTitleMinLines = 0;

    @InjectView(R.id.card)
    CardView mCardView;

    @InjectView(R.id.selector_frame)
    FrameLayout mSelectorFrame;

    @InjectView(R.id.thumbnail_frame)
    FrameLayout mThumbnailFrame;

    @InjectView(R.id.thumbnail)
    ImageView mThumbnail;

    @InjectView(R.id.date_header)
    TextView mDateHeader;

    @InjectView(R.id.happening_badge)
    TextView mHappeningBadge;

    @InjectView(R.id.meta)
    TextView mMeta;

    @InjectView(R.id.title)
    TextView mTitle;

    @InjectView(R.id.meta_container)
    ViewGroup mMetaContainer;

    public
    FeedItemView(Context context)
    {
        super(context);
        init();
    }

    public
    FeedItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public
    FeedItemView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private
    void init()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.item_feed, this, true);
        ButterKnife.inject(this);
        if (sVideosTitleMinLines < 1) {
            sVideosTitleMinLines = getResources().getInteger(R.integer.video_item_title_minLines);
        }
    }

    private
    void bindArticle(FeedItemData data, int position)
    {
        if (data.item.pubDate() != null) {
            mMeta.setText(ITEM_TIME_FORMAT.format(data.item.pubDate()));
        } else {
            mMeta.setVisibility(GONE);
        }
        if (FeedType.TYPE_VIDEO.equals(data.item.type())) {
            mTitle.setMinLines(sVideosTitleMinLines);
        } else {
            mTitle.setMinLines(1);
        }
        mTitle.setText(data.item.title());
        mTitle.setTypeface(Typeface.SERIF);
        mMetaContainer.setVisibility(View.VISIBLE);
        if (data.shouldShowDate) {
            mDateHeader.setVisibility(View.VISIBLE);
            if (data.todayMarker) {
                mDateHeader.setText("Today");
            } else if (DateUtils.isYesterday(data.item.pubDate())) {
                mDateHeader.setText("Yesterday");
            } else {
                mDateHeader.setText(ITEM_DATE_FORMAT.format(data.item.pubDate()));
            }
        } else {
            mDateHeader.setVisibility(View.GONE);
        }
    }

    private
    void bindPhoto(FeedItemData data, int position)
    {
        mMetaContainer.setVisibility(View.GONE);
    }

    private
    void bindVideo(FeedItemData data, int position)
    {
        mThumbnailFrame.setForeground(
                getResources().getDrawable(R.drawable.ic_av_play_circle_outline));
        bindArticle(data, position);
    }

    @Override
    public
    void onBindWith(FeedItemData data, int position)
    {
        final String thumbnailUrl;

        mThumbnailFrame.setForeground(null);
        thumbnailUrl = data.item.getBestThumbnailUrl(1080);
        if (thumbnailUrl != null) {
            mThumbnail.setVisibility(View.VISIBLE);
            Picasso.with(getContext())
                   .load(thumbnailUrl)
                   .error(R.drawable.error_thumbnail)
                   .fit()
                   .centerCrop()
                   .into(mThumbnail);
        } else {
            mThumbnail.setVisibility(View.GONE);
        }
        mDateHeader.setVisibility(View.GONE);
        mHappeningBadge.setVisibility(View.GONE);
        if (data.item.type().equals(FeedType.TYPE_LIVE)) {
            mDateHeader.setVisibility(View.VISIBLE);
            if (data.historicMarker) {
                mDateHeader.setText("Previously");
            } else if (data.todayMarker) {
                mDateHeader.setText("Today");
            } else if (data.futureMarker) {
                mDateHeader.setText("Upcoming");
            } else {
                mDateHeader.setVisibility(View.GONE);
            }
            if (DateUtils.within30MinutesBeforeNow(data.item.pubDate())) {
                mHappeningBadge.setVisibility(View.VISIBLE);
            }
            bindArticle(data, position);
        } else if (data.item.type().equals(FeedType.TYPE_PHOTO)) {
            bindPhoto(data, position);
        } else if (data.item.type().equals(FeedType.TYPE_VIDEO)) {
            bindVideo(data, position);
        } else {
            bindArticle(data, position);
        }
    }

    @Override
    public
    void onSetViewClickListener(BaseAdapter.OnItemClickListener listener, int position)
    {
        mSelectorFrame.setOnClickListener(v -> listener.onItemClick(mSelectorFrame, position));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public
    void drawableHotspotChanged(float x, float y)
    {
        super.drawableHotspotChanged(x, y);
        if (mSelectorFrame.getForeground() != null) {
            mSelectorFrame.getForeground().setHotspot(x, y);
        }
    }
}
