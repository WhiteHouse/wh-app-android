package gov.whitehouse.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import gov.whitehouse.BuildConfig;

public
class HackyViewPager extends ViewPager
{
    public
    HackyViewPager(Context context)
    {
        super(context);
    }

    public
    HackyViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public
    boolean onInterceptTouchEvent(MotionEvent ev)
    {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
