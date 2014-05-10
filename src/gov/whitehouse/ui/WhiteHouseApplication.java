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

package gov.whitehouse.ui;

import com.bugsense.trace.BugSenseHandler;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import gov.whitehouse.R;
import gov.whitehouse.receivers.PushReceiver;
import gov.whitehouse.services.LiveService;

public class WhiteHouseApplication extends Application {

    // this field is necessary to prevent the listener from being GC'ed
    // http://stackoverflow.com/a/3104265/56243
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    @Override
    public void onCreate() {
        super.onCreate();

        BugSenseHandler.setup(this, getString(R.string.bugsense_id));

        /*
         * Make sure LiveService is running
         */
        Intent serviceIntent = new Intent(this, LiveService.class);
        startService(serviceIntent);
        
        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);
        options.inProduction = false;
        options.pushServiceEnabled = true;

        UAirship.takeOff(this, options);

        PushManager.shared().setIntentReceiver(PushReceiver.class);

        Set<String> tags = new HashSet<String>();
        tags.add("app_v2");
        tags.add("android_" + Build.VERSION.SDK_INT);
        tags.add("language_" + Locale.getDefault().getLanguage());
        tags.add("country_" + Locale.getDefault().getCountry());

        TimeZone timeZone = TimeZone.getDefault();
        String timeZoneCode = timeZone.getDisplayName(timeZone.inDaylightTime(new Date()), TimeZone.SHORT, Locale.US);
        tags.add(timeZoneCode);

        PushManager.shared().setTags(tags);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String pushPrefKey = getString(R.string.pref_key_general_notifications);

        // keep a reference to this listener... see notes on the field def
        mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (!s.equals(pushPrefKey)) {
                    return;
                }

                Boolean pushEnabledDefault = getResources()
                        .getBoolean(R.bool.pref_default_general_notifications);
                Boolean pushEnabled = sharedPreferences.getBoolean(pushPrefKey, pushEnabledDefault);
                if (pushEnabled) {
                    PushManager.enablePush();
                    PushPreferences prefs = PushManager.shared().getPreferences();
                    prefs.setSoundEnabled(true);
                    prefs.setVibrateEnabled(true);
                    Log.d("whitehouse", "App APID: " + prefs.getPushId());
                } else {
                    PushManager.disablePush();
                }
            }
        };

        // manually trigger the change handler, to set up the initial Urban Airship state
        mPrefListener.onSharedPreferenceChanged(prefs, pushPrefKey);

        prefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }
}
