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

package gov.whitehouse.ui.fragments.app;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.core.SearchResult;
import gov.whitehouse.ui.adapters.SearchResultsListAdapter;
import gov.whitehouse.ui.loaders.SearchResultsLoader;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;

public class SearchResultsFragment extends SherlockListFragment
        implements LoaderManager.LoaderCallbacks<List<SearchResult>> {

    private static int LOADER_SEARCH = 404;

    private static String TAG = "SearchResultsFragment";

    private SearchResultsListAdapter mAdapter;

    private List<SearchResult> mSearchResults;

    private String mQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        final int padding = (int) (applyDimension(COMPLEX_UNIT_DIP, 6.0f,
                getResources().getDisplayMetrics()) + 0.5f);
        getListView().setPadding(padding, padding, padding, padding);
        getView().setBackgroundColor(getResources().getColor(R.color.dashboard_header_background));
        setListShown(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mSearchResults.size() > position) {
            SearchResult result = mSearchResults.get(position);
            Intent nextIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getUnescapedUrl()));
            this.startActivity(nextIntent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();

        actionBar.setLogo(R.drawable.logo_wh);
        actionBar.setIcon(R.drawable.ic_menu);

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
    }

    @Override
    public Loader<List<SearchResult>> onCreateLoader(int i, Bundle bundle) {
        return new SearchResultsLoader(getSherlockActivity(), mQuery);
    }

    @Override
    public void onLoadFinished(Loader<List<SearchResult>> listLoader,
            List<SearchResult> searchResults) {
        mSearchResults = new ArrayList<SearchResult>();
        mSearchResults.addAll(searchResults);

        mAdapter = new SearchResultsListAdapter(getSherlockActivity());
        mAdapter.fillWithItems(mSearchResults);
        mAdapter.notifyDataSetChanged();

        setListAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<SearchResult>> listLoader) {
        mSearchResults.clear();
    }

    public void doSearchForQuery(final String searchQuery) {
        mQuery = searchQuery;

        if (mQuery != null && mQuery.length() > 0) {
            setEmptyText(getString(R.string.search_no_results));
        } else {
            setEmptyText(null);
        }

        if (getLoaderManager().getLoader(LOADER_SEARCH) != null) {
            getLoaderManager().destroyLoader(LOADER_SEARCH);
        }
        getLoaderManager().restartLoader(LOADER_SEARCH, null, this);
    }
}
