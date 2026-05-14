package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.preference.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.liveactivity.LiveActivityUtils;

public class NotificationPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey);

        findPreference("enable_live_activity").setOnPreferenceChangeListener((preference, newValue) -> {
            LiveActivityUtils.scheduleWorker(requireContext());
            return true;
        });

        findPreference("live_activity_refresh_interval").setOnPreferenceChangeListener((preference, newValue) -> {
            LiveActivityUtils.scheduleWorker(requireContext());
            return true;
        });
    }
}