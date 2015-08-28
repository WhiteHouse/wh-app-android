package gov.whitehouse.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import timber.log.Timber;

public
class LayeredScrollView extends NotifyingScrollView
{

    private
    OnScrollChangedListener mUserScrollListener;

    public
    LayeredScrollView(Context context)
    {
        super(context);
        init();
    }

    public
    LayeredScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public
    LayeredScrollView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private
    void init()
    {
        super.setOnScrollChangedListener(new LayeredScrollChangedListener());
    }

    @Override
    public
    void addView(@NonNull View child)
    {
        Timber.d(">> addView(" + child.getClass().getSimpleName() + ")");
        try {
            super.addView(child);
        } catch (IllegalStateException e) {
            if (!"ScrollView can host only one direct child".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    @Override
    public
    void addView(@NonNull View child, int index)
    {
        Timber.d(">> addView(" + child.getClass().getSimpleName() + ")");
        try {
            super.addView(child, index);
        } catch (IllegalStateException e) {
            if (!"ScrollView can host only one direct child".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    @Override
    public
    void addView(@NonNull View child, ViewGroup.LayoutParams params)
    {
        Timber.d(">> addView(" + child.getClass().getSimpleName() + ")");
        try {
            super.addView(child, params);
        } catch (IllegalStateException e) {
            if (!"ScrollView can host only one direct child".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    @Override
    public
    void addView(@NonNull View child, int index, ViewGroup.LayoutParams params)
    {
        Timber.d(">> addView(" + child.getClass().getSimpleName() + ")");
        try {
            super.addView(child, index, params);
        } catch (IllegalStateException e) {
            if (!"ScrollView can host only one direct child".equals(e.getMessage())) {
                throw e;
            }
        }
    }

    @Override
    protected
    void onLayout(boolean changed, int l, int t, int r, int b)
    {
        Timber.d(">> onLayout(%1$b, %2$d, %3$d, %4$d, %5$d)", changed, l, t, r, b);
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public
    void setOnScrollChangedListener(OnScrollChangedListener listener)
    {
        mUserScrollListener = listener;
    }

    private
    class LayeredScrollChangedListener implements OnScrollChangedListener
    {

        @Override
        public
        void onScrollChanged(ScrollView who, int l, int t, int oldL, int oldT)
        {
            if (mUserScrollListener != null) {
                mUserScrollListener.onScrollChanged(who, l, t, oldL, oldT);
            }
        }
    }
}
