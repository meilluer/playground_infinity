package ml.docilealligator.infinityforreddit.settings;

import android.content.Intent;
import android.os.Bundle;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.CustomThemeListingActivity;
import ml.docilealligator.infinityforreddit.customviews.preference.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

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