package gov.whitehouse.core;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gov.whitehouse.data.model.FeedCategoryItem;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.data.model.FeedType;
import gov.whitehouse.util.DateUtils;
import timber.log.Timber;

/**
 * This class is responsible for parsing RSS feeds and extracting important parts into our app's
 * data model.
 */
public class FeedHandler extends DefaultHandler {

    // not *that* NS...
    public static String NS_MEDIA = "http://search.yahoo.com/mrss/";

    public static String NS_DC = "http://purl.org/dc/elements/1.1/";

    public static String NS_FEEDBURNER = "http://rssnamespace.org/feedburner/ext/1.0";

    private FeedItem.Builder currentItemBuilder;

    private Map<String, String> thumbnails;

    private List<FeedItem> items;

    private StringBuilder text;

    private String feedTitle;

    private FeedType feedType;

    public FeedHandler(String feedTitle, String viewType) {
        this.feedTitle = feedTitle;
        switch (viewType) {
            default:
            case FeedCategoryItem.VIEW_TYPE_ARTICLE_LIST:
                feedType = FeedType.TYPE_ARTICLE;
                break;
            case FeedCategoryItem.VIEW_TYPE_LIVE:
                feedType = FeedType.TYPE_LIVE;
                break;
            case FeedCategoryItem.VIEW_TYPE_PHOTO_GALLERY:
                feedType = FeedType.TYPE_PHOTO;
                break;
            case FeedCategoryItem.VIEW_TYPE_VIDEO_GALLERY:
                feedType = FeedType.TYPE_VIDEO;
                break;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        items = new ArrayList<>();
    }

    @Override
    public void startElement(String ns, String localName, String qName,
                             Attributes attributes) throws SAXException {
        /*Timber.v("start tag: %s; qName: %s; ns: %s", localName, qName, ns);*/

        text = new StringBuilder();

        if (localName.equals("item")) {
            currentItemBuilder = new FeedItem.Builder();
            currentItemBuilder.setFeedTitle(feedTitle);
            currentItemBuilder.setType(feedType);
            thumbnails = new TreeMap<>();
            currentItemBuilder.setThumbnails(thumbnails);
        } else if (localName.equals("enclosure")) {
            final String videoUrlString = attributes.getValue("url");
            currentItemBuilder.setVideoLink(parseURL(videoUrlString));
        } else if (ns.equals(NS_MEDIA) && (localName.equals("content") || localName
                .equals("thumbnail"))) {
            String urlString = attributes.getValue("url");
            String widthString = attributes.getValue("width");
            /* Only add the thumbnail if it's URL and width are in-tact */
            if (!(urlString == null || widthString == null)) {
                thumbnails.put(parseInt(widthString), parseURL(urlString));
            }
        }
    }

    public String parseInt(String s) {
        try {
            return Integer.toString(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return "0";
        }
    }

    public String parseURL(String s) {
        try {
            return new URL(s).toString();
        } catch (MalformedURLException e) {
            Timber.d("malformed URL: " + s);
            return null;
        }
    }

    @Override
    public void endElement(String ns, String localName, String qName) throws SAXException {
        if (currentItemBuilder == null) {
            return;
        }

        if (localName.equals("title")) {
            currentItemBuilder.setTitle(text.toString());
        } else if (localName.equals("description")) {
            currentItemBuilder.setDescription(text.toString());
        } else if (localName.equals("pubDate")) {
            // our RSS date format looks like "Tue, 10 Jul 2012 22:07:58 +0000"
            currentItemBuilder.setPubDate(DateUtils.parseDate(text.toString()));
        } else if (localName.equals("link")) {
            currentItemBuilder.setLink(parseURL(text.toString()));
        } else if (ns.equals(NS_DC) && localName.equals("creator")) {
            currentItemBuilder.setCreator(text.toString());
        } else if (localName.equals("guid")) {
            currentItemBuilder.setGuid(text.toString());
        } else if (localName.equals("item")) {
            items.add(currentItemBuilder.build());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        text.append(ch, start, length);
    }

    public List<FeedItem> getFeedItems() {
        return items;
    }
}

