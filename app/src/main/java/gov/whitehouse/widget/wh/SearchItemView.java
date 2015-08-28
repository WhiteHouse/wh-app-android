package gov.whitehouse.widget.wh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gov.whitehouse.R;
import gov.whitehouse.data.model.SearchResult;
import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.Bindable;

public
class SearchItemView extends FrameLayout implements Bindable<SearchResult>
{

    @InjectView(R.id.selector_frame)
    FrameLayout mSelectorFrame;

    @InjectView(R.id.title)
    TextView mTitle;

    public
    SearchItemView(Context context)
    {
        super(context);
        init();
    }

    public
    SearchItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public
    SearchItemView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private
    void init()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.item_search, this, true);
        ButterKnife.inject(this);
    }

    @Override
    public
    void onBindWith(SearchResult data, int position)
    {
        mTitle.setText(data.title());
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
