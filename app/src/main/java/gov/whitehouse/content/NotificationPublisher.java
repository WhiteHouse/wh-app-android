package gov.whitehouse.content;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

public
class NotificationPublisher extends BroadcastReceiver
{

    public static final String EXTRA_NOTIFICATION = "extra:notification";

    public static final String EXTRA_NOTIFICATION_ID = "extra:notification_id";

    @Override
    public
    void onReceive(Context context, Intent intent)
    {
        final NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        final Notification n = intent.getParcelableExtra(EXTRA_NOTIFICATION);
        final int id = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
        nm.notify(id, n);
    }
}
