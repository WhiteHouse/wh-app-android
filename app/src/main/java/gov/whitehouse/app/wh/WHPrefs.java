package gov.whitehouse.app.wh;

import android.content.Context;

import io.github.eddieringle.preffy.Preffy;

public
class WHPrefs
{

    public static
    interface Keys {

        public static final String FIRST_RUN = "pref:first_run";

        public static final String ANALYTICS_OPT_IN = "pref:analytics_opt_in";
    }

    public static
    Preffy<Boolean> isFirstRun;

    public static
    Preffy<Boolean> optInToAnalytics;

    static
    void initPrefs(Context ctx)
    {
        isFirstRun = Preffy.initBoolPref(ctx, Keys.FIRST_RUN, true);
        optInToAnalytics = Preffy.initBoolPref(ctx, Keys.ANALYTICS_OPT_IN, true);
    }
}
