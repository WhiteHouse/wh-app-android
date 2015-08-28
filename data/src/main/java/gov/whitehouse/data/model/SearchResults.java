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
class SearchResults implements Parcelable
{

    public abstract Integer
    total();

    public abstract Integer
    startrecord();

    public abstract Integer
    endrecord();

    public abstract List<String>
    related();

    public abstract String
    spelling_suggestion();

    public abstract List<BoostedSearchResult>
    boosted_results();

    public abstract List<SearchResult>
    results();

    public static SearchResults
    create(Integer total, Integer startrecord, Integer endrecord, List<String> related,
           String spelling_suggestion, List<BoostedSearchResult> boosted_results,
           List<SearchResult> results)
    {
        return new AutoParcel_SearchResults(total,
                                            startrecord,
                                            endrecord,
                                            related,
                                            spelling_suggestion,
                                            boosted_results,
                                            results);
    }

    public static
    class SearchResultsGsonAdapter implements JsonDeserializer<SearchResults>,
                                              JsonSerializer<SearchResults>
    {

        @Override
        public
        SearchResults deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return context.deserialize(json, AutoParcel_SearchResults.class);
        }

        @Override
        public
        JsonElement serialize(SearchResults src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src, AutoParcel_SearchResults.class);
        }
    }
}
