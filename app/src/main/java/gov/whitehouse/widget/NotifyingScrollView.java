package gov.whitehouse.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Based on implementation by Cyril Mottier
 * described here:
 * http://cyrilmottier.com/2013/05/24/pushing-the-actionbar-to-the-next-level/
 */
public
class NotifyingScrollView extends ScrollView
{

    private
    OnScrollChangedListener mOnScrollChangedListener;

    public
    NotifyingScrollView(Context context)
    {
        super(context);
    }

    public
    NotifyingScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public
    NotifyingScrollView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected
    void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    public
    void setOnScrollChangedListener(OnScrollChangedListener listener)
    {
        mOnScrollChangedListener = listener;
    }

    public
    interface OnScrollChangedListener
    {
        void onScrollChanged(ScrollView who, int l, int t, int oldL, int oldT);
    }
}
