package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceGroup;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivitySettingsBinding;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.settings.AboutPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.AdvancedPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.FontPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.GesturesAndButtonsPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.InterfacePreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.MainPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.PostPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.SettingsSearchFragment;
import ml.docilealligator.infinityforreddit.settings.ApiKeysPreferenceFragment;
import ml.docilealligator.infinityforreddit.activities.SavedSubredditsActivity;
import ml.docilealligator.infinityforreddit.settings.DataSavingModePreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.VideoPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.TimeFormatPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.ThemePreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.SwipeActionPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.SortTypePreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.SecurityPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.ProxyPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.PostDetailsPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.NumberOfColumnsInPostFeedPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.NotificationPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.NavigationDrawerPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.MiscellaneousPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.ImmersiveInterfacePreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.DownloadLocationPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.CreditsPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.CommentPreferenceFragment;

public class SettingsActivity extends BaseActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, SettingsSearchFragment.OnSearchResultClickListener {

    private static final String TITLE_STATE = "TS";

    private ActivitySettingsBinding binding;
    private SearchView searchView;

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

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSettingsActivity);
            }

            if (isImmersiveInterface()) {
                ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                        Insets allInsets = insets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                                        | WindowInsetsCompat.Type.displayCutout()
                        );

                        setMargins(binding.toolbarSettingsActivity,
                                allInsets.left,
                                allInsets.top,
                                allInsets.right,
                                BaseActivity.IGNORE_MARGIN);

                        return insets;
                    }
                });
            }
        }

        setSupportActionBar(binding.toolbarSettingsActivity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout_settings_activity, new MainPreferenceFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_STATE));
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout_settings_activity);
            if (fragment instanceof AboutPreferenceFragment) {
                setTitle(R.string.settings_about_master_title);
            } else if (fragment instanceof InterfacePreferenceFragment) {
                setTitle(R.string.settings_interface_title);
            } else if (fragment instanceof FontPreferenceFragment) {
                setTitle(R.string.settings_font_title);
            } else if (fragment instanceof GesturesAndButtonsPreferenceFragment) {
                setTitle(R.string.settings_gestures_and_buttons_title);
            } else if (fragment instanceof PostPreferenceFragment) {
                setTitle(R.string.settings_category_post_title);
            } else if (fragment instanceof AdvancedPreferenceFragment) {
                setTitle(R.string.settings_advanced_master_title);
            } else if (fragment instanceof ApiKeysPreferenceFragment) {
                setTitle(R.string.settings_api_keys_title);
            } else if (fragment instanceof MainPreferenceFragment) {
                setTitle(R.string.settings_activity_label);
            }
        });
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSettingsActivity,
                binding.collapsingToolbarLayoutSettingsActivity, binding.toolbarSettingsActivity);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_saved_subreddits) {
            Intent intent = new Intent(this, SavedSubredditsActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_STATE, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        return true;
    }

    private void search(String query) {
        if (query.isEmpty()) {
            getSupportFragmentManager().popBackStack();
            return;
        }

        List<SettingsSearchFragment.SearchResult> results = new ArrayList<>();
        List<Integer> preferenceResourceIds = new ArrayList<>();
        preferenceResourceIds.add(R.xml.main_preferences);
        preferenceResourceIds.add(R.xml.interface_preferences);
        preferenceResourceIds.add(R.xml.post_preferences);
        preferenceResourceIds.add(R.xml.gestures_and_buttons_preferences);
        preferenceResourceIds.add(R.xml.font_preferences);
        preferenceResourceIds.add(R.xml.advanced_preferences);
        preferenceResourceIds.add(R.xml.about_preferences);
        preferenceResourceIds.add(R.xml.api_keys_preferences);
        preferenceResourceIds.add(R.xml.data_saving_mode_preferences);
        preferenceResourceIds.add(R.xml.video_preferences);
        preferenceResourceIds.add(R.xml.time_format_preferences);
        preferenceResourceIds.add(R.xml.theme_preferences);
        preferenceResourceIds.add(R.xml.swipe_action_preferences);
        preferenceResourceIds.add(R.xml.sort_type_preferences);
        preferenceResourceIds.add(R.xml.security_preferences);
        preferenceResourceIds.add(R.xml.proxy_preferences);
        preferenceResourceIds.add(R.xml.post_details_preferences);
        preferenceResourceIds.add(R.xml.number_of_columns_in_post_feed_preferences);
        preferenceResourceIds.add(R.xml.notification_preferences);
        preferenceResourceIds.add(R.xml.navigation_drawer_preferences);
        preferenceResourceIds.add(R.xml.miscellaneous_preferences);
        preferenceResourceIds.add(R.xml.immersive_interface_preferences);
        preferenceResourceIds.add(R.xml.download_location_preferences);
        preferenceResourceIds.add(R.xml.credits_preferences);
        preferenceResourceIds.add(R.xml.comment_preferences);

        PreferenceManager preferenceManager = new PreferenceManager(this);

        for (Integer resourceId : preferenceResourceIds) {
            PreferenceScreen screen = preferenceManager.inflateFromResource(this, resourceId, null);
            addPreferencesToList(screen, query, getFragmentClassForResourceId(resourceId).getName(), results);
        }

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout_settings_activity);
        SettingsSearchFragment searchFragment;

        if (!(currentFragment instanceof SettingsSearchFragment)) {
            searchFragment = new SettingsSearchFragment();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.frame_layout_settings_activity, searchFragment)
                    .addToBackStack(null)
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        } else {
            searchFragment = (SettingsSearchFragment) currentFragment;
        }

        if (searchFragment != null) {
            searchFragment.displayResults(results);
        }
    }

    private void addPreferencesToList(PreferenceGroup preferenceGroup, String query, String fragmentClass, List<SettingsSearchFragment.SearchResult> results) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference.getTitle() != null && preference.getTitle().toString().toLowerCase().contains(query.toLowerCase())) {
                if (preference.getKey() != null) {
                    results.add(new SettingsSearchFragment.SearchResult(preference.getTitle(), preference.getSummary(), fragmentClass, preference.getKey()));
                }
            }

            if (preference instanceof PreferenceGroup) {
                addPreferencesToList((PreferenceGroup) preference, query, fragmentClass, results);
            }
        }
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.frame_layout_settings_activity, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void showSnackbar(int stringId, int actionStringId, View.OnClickListener onClickListener) {
        Snackbar.make(binding.getRoot(), stringId, BaseTransientBottomBar.LENGTH_SHORT).setAction(actionStringId, onClickListener).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecreateActivityEvent(RecreateActivityEvent recreateActivityEvent) {
        ActivityCompat.recreate(this);
    }

    private Class<? extends Fragment> getFragmentClassForResourceId(int resourceId) {
        if (resourceId == R.xml.main_preferences) {
            return MainPreferenceFragment.class;
        } else if (resourceId == R.xml.interface_preferences) {
            return InterfacePreferenceFragment.class;
        } else if (resourceId == R.xml.post_preferences) {
            return PostPreferenceFragment.class;
        } else if (resourceId == R.xml.gestures_and_buttons_preferences) {
            return GesturesAndButtonsPreferenceFragment.class;
        } else if (resourceId == R.xml.font_preferences) {
            return FontPreferenceFragment.class;
        } else if (resourceId == R.xml.advanced_preferences) {
            return AdvancedPreferenceFragment.class;
        } else if (resourceId == R.xml.about_preferences) {
            return AboutPreferenceFragment.class;
        } else if (resourceId == R.xml.api_keys_preferences) {
            return ApiKeysPreferenceFragment.class;
        } else if (resourceId == R.xml.data_saving_mode_preferences) {
            return DataSavingModePreferenceFragment.class;
        } else if (resourceId == R.xml.video_preferences) {
            return VideoPreferenceFragment.class;
        } else if (resourceId == R.xml.time_format_preferences) {
            return TimeFormatPreferenceFragment.class;
        } else if (resourceId == R.xml.theme_preferences) {
            return ThemePreferenceFragment.class;
        } else if (resourceId == R.xml.swipe_action_preferences) {
            return SwipeActionPreferenceFragment.class;
        } else if (resourceId == R.xml.sort_type_preferences) {
            return SortTypePreferenceFragment.class;
        } else if (resourceId == R.xml.security_preferences) {
            return SecurityPreferenceFragment.class;
        } else if (resourceId == R.xml.proxy_preferences) {
            return ProxyPreferenceFragment.class;
        } else if (resourceId == R.xml.post_details_preferences) {
            return PostDetailsPreferenceFragment.class;
        } else if (resourceId == R.xml.number_of_columns_in_post_feed_preferences) {
            return NumberOfColumnsInPostFeedPreferenceFragment.class;
        } else if (resourceId == R.xml.notification_preferences) {
            return NotificationPreferenceFragment.class;
        } else if (resourceId == R.xml.navigation_drawer_preferences) {
            return NavigationDrawerPreferenceFragment.class;
        } else if (resourceId == R.xml.miscellaneous_preferences) {
            return MiscellaneousPreferenceFragment.class;
        } else if (resourceId == R.xml.immersive_interface_preferences) {
            return ImmersiveInterfacePreferenceFragment.class;
        } else if (resourceId == R.xml.download_location_preferences) {
            return DownloadLocationPreferenceFragment.class;
        } else if (resourceId == R.xml.credits_preferences) {
            return CreditsPreferenceFragment.class;
        } else if (resourceId == R.xml.comment_preferences) {
            return CommentPreferenceFragment.class;
        }
        return null;
    }

    @Override
    public void onSearchResultClick(SettingsSearchFragment.SearchResult result) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().popBackStack();
        try {
            Class<?> fragmentClass = Class.forName(result.getFragmentClass());
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.frame_layout_settings_activity, fragment)
                    .addToBackStack(null)
                    .commit();
            getSupportFragmentManager().executePendingTransactions();

            if (fragment instanceof PreferenceFragmentCompat) {
                PreferenceFragmentCompat preferenceFragment = (PreferenceFragmentCompat) fragment;
                Preference preference = preferenceFragment.findPreference(result.getPreferenceKey());
                if (preference != null) {
                    preferenceFragment.scrollToPreference(preference);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
