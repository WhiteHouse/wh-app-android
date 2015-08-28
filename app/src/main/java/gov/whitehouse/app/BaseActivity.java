package gov.whitehouse.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.bugsnag.android.Bugsnag;
import com.google.android.gms.analytics.Tracker;

import gov.whitehouse.app.wh.NoConnActivity;
import gov.whitehouse.app.wh.WHApp;
import gov.whitehouse.util.NetworkUtils;
import icepick.Icepick;

public abstract class BaseActivity extends ActionBarActivity {

    public static final int NO_LAYOUT = 0;

    public
    boolean checkNetworkElseFail()
    {
        if (!(this instanceof NoConnActivity)) {
            if (!NetworkUtils.checkNetworkAvailable(this)) {
                startActivity(new Intent(this, NoConnActivity.class));
                finish();
                return false;
            }
        }
        return true;
    }

    public
    Tracker getTracker()
    {
        return ((WHApp) getApplication()).getTracker();
    }

    protected void
    onCreate(Bundle icicle, int layoutRes)
    {
        super.onCreate(icicle);
        Bugsnag.onActivityCreate(this);
        Icepick.restoreInstanceState(this, icicle);
        setContentView(layoutRes);
    }

    @Override
    protected void
    onCreate(Bundle savedInstanceState)
    {
        onCreate(savedInstanceState, NO_LAYOUT);
    }

    @Override
    protected
    void onDestroy()
    {
        super.onDestroy();
        Bugsnag.onActivityDestroy(this);
    }

    @Override
    protected
    void onPause()
    {
        super.onPause();
        Bugsnag.onActivityPause(this);
    }

    @Override
    protected
    void onResume()
    {
        super.onResume();
        Bugsnag.onActivityResume(this);
    }

    @Override
    protected void
    onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
