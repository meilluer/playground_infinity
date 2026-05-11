package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.preference.CustomFontPreferenceFragmentCompat;

public class ApiKeysPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    public int getPreferenceResourceId() {
        return R.xml.api_keys_preferences;
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.api_keys_preferences, rootKey);
    }
}