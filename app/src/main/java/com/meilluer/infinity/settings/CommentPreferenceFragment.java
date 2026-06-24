package com.meilluer.infinity.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import javax.inject.Inject;
import javax.inject.Named;

import com.meilluer.infinity.Infinity;
import com.meilluer.infinity.R;
import com.meilluer.infinity.customviews.preference.CustomFontPreferenceFragmentCompat;
import com.meilluer.infinity.customviews.preference.SliderPreference;
import com.meilluer.infinity.utils.SharedPreferencesUtils;

public class CommentPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.comment_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        SwitchPreference showCommentDividerSwitchPreference = findPreference(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER);
        ListPreference commentDividerTypeListPreference = findPreference(SharedPreferencesUtils.COMMENT_DIVIDER_TYPE);
        SliderPreference showFewerToolbarOptionsThresholdSliderPreference = findPreference(SharedPreferencesUtils.SHOW_FEWER_TOOLBAR_OPTIONS_THRESHOLD);

        if (showCommentDividerSwitchPreference != null && commentDividerTypeListPreference != null) {
            commentDividerTypeListPreference.setVisible(sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER, false));
            showCommentDividerSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                commentDividerTypeListPreference.setVisible((Boolean) newValue);
                return true;
            });
        }

        if (showFewerToolbarOptionsThresholdSliderPreference != null) {
            showFewerToolbarOptionsThresholdSliderPreference.setSummaryTemplate(R.string.settings_show_fewer_toolbar_options_threshold_summary);
        }
    }
}