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

package gov.whitehouse.receivers;


import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.core.DashboardItem;
import gov.whitehouse.ui.activities.app.HomeActivity;
import gov.whitehouse.ui.loaders.DashboardLoader;
import gov.whitehouse.utils.DashboardItemUtils;

public class PushReceiver extends BroadcastReceiver {

    private static final String logTag = "PushReceiver";

    private ArrayList<DashboardItem> mDashboardItems;

    private Intent getSectionIntent(Context context, String sectionTitle) {
        final String destSection = sectionTitle.toLowerCase();

        DashboardLoader loader = new DashboardLoader(context);
        List<DashboardItem> items = loader.loadInBackground();
        for (DashboardItem item: items) {
            final String lowerTitle = item.getTitle().toLowerCase();
            if (lowerTitle.equals(destSection)) {
                return DashboardItemUtils.createIntent(context, item);
            }
        }

        return null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
            // we will be launching *something* here
            Intent launch = null;

            // all extra info from the push notification is in the notification intent's extras
            Bundle extras = intent.getExtras();
            final String sectionToOpen = extras.getString("open-section");
            if (sectionToOpen != null) {
                launch = getSectionIntent(context, sectionToOpen);
            }

            // otherwise, just launch the app
            if (launch == null) {
                launch = new Intent(Intent.ACTION_MAIN);
                launch.setClass(UAirship.shared().getApplicationContext(), HomeActivity.class);
                launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            // finally kick off the intent
            UAirship.shared().getApplicationContext().startActivity(launch);
        } else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
            Log.i(logTag, "Registration complete. New APID = " + intent
                    .getStringExtra(PushManager.EXTRA_APID)
                    + "; valid = " + intent
                    .getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));
        }
    }
}
