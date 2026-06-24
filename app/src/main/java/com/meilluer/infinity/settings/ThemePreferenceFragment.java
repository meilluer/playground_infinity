package com.meilluer.infinity.settings;

import android.content.Intent;
import android.os.Bundle;

import com.meilluer.infinity.R;
import com.meilluer.infinity.activities.CustomThemeListingActivity;
import com.meilluer.infinity.customviews.preference.CustomFontPreferenceFragmentCompat;
import com.meilluer.infinity.utils.SharedPreferencesUtils;

public class ThemePreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);

        findPreference(SharedPreferencesUtils.MANAGE_THEMES).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), CustomThemeListingActivity.class);
            startActivity(intent);
            return true;
        });
    }
}