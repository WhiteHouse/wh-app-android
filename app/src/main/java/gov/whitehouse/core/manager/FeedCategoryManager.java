package gov.whitehouse.core.manager;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;

import gov.whitehouse.BuildConfig;
import gov.whitehouse.core.Constants;
import gov.whitehouse.data.model.FeedCategoryConfig;
import gov.whitehouse.util.GsonUtils;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import rx.Observable;

public
class FeedCategoryManager
{

    private static
    FeedCategoryConfig sCachedConfig = null;

    public static
    FeedCategoryManager get()
    {
        return new FeedCategoryManager();
    }

    private static
    RestAdapter getRestAdapter()
    {
        Gson gson = GsonUtils.createGsonBuilder()
                             .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
                             .create();
        return new RestAdapter.Builder()
                .setEndpoint(Constants.HOST)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    private
    Observable<FeedCategoryConfig> getFeedCategoryConfigFromNetwork()
    {
        FeedCategoryService ds;
        RestAdapter ra = getRestAdapter();
        if (BuildConfig.DEBUG) {
            ra.setLogLevel(RestAdapter.LogLevel.FULL);
        }
        ds = ra.create(FeedCategoryService.class);
        return ds.getConfig().first();
    }

    public
    Observable<FeedCategoryConfig> getFeedCategoryConfig()
    {
        if (sCachedConfig != null) {
            return Observable.just(sCachedConfig);
        }
        return getFeedCategoryConfigFromNetwork().doOnEach(notification -> {
            if (notification.isOnNext()) {
                sCachedConfig = (FeedCategoryConfig) notification.getValue();
            }
        });
    }

    private static
    interface FeedCategoryService
    {

        @GET("/sites/default/files/feeds/config.json")
        Observable<FeedCategoryConfig> getConfig();
    }
}
