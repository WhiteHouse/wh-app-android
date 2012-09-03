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

package gov.whitehouse.ui.loaders;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.core.DashboardConfig;
import gov.whitehouse.core.DashboardItem;
import gov.whitehouse.utils.GsonUtils;

public class DashboardLoader extends AsyncLoader<List<DashboardItem>> {

    private static final String TAG = "DashboardLoader";

    private Activity mActivity;

    public DashboardLoader(Context context) {
        super(context);
        if (context instanceof Activity) {
            mActivity = (Activity)context;
        }
    }

    @Override
    public List<DashboardItem> loadInBackground() {
        DashboardConfig config = null;
        File configFile = new File(getContext().getCacheDir(), "dashboard.json");
        URL configURL = null;

        try {
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(configFile.lastModified());
            Calendar weekAgo = Calendar.getInstance();
            weekAgo.add(Calendar.DAY_OF_YEAR, -7);

            if (configFile == null || !configFile.exists() || !configFile.isFile() || lastModified
                    .compareTo(weekAgo) < 1) {
                configURL = new URL(getContext().getString(R.string.config_url));
                HttpURLConnection conn = (HttpURLConnection) configURL.openConnection();
                InputStream in;
                int status;

                conn.setDoInput(true);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("User-Agent", getContext().getString(R.string.user_agent_string));

                in = conn.getInputStream();
                status = conn.getResponseCode();

                if (status < 400) {
                    final InputStreamReader isr = new InputStreamReader(in);
                    config = GsonUtils.fromJson(isr, DashboardConfig.class);
                    conn.disconnect();

                    /* Now write the config file to cache */
                    FileOutputStream cacheOutStream = new FileOutputStream(configFile);
                    OutputStreamWriter streamWriter = new OutputStreamWriter(cacheOutStream);

                    streamWriter.write(GsonUtils.toJson(config));
                    streamWriter.close();
                }
            } else {
                final FileInputStream fileIn = new FileInputStream(configFile);
                final InputStreamReader isr = new InputStreamReader(fileIn);
                config = GsonUtils.fromJson(isr, DashboardConfig.class);
            }

            return (config != null) ? config.getFeeds() : null;
        } catch (UnknownHostException e) {
            if (mActivity != null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "No network connection.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (IOException e) {
            Log.d(TAG, "error reading feed");
            Log.d(TAG, Log.getStackTraceString(e));
        } catch (IllegalStateException e) {
            Log.d(TAG, "this should not happen");
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return null;
    }
}
