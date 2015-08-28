package gov.whitehouse.widget.wh;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gov.whitehouse.R;
import gov.whitehouse.app.wh.DrawerFragment;
import gov.whitehouse.app.wh.LiveService;
import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.Bindable;
import rx.android.schedulers.AndroidSchedulers;

public
class DrawerItemView extends FrameLayout implements Bindable<MenuItem>
{

    @InjectView(R.id.selector_frame)
    FrameLayout mSelectorFrame;

    @InjectView(R.id.badge)
    TextView mBadge;

    @InjectView(R.id.title)
    TextView mTitle;

    public
    DrawerItemView(Context context)
    {
        super(context);
        init();
    }

    public
    DrawerItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public
    DrawerItemView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private
    void init()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.item_drawer, this, true);
        ButterKnife.inject(this);
    }

    @Override
    public
    void onBindWith(MenuItem data, int position)
    {
        if (data.getGroupId() == DrawerFragment.GROUP_LIVE) {
            LiveService.observeLiveItemCount().observeOn(AndroidSchedulers.mainThread())
                    .subscribe(num -> {
                        if (num > 0) {
                            mBadge.setVisibility(View.VISIBLE);
                        } else {
                            mBadge.setVisibility(View.GONE);
                        }
                        mBadge.setText(Integer.toString(num));
                    });
        } else {
            mBadge.setVisibility(View.GONE);
        }
        mTitle.setText(data.getTitle());
        mTitle.setTypeface(Typeface.SERIF);
    }

    @Override
    public
    void onSetViewClickListener(BaseAdapter.OnItemClickListener listener, int position)
    {
        setOnClickListener(v -> listener.onItemClick(this, position));
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
