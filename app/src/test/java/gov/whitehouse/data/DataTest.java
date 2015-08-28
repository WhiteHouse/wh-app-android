package gov.whitehouse.data;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import gov.whitehouse.data.model.FavoritesMap;
import gov.whitehouse.data.model.FeedCategoryConfig;
import gov.whitehouse.data.model.FeedCategoryItem;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.data.model.FeedType;
import gov.whitehouse.util.DateUtils;

import static org.assertj.core.api.Assertions.assertThat;

@Config(emulateSdk = 16)
@RunWith(RobolectricTestRunner.class)
public
class DataTest
{

    private static final String TEST_DATE = "Thu, 11 Dec 2014 00:02:14 +0000";

    static Gson sGson = null;

    public static GsonBuilder
    createGsonBuilder()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(FavoritesMap.class,
                                    new FavoritesMap.FavoritesMapGsonDeserializer());
        builder.registerTypeAdapter(FeedCategoryConfig.class,
                                    new FeedCategoryConfig.FeedCategoryConfigGsonDeserializer());
        builder.registerTypeAdapter(FeedCategoryItem.class,
                                    new FeedCategoryItem.FeedCategoryItemGsonDeserializer());
        builder.registerTypeAdapter(FeedItem.class,
                                    new FeedItem.FeedItemGsonDeserializer());
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
        builder.serializeNulls();

        return builder;
    }

    public static Gson
    createGson()
    {
        if (sGson == null) {
            sGson = createGsonBuilder().create();
        }
        return sGson;
    }

    public static FeedItem
    generateFeedItem()
    {
        return new FeedItem.Builder()
                .setCreator("John Doe")
                .setDescription("A feed item by John Doe")
                .setFeedTitle("John Doe's Feed")
                .setGuid("johndoeguid-1234")
                .setLink("http://www.example.com/link/john-doe/item")
                .setPubDate(DateUtils.parseDate(TEST_DATE))
                .setThumbnails(Collections.emptyMap())
                .setTitle("An Item")
                .setType(FeedType.TYPE_ARTICLE)
                .build();
    }

    @Test
    public void
    createFavoritesMap()
    {
        FeedItem item = generateFeedItem();
        FavoritesMap map =
                FavoritesMap.create(new ArrayList<>(1), new ArrayList<>(), new ArrayList<>());
        map.articles().add(item);
        assertThat(map.articles()).hasSize(1);
        assertThat(map.photos()).hasSize(0);
        assertThat(map.videos()).hasSize(0);
    }

    public static void
    printFeedItemFieldHashes(FeedItem item)
    {
        String s =
                   "Item hashCode: " + item.hashCode() + "\n"
                 + "      creator: " + item.creator().hashCode() + "\n"
                 + "  description: " + item.description().hashCode() + "\n"
                 + "    feedTitle: " + item.feedTitle().hashCode() + "\n"
                 + "         guid: " + item.guid().hashCode() + "\n"
                 + "         link: " + item.link().hashCode() + "\n"
                 + "      pubDate: " + item.pubDate().hashCode() + "\n"
                 + "   thumbnails: " + item.thumbnails().hashCode() + "\n"
                 + "        title: " + item.title().hashCode() + "\n"
                 + "         type: " + item.type().hashCode() + "\n";
        System.out.println(s);
    }

    @Test
    public void
    mapFavoritesToJson()
    {
        final Gson gson = createGson();
        String json;
        FeedItem item = generateFeedItem();
        FavoritesMap convertedMap;
        FavoritesMap map =
                FavoritesMap.create(new ArrayList<>(1), new ArrayList<>(), new ArrayList<>());
        map.articles().add(item);
        System.out.println("Map toString():\n" + map.toString() + "\n");
        json = gson.toJson(map);
        System.out.println("Map JSON:\n" + json + "\n");
        convertedMap = gson.fromJson(json, FavoritesMap.class);
        System.out.println("Converted Map toString():\n" + convertedMap.toString() + "\n");
        System.out.println("Item hashCode(): " + item.hashCode());
        printFeedItemFieldHashes(item);
        System.out.println("Converted Item hashCode(): " + convertedMap.articles().get(0).hashCode());
        printFeedItemFieldHashes(convertedMap.articles().get(0));
        assertThat(convertedMap.articles()).hasSameSizeAs(map.articles());
        assertThat(convertedMap.photos()).hasSameSizeAs(map.photos());
        assertThat(convertedMap.videos()).hasSameSizeAs(map.videos());
        assertThat(convertedMap.articles()).contains(item);
    }

    @Test
    public void
    createFeedItem()
    {
        FeedItem item = generateFeedItem();
        FeedItem dupe = generateFeedItem();

        assertThat(item).isEqualTo(item);
        assertThat(item).isEqualTo(dupe);
        assertThat(item.hashCode()).isEqualTo(dupe.hashCode());

        assertThat(item.creator()).isEqualTo("John Doe");
        assertThat(item.description()).isEqualTo("A feed item by John Doe");
        assertThat(item.feedTitle()).isEqualTo("John Doe's Feed");
        assertThat(item.guid()).isEqualTo("johndoeguid-1234");
        assertThat(item.link()).isEqualTo("http://www.example.com/link/john-doe/item");
        assertThat(item.pubDate()).isEqualTo(DateUtils.parseDate(TEST_DATE));
        assertThat(item.thumbnails()).isEqualTo(Collections.emptyMap());
        assertThat(item.title()).isEqualTo("An Item");
        assertThat(item.type()).isEqualTo(FeedType.TYPE_ARTICLE);
    }

    @Test
    public void
    listOfFeedItemEqualsFeedItem()
    {
        SoftAssertions softly = new SoftAssertions();
        FeedItem item = generateFeedItem();
        FeedItem dupe = generateFeedItem();
        List<FeedItem> list1 = new ArrayList<>(1);
        List<FeedItem> list2 = new ArrayList<>(1);

        list1.add(item);
        list2.add(item);
        softly.assertThat(list1).hasSameElementsAs(list2);
        softly.assertThat(list1).isEqualTo(list2);
        list2.clear();
        list2.add(dupe);
        softly.assertThat(list1).hasSameElementsAs(list2);
        softly.assertThat(list1).isEqualTo(list2);
        softly.assertAll();
    }
}
