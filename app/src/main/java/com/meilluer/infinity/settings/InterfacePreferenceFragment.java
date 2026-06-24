package com.meilluer.infinity.settings;


import android.os.Build;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import com.meilluer.infinity.R;
import com.meilluer.infinity.customviews.preference.CustomFontPreferenceFragmentCompat;
import com.meilluer.infinity.events.ChangeHideFabInPostFeedEvent;
import com.meilluer.infinity.events.ChangeVoteButtonsPositionEvent;
import com.meilluer.infinity.events.RecreateActivityEvent;
import com.meilluer.infinity.utils.SharedPreferencesUtils;

public class InterfacePreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.interface_preferences, rootKey);

        Preference immersiveInterfaceEntryPreference = findPreference(SharedPreferencesUtils.IMMERSIVE_INTERFACE_ENTRY_KEY);
        SwitchPreference hideFabInPostFeedSwitchPreference = findPreference(SharedPreferencesUtils.HIDE_FAB_IN_POST_FEED);
        SwitchPreference bottomAppBarSwitch = findPreference(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY);
        SwitchPreference voteButtonsOnTheRightSwitch = findPreference(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY);

        if (immersiveInterfaceEntryPreference != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            immersiveInterfaceEntryPreference.setVisible(true);
        }

        if (hideFabInPostFeedSwitchPreference != null) {
            hideFabInPostFeedSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideFabInPostFeedEvent((Boolean) newValue));
                return true;
            });
        }

        if (bottomAppBarSwitch != null) {
            bottomAppBarSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }

        if (voteButtonsOnTheRightSwitch != null) {
            voteButtonsOnTheRightSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeVoteButtonsPositionEvent((Boolean) newValue));
                return true;
            });
        }
    }
}
