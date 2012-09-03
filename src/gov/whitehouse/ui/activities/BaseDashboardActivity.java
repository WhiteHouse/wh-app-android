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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.eddieringle.android.libs.undergarment.widgets.DrawerGarment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.core.DashboardItem;
import gov.whitehouse.ui.adapters.DashboardListAdapter;
import gov.whitehouse.ui.fragments.app.SearchResultsFragment;
import gov.whitehouse.ui.loaders.DashboardLoader;
import gov.whitehouse.utils.DashboardItemUtils;

import static gov.whitehouse.core.DashboardItem.VIEW_TYPE_LIVE;

public class BaseDashboardActivity extends BaseActivity {

    public static final int LOADER_DASHBOARD = 101;

    public static final String EXTRA_SEARCH_QUERY = "search_query";

    public static final String EXTRA_SHOW_DASH = "show_dash";

    public static final String EXTRA_SHOW_SEARCH = "show_search";

    private static final int SEARCH_FRAGMENT_ANIMATION_DURATION = 300;

    private boolean mShowingDash;

    private boolean mShowingSearch;

    private EditText mSearchField;

    private ListView mDashboardListView;

    private DashboardListAdapter mDashboardListAdapter;

    private ArrayList<DashboardItem> mDashboardItems;

    private SearchResultsFragment mSearchFragment;

    private DrawerGarment mDrawerGarment;

    private class BaseDashboardLoaderCallbacks
            implements LoaderManager.LoaderCallbacks<List<DashboardItem>> {

        @Override
        public Loader<List<DashboardItem>> onCreateLoader(int i, Bundle bundle) {
            return new DashboardLoader(BaseDashboardActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<DashboardItem>> listLoader,
                List<DashboardItem> dashboardItems) {
            final ArrayList<DashboardItem> displayedItems = new ArrayList<DashboardItem>();

            // live is currently supported only for Honeycomb and up...
            Boolean liveSupported = Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT;

            for (DashboardItem item: dashboardItems) {
                Boolean itemIsLive = item.getViewType().equals(VIEW_TYPE_LIVE);
                if (liveSupported || !itemIsLive) {
                    displayedItems.add(item);
                }
            }

            // favorites always go last
            DashboardItem favorites = new DashboardItem();
            favorites.setTitle(getString(R.string.favorites));
            displayedItems.add(favorites);

            mDashboardItems = displayedItems;

            mDashboardListAdapter = new DashboardListAdapter(BaseDashboardActivity.this);
            mDashboardListAdapter.fillWithItems(mDashboardItems);
            mDashboardListView.setAdapter(mDashboardListAdapter);
        }

        @Override
        public void onLoaderReset(Loader<List<DashboardItem>> listLoader) {
        }
    }

    public void showSearch() {
        final int height = mSearchFragment.getView().getHeight();
        TranslateAnimation slideDownAnim = new TranslateAnimation(0, 0, -height, 0);
        slideDownAnim.setDuration(SEARCH_FRAGMENT_ANIMATION_DURATION);
        slideDownAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getSupportFragmentManager().beginTransaction()
                        .show(mSearchFragment)
                        .commitAllowingStateLoss();
                mSearchFragment.getView().requestLayout();
                mDrawerGarment.setDrawerEnabled(false);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                if (!mSearchField.getText().toString().equals("")) {
                    mSearchFragment.setListShown(true);
                    mSearchFragment.doSearchForQuery(mSearchField.getText().toString());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mSearchFragment.getView().startAnimation(slideDownAnim);
        mShowingSearch = true;
    }

    public void hideSearch() {
        final int height = mSearchFragment.getView().getHeight();
        TranslateAnimation slideUpAnim = new TranslateAnimation(0, 0, 0, -height);
        slideUpAnim.setDuration(SEARCH_FRAGMENT_ANIMATION_DURATION);
        slideUpAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getSupportFragmentManager().beginTransaction()
                        .hide(mSearchFragment)
                        .commitAllowingStateLoss();
                mSearchField.setText("");
                mSearchField.clearFocus();
                mDrawerGarment.setDrawerEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mSearchFragment.getView().startAnimation(slideUpAnim);
        mShowingSearch = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            mShowingDash = savedInstanceState.getBoolean(EXTRA_SHOW_DASH, false);
            mShowingSearch = savedInstanceState.getBoolean(EXTRA_SHOW_SEARCH, false);
        }

        mDrawerGarment = new DrawerGarment(this, R.layout.dashboard);
        mDrawerGarment.setDrawerCallbacks(new DrawerGarment.IDrawerCallbacks() {
            @Override
            public void onDrawerOpened() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onDrawerClosed() {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        });

        mSearchField = (EditText) findViewById(R.id.et_search);
        mDashboardListView = (ListView) findViewById(R.id.list);
        mSearchFragment = (SearchResultsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.search_fragment);

        mDashboardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final DashboardItem item = mDashboardItems.get(i);
                final Intent intent = DashboardItemUtils.createIntent(BaseDashboardActivity.this, item);
                BaseDashboardActivity.this.startActivity(intent);
                BaseDashboardActivity.this.finish();
            }
        });

        if (savedInstanceState != null
                && savedInstanceState.getString(EXTRA_SEARCH_QUERY) != null) {
            mSearchField.setText(savedInstanceState.getString(EXTRA_SEARCH_QUERY));
        }

        if (mShowingSearch) {
            showSearch();
        } else {
            hideSearch();
        }

        mSearchField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (focused && !mSearchFragment.isVisible()) {
                    showSearch();
                }
            }
        });

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSearchFragment.setListShown(true);
                mSearchFragment.doSearchForQuery(mSearchField.getText().toString());
            }
        });

        getSupportLoaderManager()
                .initLoader(LOADER_DASHBOARD, null, new BaseDashboardLoaderCallbacks());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (((getSupportActionBar()
                    .getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) == ActionBar
                    .DISPLAY_HOME_AS_UP)) {
                if (mDrawerGarment.isDrawerOpened()) {
                    if (mShowingSearch) {
                        hideSearch();
                    }
                    mDrawerGarment.closeDrawer(true);
                } else {
                    onBackPressed();
                }
            } else {
                mDrawerGarment.openDrawer(true);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mShowingDash) {
            mDrawerGarment.openDrawer(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerGarment.isDrawerOpened()) {
            if (mSearchFragment.isVisible()) {
                hideSearch();
            } else {
                mDrawerGarment.closeDrawer(true);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mSearchField.setText(savedInstanceState.getString(EXTRA_SEARCH_QUERY));
            mShowingDash = savedInstanceState.getBoolean(EXTRA_SHOW_DASH, false);
            mShowingSearch = savedInstanceState.getBoolean(EXTRA_SHOW_SEARCH, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(EXTRA_SEARCH_QUERY, mSearchField.getText().toString());
        outState.putBoolean(EXTRA_SHOW_DASH, mDrawerGarment.isDrawerOpened());
        outState.putBoolean(EXTRA_SHOW_SEARCH, mShowingSearch);
    }

    public DrawerGarment getDrawerGarment() {
        return mDrawerGarment;
    }
}
