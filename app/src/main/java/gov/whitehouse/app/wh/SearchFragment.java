package gov.whitehouse.app.wh;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.List;

import gov.whitehouse.R;
import gov.whitehouse.app.BaseListFragment;
import gov.whitehouse.core.manager.SearchManager;
import gov.whitehouse.data.model.BoostedSearchResult;
import gov.whitehouse.data.model.SearchResult;
import gov.whitehouse.util.NetworkUtils;
import gov.whitehouse.widget.BaseAdapter;
import gov.whitehouse.widget.wh.SearchItemAdapter;
import rx.Observer;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public
class SearchFragment extends BaseListFragment<SearchResult>
{
    private
    OnSearchResultClickedListener mSearchResultClickedListener;

    Subscription mSearchSub;

    @Override
    public
    void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
    }

    @Override
    public
    BaseAdapter<SearchResult> onCreateAdapter()
    {
        return new SearchItemAdapter();
    }

    @Override
    public
    void onStart()
    {
        super.onStart();
        getAdapter().setOnItemClickListener((itemView, position) -> {
            if (getTracker() != null) {
                getTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Search")
                .setAction("itemClick")
                .setLabel(getAdapter().getItem(position).unescapedUrl())
                .build());
            }
            if (mSearchResultClickedListener != null) {
                mSearchResultClickedListener.onSearchResultClicked(getAdapter().getItem(position),
                                                                   position);
            }
        });
        if (getTracker() != null) {
            getTracker().setScreenName("Search");
        }
    }

    @Override
    public
    void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundResource(R.color.wh_secondary_air);
    }

    @Override
    public
    boolean isRootFragment()
    {
        return false;
    }

    public
    void setSearchResultClickedListener(OnSearchResultClickedListener listener)
    {
        mSearchResultClickedListener = listener;
    }

    public
    void submitQuery(String query)
    {
        showList(false);
        showProgress(true);
        if (mSearchSub != null) {
            mSearchSub.unsubscribe();
        }
        if (!NetworkUtils.checkNetworkAvailable(getActivity())) {
            showProgress(false);
            Toast.makeText(getActivity(), R.string.no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        mSearchSub = AndroidObservable.bindFragment(this, SearchManager.get()
                .search(query)
                .first()
                .map(searchResults -> {
                    List<SearchResult> list = new ArrayList<>();
                    SearchResult result;
                    if (searchResults.boosted_results() != null) {
                        for (BoostedSearchResult b : searchResults.boosted_results()) {
                            result = SearchResult.create(b.description(), b.title(), b.url());
                            list.add(result);
                        }
                    }
                    if (searchResults.results() != null) {
                        list.addAll(searchResults.results());
                    }
                    return list;
                })
                .subscribeOn(Schedulers.newThread()))
                .subscribe(new Observer<List<SearchResult>>()
                {
                    @Override
                    public
                    void onCompleted()
                    {
                        showProgress(false);
                        showList(true);
                    }

                    @Override
                    public
                    void onError(Throwable e)
                    {
                        Toast.makeText(getActivity(), "An error occurred while trying to search", Toast.LENGTH_SHORT).show();
                        Timber.w(e, "Error searching for query '%s'", query);
                    }

                    @Override
                    public
                    void onNext(List<SearchResult> searchResults)
                    {
                        getAdapter().clear();
                        if (searchResults != null) {
                            getAdapter().addAll(searchResults);
                        }
                    }
                });
        bindSubscription(mSearchSub);
        if (getTracker() != null) {
            getTracker().set("searchQuery", query);
        }
    }

    public static
    interface OnSearchResultClickedListener
    {
        public
        void onSearchResultClicked(SearchResult result, int position);
    }
}
