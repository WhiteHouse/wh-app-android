package gov.whitehouse.data.model;

import android.os.Parcelable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract
class FeedCategoryItem implements Parcelable
{

    public static final String
    VIEW_TYPE_ARTICLE_LIST = "article-list";

    public static final String
    VIEW_TYPE_PHOTO_GALLERY = "photo-gallery";

    public static final String
    VIEW_TYPE_VIDEO_GALLERY = "video-gallery";

    public static final String
    VIEW_TYPE_LIVE = "live";

    public abstract String
    feedId();

    public abstract String
    feedUrl();

    public abstract String
    title();

    public abstract String
    viewType();

    public static FeedCategoryItem
    create(String feedId, String feedUrl, String title, String viewType)
    {
        return new AutoParcel_FeedCategoryItem(feedId, feedUrl, title, viewType);
    }

    public static
    class FeedCategoryItemGsonDeserializer implements JsonDeserializer<FeedCategoryItem>
    {
        @Override
        public
        FeedCategoryItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return context.deserialize(json, AutoParcel_FeedCategoryItem.class);
        }
    }
}
