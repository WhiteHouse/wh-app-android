/*
 * This project constitutes a work of the United States Government and is
 * not subject to domestic copyright protection under 17 USC § 105.
 * 
 * However, because the project utilizes code licensed from contributors
 * and other third parties, it therefore is licensed under the MIT
 * License.  http://opensource.org/licenses/mit-license.php.  Under that
 * license, permission is granted free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the conditions that any appropriate copyright notices and this
 * permission notice are included in all copies or substantial portions
 * of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package gov.whitehouse.services;

import com.google.gson.reflect.TypeToken;

import org.xml.sax.SAXException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import gov.whitehouse.R;
import gov.whitehouse.core.FeedHandler;
import gov.whitehouse.core.FeedItem;
import gov.whitehouse.ui.activities.app.LiveFeedActivity;
import gov.whitehouse.utils.GsonUtils;

public class LiveService extends IntentService {

    public static final String GET_LIVE_DATA_INTENT = "whitehouse:get_live_data_intent";

    public static final String REFRESH_LIVE_UI_INTENT = "whitehouse:refresh_live_ui_intent";

    public static final String EXTRA_JSON = "whitehouse:live_data_json";

    public static final String PREF_PREVIOUS_NOTIFICATIONS =
            "previous_notifications_json_collection_key";

    private static final String TAG = "WH::LiveService";

    private ArrayList<FeedItem> mLiveEvents;

    private String mFeedUrl;

    public LiveService() {
        super("LiveService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void postLiveItem(FeedItem item) {
        final Intent liveIntent = new Intent(this, LiveFeedActivity.class);
        liveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final SimpleDateFormat time = new SimpleDateFormat("h:mm a");

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this);
        notifyBuilder.setContentTitle("Live @ " + time.format(item.getPubDate()));
        notifyBuilder.setContentText(item.getTitle());
        notifyBuilder.setContentIntent(
                PendingIntent.getActivity(this, 0, liveIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        notifyBuilder.setSmallIcon(R.drawable.ic_launcher);
        notifyBuilder.setAutoCancel(true);

        final Notification notification = notifyBuilder.getNotification();
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, notification);
    }

    private Set<String> getStoredNotificationKeys() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // grab previously stored JSON, or an empty JSON collection
        String oldJsonString = prefs.getString(PREF_PREVIOUS_NOTIFICATIONS, "[]");

        Type collectionType = new TypeToken<Collection<String>>() {
        }.getType();
        Collection<String> oldNotificationsCollection = GsonUtils
                .fromJson(oldJsonString, collectionType);

        return new HashSet<String>(oldNotificationsCollection);
    }

    private void saveNotifications(Set<String> notifications) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        String newNotificationsJson = GsonUtils.toJson(notifications);
        editor.putString(PREF_PREVIOUS_NOTIFICATIONS, newNotificationsJson);
        editor.commit();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("whitehouse", ">> LiveService >> onHandleIntent()");

        /*
         * If the device is rocking an Android version earlier than Honeycomb,
         * don't do anything at all. Live streaming is only supported on
         * Android 3.0 and higher.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final Intent refreshIntent = new Intent(REFRESH_LIVE_UI_INTENT);

        if (intent.getAction() == GET_LIVE_DATA_INTENT && mLiveEvents != null) {
            refreshIntent.putExtra(EXTRA_JSON, GsonUtils.toJson(mLiveEvents));
            sendBroadcast(refreshIntent);
        }

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean notificationsDefault = getResources()
                .getBoolean(R.bool.pref_default_general_notifications);
        String prefKey = getString(R.string.pref_key_general_notifications);
        boolean notificationsEnabled = prefs.getBoolean(prefKey, notificationsDefault);

        mFeedUrl = getString(R.string.live_feed_url);
        updateEvents();

        if (notificationsEnabled && mLiveEvents != null) {
            // these aren't new per se, but just the new value that we'll store at the end
            Set<String> newNotifications = new HashSet<String>();
            Set<String> oldNotifications = getStoredNotificationKeys();

            // the user's current time is the reference point for all live events
            Date now = new Date();

            for (FeedItem item : mLiveEvents) {
                String guid = item.getGuid();
                Date pubDate = item.getPubDate();

                // create a key from the guid + time, so that we can tell if a notification
                // for this item, with this particular pubDate, was ever posted before
                String key = String.format("%s+%tQ", guid, pubDate);

                // So, if the item *should* have any notification at all ...
                if (pubDate != null && pubDate.before(now)) {
                    // ... then only post it if we haven't already, or if the date has changed
                    if (!oldNotifications.contains(key)) {
                        postLiveItem(item);
                    }

                    // and always keep the key around until it disappears from the live feed
                    newNotifications.add(key);
                }
            }

            // feed items that are not in the live feed anymore will "fall off" the set of items
            // we are
            // concerned with here, so that we don't accumulate a huge set of string keys

            saveNotifications(newNotifications);
        }

        /*
         * Send off a refresh broadcast to anything listening. Also
         * includes the actual refreshed list of live events so
         * BroadcastReceivers can handle the data straight away.
         */
        refreshIntent.putExtra(EXTRA_JSON, GsonUtils.toJson(mLiveEvents));
        sendBroadcast(refreshIntent);

        scheduleNextUpdate();
    }

    private boolean updateEvents() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(mFeedUrl).openConnection();
            InputStream in;
            int status;

            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", getString(R.string.user_agent_string));

            in = conn.getInputStream();
            status = conn.getResponseCode();

            if (status < 400 && status != 304) {
                /* We should be good to go */
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                FeedHandler handler = new FeedHandler();
                parser.parse(in, handler);

                /*
                 * Cycle through the received events and make sure they're all valid.
                 */
                ArrayList<FeedItem> validLiveEvents = new ArrayList<FeedItem>();
                for (FeedItem item: handler.getFeedItems()) {
                    if (item != null && item.getPubDate() != null) {
                        validLiveEvents.add(item);
                    }
                }

                mLiveEvents = validLiveEvents;
            }

            conn.disconnect();

            return status < 400;
        } catch (SAXException e) {
            Log.d(TAG, "failed to parse XML");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (IOException e) {
            Log.d(TAG, "error reading feed");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (IllegalStateException e) {
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (ParserConfigurationException e) {
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return false;
    }

    private void scheduleNextUpdate() {
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent
                .getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long now = System.currentTimeMillis();
        int intervalSeconds = getResources()
                .getInteger(R.integer.live_feed_update_interval_seconds);
        long intervalMillis = intervalSeconds * DateUtils.SECOND_IN_MILLIS;
        long nextUpdateTimeMillis = now + intervalMillis;

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
    }
}
