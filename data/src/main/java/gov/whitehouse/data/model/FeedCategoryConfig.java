package gov.whitehouse.data.model;

import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract
class FeedCategoryConfig implements Parcelable
{

    public abstract List<FeedCategoryItem>
    feeds();

    public static FeedCategoryConfig
    create(List<FeedCategoryItem> feeds)
    {
        return new AutoParcel_FeedCategoryConfig(feeds);
    }

    public static
    class FeedCategoryConfigGsonDeserializer implements JsonDeserializer<FeedCategoryConfig>
    {
        @Override
        public
        FeedCategoryConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return context.deserialize(json, AutoParcel_FeedCategoryConfig.class);
        }
    }
}
