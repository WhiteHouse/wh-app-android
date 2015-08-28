package gov.whitehouse.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Date;

import gov.whitehouse.data.model.BoostedSearchResult;
import gov.whitehouse.data.model.FavoritesMap;
import gov.whitehouse.data.model.FeedCategoryConfig;
import gov.whitehouse.data.model.FeedCategoryItem;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.data.model.SearchResult;
import gov.whitehouse.data.model.SearchResults;

/**
 * Gson utilities.
 */
public abstract class GsonUtils {

    private static final Gson GSON = createGson(true);

    private static final Gson GSON_NO_NULLS = createGson(false);

    public static final GsonBuilder createGsonBuilder(final boolean serializeNulls) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(BoostedSearchResult.class,
                                    new BoostedSearchResult.BoostedSearchResultGsonAdapter());
        builder.registerTypeAdapter(Date.class, new DateFormatter());
        builder.registerTypeAdapter(FavoritesMap.class,
                                    new FavoritesMap.FavoritesMapGsonDeserializer());
        builder.registerTypeAdapter(FeedCategoryConfig.class,
                                    new FeedCategoryConfig.FeedCategoryConfigGsonDeserializer());
        builder.registerTypeAdapter(FeedCategoryItem.class,
                                    new FeedCategoryItem.FeedCategoryItemGsonDeserializer());
        builder.registerTypeAdapter(FeedItem.class,
                                    new FeedItem.FeedItemGsonDeserializer());
        builder.registerTypeAdapter(SearchResult.class,
                                    new SearchResult.SearchResultGsonAdapter());
        builder.registerTypeAdapter(SearchResults.class,
                                    new SearchResults.SearchResultsGsonAdapter());
        if (serializeNulls) {
            builder.serializeNulls();
        }
        return builder;
    }

    public static final GsonBuilder createGsonBuilder() {
        return createGsonBuilder(true);
    }

    /**
     * Create the standard {@link Gson} configuration
     *
     * @return created gson, never null
     */
    public static final Gson createGson() {
        return createGson(true);
    }

    /**
     * Create the standard {@link Gson} configuration
     *
     * @param serializeNulls whether nulls should be serialized
     * @return created gson, never null
     */
    public static final Gson createGson(final boolean serializeNulls) {
        final GsonBuilder builder = createGsonBuilder(serializeNulls);
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return builder.create();
    }

    /**
     * Get reusable pre-configured {@link Gson} instance
     *
     * @return Gson instance
     */
    public static final Gson getGson() {
        return GSON;
    }

    /**
     * Get reusable pre-configured {@link Gson} instance
     *
     * @return Gson instance
     */
    public static final Gson getGson(final boolean serializeNulls) {
        return serializeNulls ? GSON : GSON_NO_NULLS;
    }

    /**
     * Convert object to json
     *
     * @return json string
     */
    public static final String toJson(final Object object) {
        return toJson(object, true);
    }

    /**
     * Convert object to json
     *
     * @return json string
     */
    public static final String toJson(final Object object, final boolean includeNulls) {
        return includeNulls ? GSON.toJson(object) : GSON_NO_NULLS.toJson(object);
    }

    /**
     * Convert string to given type
     *
     * @return instance of type
     */
    public static final <V> V fromJson(String json, Class<V> type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Convert string to given type
     *
     * @return instance of type
     */
    public static final <V> V fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Convert content of reader to given type
     *
     * @return instance of type
     */
    public static final <V> V fromJson(Reader reader, Class<V> type) {
        return GSON.fromJson(reader, type);
    }

    /**
     * Convert content of reader to given type
     *
     * @return instance of type
     */
    public static final <V> V fromJson(Reader reader, Type type) {
        return GSON.fromJson(reader, type);
    }
}
