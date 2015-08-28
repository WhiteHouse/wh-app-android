package gov.whitehouse.util;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import gov.whitehouse.data.model.FavoritesMap;
import gov.whitehouse.data.model.FeedItem;
import timber.log.Timber;

public
class FavoritesUtils
{

    public static final String FAVORITE_ARTICLES = "articles";

    public static final String FAVORITE_PHOTOS = "photos";

    public static final String FAVORITE_VIDEOS = "videos";

    public static final String FAVORITES_FILE = "favorites_map.json";

    private static final
    FieldNamingPolicy sFieldNamingPolicy = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

    private static Gson sGson = null;

    public static
    FileInputStream getInputStream(final Context context)
            throws FileNotFoundException
    {
        return context.openFileInput(FAVORITES_FILE);
    }

    public static
    FileOutputStream getOutputStream(final Context context)
    {
        try {
            return context.openFileOutput(FAVORITES_FILE, 0);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static
    File getFileDescriptor(final Context context)
    {
        return context.getFileStreamPath(FAVORITES_FILE);
    }

    private static
    Gson getGson()
    {
        if (sGson == null) {
            sGson = GsonUtils.createGsonBuilder()
                    .setFieldNamingPolicy(sFieldNamingPolicy)
                    .create();
        }
        return sGson;
    }

    public static
    boolean isFavorited(final Context context, final Date pubDate, final String link)
    {
        try {
            FavoritesMap map = getGson()
                    .fromJson(new InputStreamReader(getInputStream(context)), FavoritesMap.class);
            if (map == null) {
                return false;
            }

            if (map.articles() != null) {
                for (FeedItem i : map.articles()) {
                    if (i.pubDate().equals(pubDate) && i.link().equals(link)) {
                        return true;
                    }
                }
            }
            if (map.photos() != null) {
                for (FeedItem i : map.photos()) {
                    if (i.pubDate().equals(pubDate) && i.link().equals(link)) {
                        return true;
                    }
                }
            }
            if (map.videos() != null) {
                for (FeedItem i : map.videos()) {
                    if (i.pubDate().equals(pubDate) && i.link().equals(link)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        }

        return false;
    }

    public static
    boolean isFavorited(final Context context, final FeedItem item)
    {
        return isFavorited(context, item.pubDate(), item.link());
    }

    public static
    void addToFavorites(final Context context, final String type, final FeedItem item)
    {
        Timber.d(">> addToFavorites");
        FavoritesMap map;
        try {
            map = getGson()
                    .fromJson(new InputStreamReader(getInputStream(context)), FavoritesMap.class);
        } catch (FileNotFoundException e) {
            map = null;
        }

        if (map == null) {
            map = FavoritesMap.create(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        switch (type) {
            case FAVORITE_ARTICLES:
                map.articles().add(item);
                break;
            case FAVORITE_PHOTOS:
                map.photos().add(item);
                break;
            case FAVORITE_VIDEOS:
                map.videos().add(item);
                break;
        }

        OutputStreamWriter out = new OutputStreamWriter(getOutputStream(context));
        try {
            String json = getGson().toJson(map);
            Timber.d("Converted the following Map:");
            Timber.d(map.toString());
            Timber.d("to the following Json:");
            Timber.d(json);
            out.write(json);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timber.d("<< addToFavorites");
    }

    public static
    void removeFromFavorites(final Context context, final String type, final FeedItem item)
    {
        FavoritesMap map;
        try {
            map = getGson()
                    .fromJson(new InputStreamReader(getInputStream(context)), FavoritesMap.class);
        } catch (FileNotFoundException e) {
            map = null;
        }

        if (map == null) {
            return;
        }

        if (FAVORITE_ARTICLES.equals(type) &&
                (map.articles() == null || map.articles().size() < 1)) {
            return;
        } else if (FAVORITE_PHOTOS.equals(type) &&
                (map.photos() == null || map.photos().size() < 1)) {
            return;
        } else if (FAVORITE_VIDEOS.equals(type) &&
                (map.videos() == null || map.videos().size() < 1)) {
            return;
        }

        final LinkedList<FeedItem> llist;
        switch (type) {
            case FAVORITE_ARTICLES:
                llist = new LinkedList<>(map.articles());
                break;
            case FAVORITE_PHOTOS:
                llist = new LinkedList<>(map.photos());
                break;
            case FAVORITE_VIDEOS:
                llist = new LinkedList<>(map.videos());
                break;
            default:
                llist = new LinkedList<>();
                break;
        }

        for (Iterator itr = llist.iterator(); itr.hasNext(); ) {
            FeedItem i = (FeedItem) itr.next();
            if (i.pubDate().equals(item.pubDate()) && i.link().equals(item.link())) {
                itr.remove();
            }
        }

        switch (type) {
            case FAVORITE_ARTICLES:
                map.articles().clear();
                map.articles().addAll(llist);
                break;
            case FAVORITE_PHOTOS:
                map.photos().clear();
                map.photos().addAll(llist);
                break;
            case FAVORITE_VIDEOS:
                map.videos().clear();
                map.videos().addAll(llist);
                break;
        }

        OutputStreamWriter out = new OutputStreamWriter(getOutputStream(context));
        try {
            out.write(getGson().toJson(map));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
