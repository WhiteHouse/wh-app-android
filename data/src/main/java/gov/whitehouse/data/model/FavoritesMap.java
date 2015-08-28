package gov.whitehouse.data.model;

import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract
class FavoritesMap implements Parcelable
{

    public abstract List<FeedItem>
    articles();

    public abstract List<FeedItem>
    photos();

    public abstract List<FeedItem>
    videos();

    public static FavoritesMap
    create(List<FeedItem> articles, List<FeedItem> photos, List<FeedItem> videos)
    {
        return new AutoParcel_FavoritesMap(articles, photos, videos);
    }

    public static
    class FavoritesMapGsonDeserializer implements JsonDeserializer<FavoritesMap>, JsonSerializer<FavoritesMap>
    {


        @Override
        public
        FavoritesMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return context.deserialize(json, AutoParcel_FavoritesMap.class);
        }

        @Override
        public
        JsonElement serialize(FavoritesMap src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src, AutoParcel_FavoritesMap.class);
        }
    }
}
