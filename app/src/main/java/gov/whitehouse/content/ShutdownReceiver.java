package gov.whitehouse.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gov.whitehouse.app.wh.LiveService;

public
class ShutdownReceiver extends BroadcastReceiver
{

    @Override
    public
    void onReceive(Context context, Intent intent)
    {
        if (context != null) {
            context.stopService(new Intent(context, LiveService.class));
        }
    }
}
