package gov.whitehouse.core.manager;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import gov.whitehouse.core.FeedHandler;
import gov.whitehouse.data.model.FavoritesMap;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.data.model.FeedType;
import gov.whitehouse.util.FavoritesUtils;
import gov.whitehouse.util.GsonUtils;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public
class FeedManager
{
    private static OkHttpClient sOkHttpClient;

    private static final
    BehaviorSubject<Observable<List<FeedItem>>> sFavoritesSubject = BehaviorSubject.create();

    private static final
    Map<String, BehaviorSubject<Observable<List<FeedItem>>>> sFeedItemsSubject = new HashMap<>();

    private static
    OkHttpClient getClient()
    {
        if (sOkHttpClient == null) {
            sOkHttpClient = new OkHttpClient();
        }
        return sOkHttpClient;
    }

    private static
    Request.Builder getBaseRequest()
    {
        return new Request.Builder()
                .header("User-Agent", "White House App/Android");
    }

    public static
    void updateFeedFromServer(String url, String title, String viewType)
    {
        final BehaviorSubject<Observable<List<FeedItem>>> subject;
        final OkHttpClient client = getClient();
        final Request request = getBaseRequest()
                .url(url)
                .get()
                .build();
        if (sFeedItemsSubject.get(url) == null) {
            sFeedItemsSubject.put(url, BehaviorSubject.create());
        }
        subject = sFeedItemsSubject.get(url);
        subject.onNext(Observable.create((Subscriber<? super List<FeedItem>> op) -> {
            Response response;
            try {
                response = client.newCall(request).execute();
                if (response.code() >= 400) {
                    op.onError(new IOException("Response code " + Integer.toString(response.code())));
                } else {
                    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                    FeedHandler handler = new FeedHandler(title, viewType);
                    parser.parse(response.body().byteStream(), handler);
                    op.onNext(handler.getFeedItems());
                }
                op.onCompleted();
            } catch (Exception e) {
                op.onError(e);
            }
        }));
    }

    private static
    void processAndStoreFavorites(List<FeedItem> items, List<FeedItem> dest, FeedType type)
    {
        for (FeedItem item : items) {
            dest.add(FeedItem.create(item.creator(),
                                     item.description(),
                                     true,
                                     item.feedTitle(),
                                     item.guid(),
                                     item.link(),
                                     item.pubDate(),
                                     item.thumbnails(),
                                     item.title(),
                                     type,
                                     item.videoLink()));
        }
    }

    public static
    void updateFavorites(Context ctx)
    {
        sFavoritesSubject.onNext(
                Observable.create((Subscriber<? super List<FeedItem>> op) -> {
                    final FavoritesMap map;
                    final InputStreamReader isr;
                    final List<FeedItem> favorites = new ArrayList<>();
                    final Gson gson = GsonUtils.createGsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create();

                    try {
                        isr = new InputStreamReader(FavoritesUtils.getInputStream(ctx));
                        map = gson.fromJson(isr, FavoritesMap.class);
                        if (map.articles() != null) {
                            processAndStoreFavorites(map.articles(), favorites, FeedType.TYPE_ARTICLE);
                        }
                        if (map.photos() != null) {
                            processAndStoreFavorites(map.photos(), favorites, FeedType.TYPE_PHOTO);
                        }
                        if (map.videos() != null) {
                            processAndStoreFavorites(map.videos(), favorites, FeedType.TYPE_VIDEO);
                        }
                        Collections.sort(favorites, (lhs, rhs) -> {
                            if (lhs.feedTitle() != null && rhs.feedTitle() != null) {
                                return lhs.feedTitle().compareTo(rhs.feedTitle());
                            }
                            return 1;
                        });
                    } catch (FileNotFoundException ignored) {
                    }
                    op.onNext(favorites);
                    op.onCompleted();
                }).subscribeOn(Schedulers.io())
        );
    }

    public static
    Observable<List<FeedItem>> observeFavorites()
    {
        return Observable.switchOnNext(sFavoritesSubject);
    }

    public static
    Observable<List<FeedItem>> observeFeedItems(String url)
    {
        if (sFeedItemsSubject.get(url) == null) {
            sFeedItemsSubject.put(url, BehaviorSubject.create());
        }
        return Observable.switchOnNext(sFeedItemsSubject.get(url));
    }
}
