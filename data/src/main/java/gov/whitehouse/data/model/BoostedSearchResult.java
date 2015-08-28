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
class BoostedSearchResult implements Parcelable
{

    public abstract String
    description();

    public abstract String
    title();

    public abstract String
    url();

    public static BoostedSearchResult
    create(String description, String title, String url)
    {
        return new AutoParcel_BoostedSearchResult(description, title, url);
    }

    public static
    class BoostedSearchResultGsonAdapter implements JsonDeserializer<BoostedSearchResult>,
                                                    JsonSerializer<BoostedSearchResult>
    {

        @Override
        public
        BoostedSearchResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return context.deserialize(json, AutoParcel_BoostedSearchResult.class);
        }

        @Override
        public
        JsonElement serialize(BoostedSearchResult src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src, AutoParcel_BoostedSearchResult.class);
        }
    }
}
