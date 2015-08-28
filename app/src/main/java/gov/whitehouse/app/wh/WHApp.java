package gov.whitehouse.app.wh;

import android.app.Application;
import android.content.Intent;

import com.bugsnag.android.Bugsnag;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;

import gov.whitehouse.BuildConfig;
import timber.log.Timber;

import static com.google.android.gms.common.ConnectionResult.SUCCESS;

public
class WHApp extends Application
{
    private
    Tracker mAppTracker;

    public
    Tracker getTracker()
    {
        return mAppTracker;
    }

    private
    void configureAnalytics()
    {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == SUCCESS) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mAppTracker = analytics.newTracker(gov.whitehouse.R.xml.google_analytics);
            mAppTracker.enableAutoActivityTracking(true);
            mAppTracker.setAnonymizeIp(true);
        }
    }

    private
    void configureBugsnag()
    {
        Bugsnag.register(this, "77d9c523c3567a4537c341cb1dd06c09");
        Bugsnag.setReleaseStage(BuildConfig.BUILD_TYPE);
        Bugsnag.setProjectPackages("gov.whitehouse");
    }

    private
    void configureUrbanAirship()
    {
        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);
        options.inProduction = !BuildConfig.DEBUG;
        UAirship.takeOff(this, options);
        UAirship.shared().getPushManager().setUserNotificationsEnabled(true);
    }

    private
    void pokeLiveService()
    {
        final Intent sIntent = new Intent(this, LiveService.class);
        startService(sIntent);
    }

    private
    void watchPrefs()
    {
        GoogleAnalytics.getInstance(this).setAppOptOut(!WHPrefs.optInToAnalytics.getValue());
        WHPrefs.optInToAnalytics.watch().subscribe(optIn -> {
            GoogleAnalytics.getInstance(this).setAppOptOut(!optIn);
        });
    }

    @Override
    public
    void onCreate()
    {
        super.onCreate();

        configureBugsnag();
        configureUrbanAirship();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
        WHPrefs.initPrefs(this);
        watchPrefs();
        pokeLiveService();
        if (WHPrefs.optInToAnalytics.getValue()) {
            configureAnalytics();
        }
    }

    private static
    class CrashReportingTree extends Timber.HollowTree
    {
        private static final
        String SEVERITY_ERROR = "error";

        @Override
        public
        void e(Throwable t, String message, Object... args)
        {
            String infoString = String.format(message, args);
            if (t != null) {
                Bugsnag.notify(new Throwable(infoString, t), SEVERITY_ERROR);
            } else {
                Bugsnag.notify(new Throwable(infoString), SEVERITY_ERROR);
            }
        }
    }
}
