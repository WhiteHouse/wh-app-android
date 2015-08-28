package gov.whitehouse.widget.wh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import gov.whitehouse.R;

public
class DrawerHeaderView extends FrameLayout
{

    public
    DrawerHeaderView(Context context)
    {
        super(context);
        init();
    }

    public
    DrawerHeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public
    DrawerHeaderView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private
    void init()
    {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.header_logo, this, false);
        ViewGroup.LayoutParams lps = header.getLayoutParams();
        if (lps == null) {
            lps = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                   ViewGroup.LayoutParams.MATCH_PARENT);
        }
        addView(header, lps);
        setEnabled(false);
        setClickable(false);
    }
}
