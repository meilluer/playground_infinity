package ml.docilealligator.infinityforreddit.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivitySavedSubredditsBinding;
import ml.docilealligator.infinityforreddit.subreddit.SavedSubredditsManager;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;

import javax.inject.Inject;
import javax.inject.Named;

public class SavedSubredditsActivity extends BaseActivity implements SubredditAutocompleteRecyclerViewAdapter.ItemOnClickListener {

    private ActivitySavedSubredditsBinding binding;
    private SavedSubredditsManager savedSubredditsManager;
    private SubredditAutocompleteRecyclerViewAdapter adapter;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("light_theme")
    SharedPreferences mLightThemeSharedPreferences;
    @Inject
    @Named("dark_theme")
    SharedPreferences mDarkThemeSharedPreferences;
    @Inject
    @Named("amoled_theme")
    SharedPreferences mAmoledThemeSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Infinity) getApplication()).getAppComponent().inject(this);
        binding = ActivitySavedSubredditsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarSavedSubredditsActivity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.saved_subreddits);
        }

        savedSubredditsManager = new SavedSubredditsManager(this);
        adapter = new SubredditAutocompleteRecyclerViewAdapter(this, new CustomThemeWrapper(mLightThemeSharedPreferences, mDarkThemeSharedPreferences, mAmoledThemeSharedPreferences), this);
        binding.recyclerViewSavedSubreddits.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSavedSubreddits.setAdapter(adapter);

        loadSavedSubreddits();
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return new CustomThemeWrapper(mLightThemeSharedPreferences, mDarkThemeSharedPreferences, mAmoledThemeSharedPreferences);
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(getCustomThemeWrapper().getBackgroundColor());
        binding.toolbarSavedSubredditsActivity.setBackgroundColor(getCustomThemeWrapper().getColorPrimary());
        binding.toolbarSavedSubredditsActivity.setTitleTextColor(getCustomThemeWrapper().getToolbarPrimaryTextAndIconColor());
    }

    private void loadSavedSubreddits() {
        List<SubredditData> savedSubreddits = new ArrayList<>(savedSubredditsManager.getSavedSubreddits());
        adapter.setSubreddits(savedSubreddits);
    }

    @Override
    public void onClick(SubredditData subredditData) {
        // Handle click on saved subreddit (e.g., navigate to subreddit detail)
        // For now, just remove it from the saved list
        savedSubredditsManager.removeSubreddit(subredditData);
        loadSavedSubreddits();
    }
}