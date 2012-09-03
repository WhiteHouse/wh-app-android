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

package gov.whitehouse.utils;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * I manage the state of Google Analytics tracking, with the goal of only starting a new GA session
 * when there are no activities running (i.e. the user has recently been using the app). I also
 * provide a utility method {@link #track(CharSequence...)} to track "page" views.
 */
public class GATrackingManager {
    private static GATrackingManager sharedInstance;

    private Context context;
    private String account;
    private int dispatchInterval;
    private int activityCount;
    private GoogleAnalyticsTracker tracker;

    /**
     * Use this method to set up and get the tracking manager at app startup.
     * @param context the application context
     * @param account your Google Analytics account
     * @param dispatchInterval how many seconds to wait between sending GA data
     * @return the initially-configured shared tracking manager
     */
    public static GATrackingManager getInstance(Application context, String account, int dispatchInterval) {
        if (sharedInstance == null) {
            sharedInstance = new GATrackingManager(context, account, dispatchInterval);
        }

        return sharedInstance;
    }

    /**
     * Use this method to get the tracking manager outside of the app's home activity.
     * @return the shared tracking manager
     */
    public static GATrackingManager getInstance() {
        if (sharedInstance == null) {
            throw new IllegalStateException("GATrackingManager not initialized.");
        }

        return sharedInstance;
    }

    private GATrackingManager(Application context, String account, int dispatchInterval) {
        activityCount = 0;

        this.context = context;
        this.account = account;
        this.dispatchInterval = dispatchInterval;

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.setAnonymizeIp(true);
    }

    /**
     * Increments the activity counter. You must call this method when an activity becomes active.
     */
    public void push() {
        if (activityCount == 0) {
            tracker.startNewSession(account, dispatchInterval, context);
        }
        activityCount++;
    }

    /**
     * Decrements the activity counter. You must call this method when an activity is destroyed.
     */
    public void pop() {
        if (activityCount > 0) {
            activityCount--;

            if (activityCount == 0) {
                tracker.stopSession();
            }
        }
    }

    /**
     * Track a page view using the supplied pathComponents with a base "/" prepended.
     * @param pathComponents a list of path components, to be joined by "/"
     */
    public void track(CharSequence... pathComponents) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence component: pathComponents) {
            sb.append("/" + component);
        }

        String page = sb.toString();
        Log.d(this.getClass().getSimpleName(), "page view: " + page);
        GoogleAnalyticsTracker.getInstance().trackPageView(page);
    }
}
