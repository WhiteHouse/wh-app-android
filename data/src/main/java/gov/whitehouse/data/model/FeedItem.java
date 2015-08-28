package gov.whitehouse.data.model;

import android.net.Uri;
import android.os.Parcelable;
import android.os.SystemClock;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract
class FeedItem implements Parcelable
{

    public abstract String
    creator();

    public abstract String
    description();

    public abstract Boolean
    favorited();

    public abstract String
    feedTitle();

    public abstract String
    guid();

    public abstract String
    link();

    public abstract Date
    pubDate();

    public abstract Map<String, String>
    thumbnails();

    public abstract String
    title();

    public abstract FeedType
    type();

    public abstract String
    videoLink();

    public String
    getBestThumbnailUrl(int idealWidth)
    {
        int thisWidth, thisDiff, closestDiff;
        int closest = Integer.MAX_VALUE;

        for (Map.Entry<String, String> pair : thumbnails().entrySet()) {
            thisWidth = Integer.parseInt(pair.getKey());
            thisDiff = Math.abs(idealWidth - thisWidth);
            closestDiff = Math.abs(idealWidth - closest);
            if (thisDiff < closestDiff) {
                closest = thisWidth;
            }
        }

        return thumbnails().get(Integer.toString(closest));
    }

    public boolean
    isYouTubeVideo()
    {
        if (videoLink() != null) {
            return Pattern.compile("(www\\.)?((youtube(-nocookie)?)\\.com|youtu\\.be)")
                          .matcher(Uri.parse(videoLink()).getHost())
                          .matches();
        }

        return false;
    }

    public static FeedItem
    create(String creator, String description, Boolean favorited, String feedTitle, String guid,
           String link, Date pubDate, Map<String, String> thumbnails, String title, FeedType type,
           String videoLink)
    {
        return new AutoParcel_FeedItem(creator, description, favorited, feedTitle, guid,
                                       link, pubDate, thumbnails, title, type, videoLink);
    }

    public static
    class Builder
    {

        private String
        mCreator = "";

        private String
        mDescription = "";

        private Boolean
        mFavorited = false;

        private String
        mFeedTitle = "";

        private String
        mGuid = "";

        private String
        mLink = "";

        private Date
        mPubDate;

        private Map<String, String>
        mThumbnails;

        private String
        mTitle = "";

        private FeedType
        mType;

        private String
        mVideoLink = "";

        public
        Builder()
        {
        }

        public
        Builder(FeedItem item)
        {
            mCreator = item.creator();
            mDescription = item.description();
            mFavorited = item.favorited();
            mFeedTitle = item.feedTitle();
            mGuid = item.guid();
            mLink = item.link();
            mPubDate = item.pubDate();
            mThumbnails = item.thumbnails();
            mTitle = item.title();
            mType = item.type();
            mVideoLink = item.videoLink();
        }

        public FeedItem
        build()
        {
            return FeedItem.create(mCreator,
                                   mDescription,
                                   mFavorited,
                                   mFeedTitle,
                                   mGuid,
                                   mLink,
                                   mPubDate,
                                   mThumbnails,
                                   mTitle,
                                   mType,
                                   mVideoLink);
        }

        public Builder
        setCreator(String creator)
        {
            mCreator = creator;
            return this;
        }

        public Builder
        setDescription(String description)
        {
            mDescription = description;
            return this;
        }

        public Builder
        setFavorited(Boolean favorited)
        {
            mFavorited = favorited;
            return this;
        }

        public Builder
        setFeedTitle(String feedTitle)
        {
            mFeedTitle = feedTitle;
            return this;
        }

        public Builder
        setGuid(String guid)
        {
            mGuid = guid;
            return this;
        }

        public Builder
        setLink(String link)
        {
            mLink = link;
            return this;
        }

        public Builder
        setPubDate(Date pubDate)
        {
            mPubDate = pubDate;
            return this;
        }

        public Builder
        setThumbnails(Map<String, String> thumbnails)
        {
            mThumbnails = thumbnails;
            return this;
        }

        public Builder
        setTitle(String title)
        {
            mTitle = title;
            return this;
        }

        public Builder
        setType(FeedType type)
        {
            mType = type;
            return this;
        }

        public Builder
        setVideoLink(String videoLink)
        {
            mVideoLink = videoLink;
            return this;
        }
    }

    public static
    class FeedItemGsonDeserializer implements JsonDeserializer<FeedItem>, JsonSerializer<FeedItem>
    {
        @Override
        public
        FeedItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            return context.deserialize(json, AutoParcel_FeedItem.class);
        }

        @Override
        public
        JsonElement serialize(FeedItem src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src, AutoParcel_FeedItem.class);
        }
    }
}
