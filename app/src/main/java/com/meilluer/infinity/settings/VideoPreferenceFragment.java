package com.meilluer.infinity.settings;

import android.os.Bundle;

import com.meilluer.infinity.R;
import com.meilluer.infinity.customviews.preference.CustomFontPreferenceFragmentCompat;

public class VideoPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.video_preferences, rootKey);
    }
}