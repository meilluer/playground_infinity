package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.fragments.OfflinePostFragment;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;

public class OfflinePostsActivity extends BaseActivity {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;

    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_posts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
        if (subredditName != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(subredditName);
            }
        }

        if (savedInstanceState == null) {
            OfflinePostFragment fragment = new OfflinePostFragment();
            Bundle bundle = new Bundle();
            bundle.putString(PostFragment.EXTRA_NAME, subredditName);
            bundle.putInt(PostFragment.EXTRA_POST_TYPE, ml.docilealligator.infinityforreddit.post.PostPagingSource.TYPE_SUBREDDIT);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        // Handled by BaseActivity or ThemeUtils
    }
}
