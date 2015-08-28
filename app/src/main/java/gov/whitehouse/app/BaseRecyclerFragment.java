package gov.whitehouse.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import gov.whitehouse.R;
import gov.whitehouse.widget.BaseAdapter;

public abstract
class BaseRecyclerFragment<T> extends BaseFragment
{

    private
    BaseAdapter<T> mAdapter;

    @InjectView(R.id.progress)
    @Optional
    ProgressBar mProgress;

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;

    @InjectView(R.id.empty)
    TextView mEmptyView;

    public abstract
    BaseAdapter<T> onCreateAdapter();

    public abstract
    RecyclerView.LayoutManager onCreateLayoutManager();

    @Override
    public
    void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (mAdapter == null) {
            mAdapter = onCreateAdapter();
        }
    }

    @Nullable
    @Override
    public
    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public
    void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(onCreateLayoutManager());
        mRecyclerView.setAdapter(mAdapter);
    }

    public BaseAdapter<T>
    getAdapter()
    {
        return mAdapter;
    }

    public TextView
    getEmptyView()
    {
        return mEmptyView;
    }

    public ProgressBar
    getProgressBar()
    {
        return mProgress;
    }

    public RecyclerView
    getRecyclerView()
    {
        return mRecyclerView;
    }

    public void
    showEmpty(boolean show)
    {
        if (show) {
            getEmptyView().setVisibility(View.VISIBLE);
        } else {
            getEmptyView().setVisibility(View.GONE);
        }
    }

    public void
    showList(boolean show)
    {
        if (show) {
            getRecyclerView().setVisibility(View.VISIBLE);
        } else {
            getRecyclerView().setVisibility(View.GONE);
        }
    }

    public void
    showProgress(boolean show)
    {
        if (show) {
            getProgressBar().setVisibility(View.VISIBLE);
        } else {
            getProgressBar().setVisibility(View.GONE);
        }
    }

    public void
    crossfadeIntoProgress()
    {
        View fromView;
        if (getAdapter().getItemCount() > 0) {
            fromView = getRecyclerView();
            showEmpty(false);
        } else {
            fromView = getEmptyView();
            showList(false);
        }
        fromView.setVisibility(View.VISIBLE);
        showProgress(true);
        fromView.setAlpha(1);
        getProgressBar().setAlpha(0);
        fromView.animate()
                .alpha(0)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public
                    void onAnimationEnd(Animator animation)
                    {
                        fromView.setVisibility(View.GONE);
                    }
                });
        getProgressBar().animate()
                .alpha(1)
                .setDuration(250)
                .setListener(null);
    }

    public void
    crossfadeIntoListOrEmpty()
    {
        View toView;
        if (getAdapter().getItemCount() > 0) {
            toView = getRecyclerView();
            showEmpty(false);
        } else {
            toView = getEmptyView();
            showList(false);
        }
        toView.setVisibility(View.VISIBLE);
        showProgress(true);
        getProgressBar().setAlpha(1);
        toView.setAlpha(0);
        getProgressBar().animate()
                .alpha(0)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public
                    void onAnimationEnd(Animator animation)
                    {
                        showProgress(false);
                    }
                });
        toView.animate()
                .alpha(1)
                .setDuration(250)
                .setListener(null);
    }
}
