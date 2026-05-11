package ml.docilealligator.infinityforreddit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.post.OfflinePostViewModel;

public class OfflinePostFragment extends PostFragment {

    @Inject
    RedditDataRoomDatabase database;

    private OfflinePostViewModel offlinePostViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inject dependencies for OfflinePostFragment (specifically database)
        // Note: PostFragment's onCreateView also calls inject, but as PostFragment.
        ((Infinity) requireActivity().getApplication()).getAppComponent().inject(this);

        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Remove observers from the PostViewModel created by super (which tries to load from network)
        if (mPostViewModel != null && mPostViewModel.getPosts() != null) {
            mPostViewModel.getPosts().removeObservers(getViewLifecycleOwner());
        }

        // Disable SwipeRefreshLayout as we are offline
        // binding is private in PostFragment, but we can find the view by ID
        // R.id.swipe_refresh_layout_post_fragment
        if (view != null) {
            View swipeRefresh = view.findViewById(ml.docilealligator.infinityforreddit.R.id.swipe_refresh_layout_post_fragment);
            if (swipeRefresh instanceof androidx.swiperefreshlayout.widget.SwipeRefreshLayout) {
                ((androidx.swiperefreshlayout.widget.SwipeRefreshLayout) swipeRefresh).setEnabled(false);
            }
        }

        String subredditName = getArguments().getString(EXTRA_NAME);
        android.util.Log.d("OfflinePostFragment", "Subreddit name: " + subredditName);

        offlinePostViewModel = new OfflinePostViewModel(database, subredditName, mExecutor);

        offlinePostViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            android.util.Log.d("OfflinePostFragment", "Received posts for offline viewing");
            if (mAdapter != null) {
                mAdapter.submitData(getViewLifecycleOwner().getLifecycle(), posts);
            }
        });

        return view;
    }

    @Override
    protected void initializeAndBindPostViewModel() {
        // Do nothing to prevent network call
    }

    @Override
    protected void initializeAndBindPostViewModelForAnonymous(String concatenatedSubredditNames) {
        // Do nothing to prevent network call
    }

    @Override
    protected void showErrorView(int stringResId) {
        // Do nothing to prevent PostFragment from hiding RecyclerView when network fails
    }
}
