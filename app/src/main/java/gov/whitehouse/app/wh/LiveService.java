package gov.whitehouse.app.wh;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.whitehouse.R;
import gov.whitehouse.content.NotificationPublisher;
import gov.whitehouse.core.manager.FeedCategoryManager;
import gov.whitehouse.core.manager.FeedManager;
import gov.whitehouse.data.model.FeedCategoryConfig;
import gov.whitehouse.data.model.FeedCategoryItem;
import gov.whitehouse.data.model.FeedItem;
import gov.whitehouse.util.DateUtils;
import gov.whitehouse.util.GsonUtils;
import gov.whitehouse.util.NetworkUtils;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public
class LiveService extends IntentService
{

    public static final String INTENT_GET_LIVE_DATA = "gov.whitehouse:get_live_data_intent";

    public static final String INTENT_REFRESH_LIVE_UI = "gov.whitehouse:refresh_live_ui_intent";

    public static final String EXTRA_JSON = "extra:json";

    public static final String PREF_PREV_NOTIFICATIONS = "previous_notifications_json_collection";

    private static final long MILLIS_IN_MINUTE = 60000;

    private static final
    BehaviorSubject<Observable<Integer>> sLiveItemCountObservable = BehaviorSubject.create();

    private
    List<FeedItem> mLiveEvents;

    public
    LiveService()
    {
        super("LiveService");
    }

    private
    Notification buildLiveNotification(final FeedItem item)
    {
        final Intent liveIntent = new Intent(this, MainActivity.class);
        final SimpleDateFormat time = new SimpleDateFormat("h:mm a");
        final NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this);
        final Uri notifSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        liveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notifBuilder.setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                    .setContentIntent(
                            PendingIntent.getActivity(this, 0, liveIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle("Live @ " + time.format(item.pubDate()))
                    .setContentText(item.title())
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .setExtras(new Bundle())
                    .setSmallIcon(R.drawable.ic_stat_wh)
                    .setSound(notifSound)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setWhen(item.pubDate().getTime());

        return notifBuilder.build();
    }

    private
    void postLiveItem(final FeedItem item)
    {
        final Notification notification = buildLiveNotification(item);
        final NotificationManagerCompat nm = NotificationManagerCompat.from(this);

        nm.notify(0, notification);
    }

    private
    Set<String> getStoredNotificationKeys()
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String oldJson = prefs.getString(PREF_PREV_NOTIFICATIONS, "[]");
        final Type collectionType = new TypeToken<Collection<String>>(){}.getType();
        final Collection<String> oldNotifications = GsonUtils.fromJson(oldJson, collectionType);

        return new HashSet<>(oldNotifications);
    }

    private
    void saveNotifications(final Set<String> notifications)
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();
        final String newNotifications = GsonUtils.toJson(notifications);

        editor.putString(PREF_PREV_NOTIFICATIONS, newNotifications).apply();
    }

    private
    void refreshLiveUi()
    {
        final Intent refreshIntent = new Intent(INTENT_REFRESH_LIVE_UI);

        refreshIntent.putExtra(EXTRA_JSON, GsonUtils.toJson(mLiveEvents));
        sendBroadcast(refreshIntent);
    }

    private
    void refreshLiveUiIfNeeded(final Intent launchingIntent)
    {
        if (INTENT_GET_LIVE_DATA.equals(launchingIntent.getAction()) && mLiveEvents != null) {
            refreshLiveUi();
        }
    }

    private
    void scheduleNextUpdate()
    {
        final Intent intent = new Intent(this, this.getClass());
        final PendingIntent pIntent =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final long now = System.currentTimeMillis();
        final int intervalSec = getResources().getInteger(R.integer.live_feed_update_interval_sec);
        final long intervalMs = intervalSec * DateUtils.SECOND_IN_MILLIS;
        final long nextUpdateTime = now + intervalMs;
        final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC, nextUpdateTime, pIntent);
    }

    private
    void scheduleNotification(String key, Notification n, long time)
    {
        final Intent nIntent = new Intent(this, NotificationPublisher.class);
        final PendingIntent pIntent;
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        nIntent.putExtra(NotificationPublisher.EXTRA_NOTIFICATION_ID, key.hashCode());
        nIntent.putExtra(NotificationPublisher.EXTRA_NOTIFICATION, n);
        pIntent = PendingIntent.getBroadcast(this, 0, nIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pIntent);
        }
    }

    private
    void updateEvents()
    {
        final FeedCategoryManager fcm = FeedCategoryManager.get();
        final FeedCategoryConfig config = fcm.getFeedCategoryConfig()
                                             .toBlocking()
                                             .first();
        final List<FeedItem> feedItems;
        final List<FeedItem> validItems = new ArrayList<>();
        FeedCategoryItem liveItem = null;

        for(FeedCategoryItem c : config.feeds()) {
            if (FeedCategoryItem.VIEW_TYPE_LIVE.equals(c.viewType())) {
                liveItem = c;
                break;
            }
        }
        if (liveItem == null) {
            return;
        }
        FeedManager.updateFeedFromServer(liveItem.feedUrl(), liveItem.title(), liveItem.viewType());
        feedItems = FeedManager.observeFeedItems(liveItem.feedUrl())
                .toBlocking()
                .first();
        for (FeedItem item : feedItems) {
            if (item != null && item.pubDate() != null) {
                validItems.add(item);
            }
        }
        sLiveItemCountObservable.onNext(Observable.just(validItems.size()));
        mLiveEvents = validItems;
    }

    private
    void processEvents()
    {
        final Set<String> newNotifs = new HashSet<>();
        final Set<String> oldNotifs = getStoredNotificationKeys();
        final Date now = new Date();
        Date pubDate;
        Date notifDate;
        String key;

        for (FeedItem item : mLiveEvents) {
            pubDate = item.pubDate();
            notifDate = new Date();
            notifDate.setTime(pubDate.getTime() - (30 * MILLIS_IN_MINUTE));
            key = String.format("%s+%tQ", item.guid(), pubDate);
            if (pubDate.after(now)) {
                if (!oldNotifs.contains(key)) {
                    scheduleNotification(key, buildLiveNotification(item), notifDate.getTime());
                }
                newNotifs.add(key);
            }
        }
        saveNotifications(newNotifs);
    }

    @Override
    protected
    void onHandleIntent(Intent intent)
    {
        if (!NetworkUtils.checkNetworkAvailable(this)) {
            Timber.i("No network, cannot update live events");
            return;
        }
        refreshLiveUiIfNeeded(intent);
        updateEvents();
        if (mLiveEvents != null) {
            processEvents();
        }
        refreshLiveUi();
        scheduleNextUpdate();
    }

    public static
    Observable<Integer> observeLiveItemCount()
    {
        return Observable.switchOnNext(sLiveItemCountObservable);
    }
}
