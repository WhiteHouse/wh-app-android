package gov.whitehouse.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public
class NetworkUtils
{

    public static
    boolean checkNetworkAvailable(Context ctx)
    {
        ConnectivityManager cm =
                ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
