package com.meilluer.infinity.settings;

import android.os.Bundle;

import com.meilluer.infinity.R;
import com.meilluer.infinity.customviews.preference.CustomFontPreferenceFragmentCompat;

public class ApiKeysPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    public int getPreferenceResourceId() {
        return R.xml.api_keys_preferences;
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.api_keys_preferences, rootKey);
    }
}