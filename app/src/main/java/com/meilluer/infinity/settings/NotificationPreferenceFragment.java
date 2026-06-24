package com.meilluer.infinity.settings;

import android.os.Bundle;

import com.meilluer.infinity.R;
import com.meilluer.infinity.customviews.preference.CustomFontPreferenceFragmentCompat;
import com.meilluer.infinity.liveactivity.LiveActivityUtils;

public class NotificationPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey);

        findPreference("enable_live_activity").setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                LiveActivityUtils.enableLiveActivity(requireContext());
                LiveActivityUtils.scheduleWorker(requireContext());
            } else {
                LiveActivityUtils.cancelWorker(requireContext());
            }
            return true;
        });

        findPreference("live_activity_refresh_interval").setOnPreferenceChangeListener((preference, newValue) -> {
            LiveActivityUtils.scheduleWorker(requireContext());
            return true;
        });

        findPreference("live_activity_follow_duration").setOnPreferenceChangeListener((preference, newValue) -> true);
    }
}
