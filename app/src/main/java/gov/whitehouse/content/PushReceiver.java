package gov.whitehouse.content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.urbanairship.UAirship;
import com.urbanairship.push.BaseIntentReceiver;
import com.urbanairship.push.PushMessage;

import gov.whitehouse.app.wh.MainActivity;
import gov.whitehouse.core.manager.FeedCategoryManager;
import gov.whitehouse.data.model.FeedCategoryConfig;
import gov.whitehouse.data.model.FeedCategoryItem;

public
class PushReceiver extends BaseIntentReceiver
{

    @Override
    protected
    void onChannelRegistrationSucceeded(Context context, String s)
    {
    }

    @Override
    protected
    void onChannelRegistrationFailed(Context context)
    {
    }

    @Override
    protected
    void onPushReceived(Context context, PushMessage pushMessage, int i)
    {
    }

    @Override
    protected
    void onBackgroundPushReceived(Context context, PushMessage pushMessage)
    {
    }

    @Override
    protected
    boolean onNotificationOpened(Context context, PushMessage pushMessage, int i)
    {
        final Intent launch = new Intent(Intent.ACTION_MAIN);
        launch.setClass(context.getApplicationContext(), MainActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(launch);
        return true;
    }

    @Override
    protected
    boolean onNotificationActionOpened(Context context, PushMessage pushMessage, int i, String s, boolean b)
    {
        return false;
    }
}
