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
class SearchResult implements Parcelable
{

    public abstract String
    content();

    public abstract String
    title();

    public abstract String
    unescapedUrl();

    public static SearchResult
    create(String content, String title, String unescapedUrl)
    {
        return new AutoParcel_SearchResult(content, title, unescapedUrl);
    }

    public static
    class SearchResultGsonAdapter implements JsonDeserializer<SearchResult>,
                                             JsonSerializer<SearchResult>
    {

        @Override
        public
        SearchResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return context.deserialize(json, AutoParcel_SearchResult.class);
        }

        @Override
        public
        JsonElement serialize(SearchResult src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src, AutoParcel_SearchResult.class);
        }
    }
}
