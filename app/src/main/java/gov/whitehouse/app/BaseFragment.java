package gov.whitehouse.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.app.wh.DrawerFragment;
import gov.whitehouse.app.wh.MainActivity;
import gov.whitehouse.app.wh.WHApp;
import icepick.Icepick;
import rx.Subscription;

public abstract class BaseFragment extends Fragment {

    private
    List<Subscription> mSubscriptions = new ArrayList<>();

    public
    Subscription bindSubscription(Subscription s)
    {
        mSubscriptions.add(s);
        return s;
    }

    @Override
    public
    void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        onCreateToolbar(getToolbar());
    }

    @Override
    public
    void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (!(activity instanceof BaseActivity)) {
            throw new IllegalStateException("BaseFragment must attach to child of BaseActivity");
        }
    }

    public
    void onCreateToolbar(Toolbar toolbar)
    {
    }

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        Icepick.restoreInstanceState(this, icicle);
    }

    @Override
    public
    void onDestroy()
    {
        super.onDestroy();
        for (Subscription s : mSubscriptions) {
            if (s != null && !s.isUnsubscribed()) {
                s.unsubscribe();
            }
        }
        mSubscriptions.clear();
    }

    @Override
    public
    void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public
    void onStart()
    {
        super.onStart();
        if (getActivity() instanceof MainActivity && !(this instanceof DrawerFragment)) {
            if (isRootFragment()) {
                ((MainActivity) getActivity()).enableDrawer();
            } else {
                ((MainActivity) getActivity()).disableDrawer();
            }
        }
    }

    public
    boolean onToolbarMenuItemClicked(MenuItem item)
    {
        return false;
    }

    public
    Toolbar getToolbar()
    {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getToolbar();
        }
        return null;
    }

    public
    Tracker getTracker()
    {
        return ((WHApp) getActivity().getApplication()).getTracker();
    }

    public
    boolean isRootFragment()
    {
        return true;
    }

    public
    boolean shouldAddActionItems()
    {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).shouldAddActionItems();
        }
        return true;
    }
}
