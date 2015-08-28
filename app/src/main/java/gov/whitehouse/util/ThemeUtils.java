package gov.whitehouse.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.util.TypedValue;

import gov.whitehouse.R;

public
class ThemeUtils {

    public static
    Drawable getDrawableFromAttribute(Context ctx, @AttrRes int attr)
    {
        int[] attrs = new int[] { attr };
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        Drawable d = ta.getDrawable(0);
        ta.recycle();
        return d;
    }

    public static
    int getDimenFromAttribute(Context ctx, @AttrRes int attr)
    {
        int[] attrs = new int[] { attr };
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        int d = ta.getDimensionPixelSize(0, 0);
        ta.recycle();;
        return d;
    }

    public static
    int getColorFromAttribute(Context ctx, @AttrRes int attr)
    {
        int[] attrs = new int[] { attr };
        TypedArray ta = ctx.obtainStyledAttributes(attrs);
        int c = ta.getColor(0, -1);
        ta.recycle();
        if (c == -1) {
            throw new IllegalArgumentException("getColorFromAttribute requires an attribute pointing to a color resource");
        }
        return c;
    }

    public static
    float dpToPx(Resources r, int dp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static
    int dimenInPx(Resources r, int dimen)
    {
        return r.getDimensionPixelSize(dimen);
    }

    public static
    int getLeftDrawerWidth(Context ctx)
    {
        Configuration config = ctx.getResources().getConfiguration();
        int screenWidthDp = Math.round(dpToPx(ctx.getResources(), config.smallestScreenWidthDp));
        int toolbarHeight = getDimenFromAttribute(ctx, R.attr.actionBarSize);
        return Math.min(screenWidthDp - toolbarHeight,
                        dimenInPx(ctx.getResources(), R.dimen.drawer_max_width));
    }
}
