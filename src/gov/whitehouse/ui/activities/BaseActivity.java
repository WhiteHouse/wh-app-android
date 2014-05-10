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

package gov.whitehouse.ui.activities;

import gov.whitehouse.ui.activities.app.WHPreferencesActivity;
import gov.whitehouse.utils.GATrackingManager;
import gov.whitehouse.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.androidquery.util.AQUtility;


public class BaseActivity extends ActionBarActivity {

    private boolean mIsMultipaned;

    private OnBackPressedHandler mOnBackPressedHandler;

    public interface OnBackPressedHandler {
        public boolean handleBackPress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String account = getString(R.string.google_analytics_account_id);
        int interval = getResources().getInteger(R.integer.google_analytics_dispatch_interval);
        GATrackingManager.getInstance(getApplication(), account, interval).push();

        setContentView(R.layout.main);

        mIsMultipaned = getResources().getBoolean(R.bool.multipaned);
    }

    public String getTrackingPathComponent() {
        return getSupportActionBar().getTitle().toString();
    }

    public void trackPageView() {
        GATrackingManager.getInstance().track(getTrackingPathComponent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackPageView();
    }

    public void setOnBackPressedHandler(final OnBackPressedHandler handler) {
        mOnBackPressedHandler = handler;
    }

    public OnBackPressedHandler getOnBackPressedHandler() {
        return mOnBackPressedHandler;
    }

    public boolean isMultipaned() {
        return mIsMultipaned;
    }

    @Override
    public void onBackPressed() {
        if (mOnBackPressedHandler != null) {
            if (!mOnBackPressedHandler.handleBackPress()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            final Intent settingsIntent = new Intent(this, WHPreferencesActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        GATrackingManager.getInstance().pop();

        if (isTaskRoot()) {
            final long triggerSize = 8000000;
            final long targetSize = 2000000;
            AQUtility.cleanCacheAsync(this, triggerSize, targetSize);
        }
    }
}
