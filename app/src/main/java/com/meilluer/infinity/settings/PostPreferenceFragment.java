package com.meilluer.infinity.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import com.meilluer.infinity.R;
import com.meilluer.infinity.customviews.preference.CustomFontPreferenceFragmentCompat;
import com.meilluer.infinity.events.ChangeCompactLayoutToolbarHiddenByDefaultEvent;
import com.meilluer.infinity.events.ChangeDefaultLinkPostLayoutEvent;
import com.meilluer.infinity.events.ChangeDefaultPostLayoutEvent;
import com.meilluer.infinity.events.ChangeFixedHeightPreviewInCardEvent;
import com.meilluer.infinity.events.ChangeHidePostFlairEvent;
import com.meilluer.infinity.events.ChangeHidePostTypeEvent;
import com.meilluer.infinity.events.ChangeHideSubredditAndUserPrefixEvent;
import com.meilluer.infinity.events.ChangeHideTextPostContent;
import com.meilluer.infinity.events.ChangeHideTheNumberOfCommentsEvent;
import com.meilluer.infinity.events.ChangeHideTheNumberOfVotesEvent;
import com.meilluer.infinity.events.ChangeLongPressToHideToolbarInCompactLayoutEvent;
import com.meilluer.infinity.events.ChangeShowAbsoluteNumberOfVotesEvent;
import com.meilluer.infinity.events.ShowDividerInCompactLayoutPreferenceEvent;
import com.meilluer.infinity.events.ShowThumbnailOnTheLeftInCompactLayoutEvent;
import com.meilluer.infinity.utils.SharedPreferencesUtils;

public class PostPreferenceFragment extends CustomFontPreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.post_preferences, rootKey);

        ListPreference defaultPostLayoutList = findPreference(SharedPreferencesUtils.DEFAULT_POST_LAYOUT_KEY);
        ListPreference defaultLinkPostLayoutList = findPreference(SharedPreferencesUtils.DEFAULT_LINK_POST_LAYOUT_KEY);
        SwitchPreference showDividerInCompactLayoutSwitch = findPreference(SharedPreferencesUtils.SHOW_DIVIDER_IN_COMPACT_LAYOUT);
        SwitchPreference showThumbnailOnTheLeftInCompactLayoutSwitch = findPreference(SharedPreferencesUtils.SHOW_THUMBNAIL_ON_THE_LEFT_IN_COMPACT_LAYOUT);
        SwitchPreference showAbsoluteNumberOfVotesSwitch = findPreference(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES);
        SwitchPreference longPressToHideToolbarInCompactLayoutSwitch = findPreference(SharedPreferencesUtils.LONG_PRESS_TO_HIDE_TOOLBAR_IN_COMPACT_LAYOUT);
        SwitchPreference postCompactLayoutToolbarHiddenByDefaultSwitch = findPreference(SharedPreferencesUtils.POST_COMPACT_LAYOUT_TOOLBAR_HIDDEN_BY_DEFAULT);
        SwitchPreference hidePostTypeSwitch = findPreference(SharedPreferencesUtils.HIDE_POST_TYPE);
        SwitchPreference hidePostFlairSwitch = findPreference(SharedPreferencesUtils.HIDE_POST_FLAIR);
        SwitchPreference hideSubredditAndUserPrefixSwitch = findPreference(SharedPreferencesUtils.HIDE_SUBREDDIT_AND_USER_PREFIX);
        SwitchPreference hideTheNumberOfVotesSwitch = findPreference(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_VOTES);
        SwitchPreference hideTheNumberOfCommentsSwitch = findPreference(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_COMMENTS);
        SwitchPreference hideTextPostContentSwitch = findPreference(SharedPreferencesUtils.HIDE_TEXT_POST_CONTENT);
        SwitchPreference fixedHeightPreviewInCardSwitch = findPreference(SharedPreferencesUtils.FIXED_HEIGHT_PREVIEW_IN_CARD);

        if (defaultPostLayoutList != null) {
            defaultPostLayoutList.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeDefaultPostLayoutEvent(Integer.parseInt((String) newValue)));
                return true;
            });
        }

        if (defaultLinkPostLayoutList != null) {
            defaultLinkPostLayoutList.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeDefaultLinkPostLayoutEvent(Integer.parseInt((String) newValue)));
                return true;
            });
        }

        if (showDividerInCompactLayoutSwitch != null) {
            showDividerInCompactLayoutSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ShowDividerInCompactLayoutPreferenceEvent((Boolean) newValue));
                return true;
            });
        }

        if (showThumbnailOnTheLeftInCompactLayoutSwitch != null) {
            showThumbnailOnTheLeftInCompactLayoutSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ShowThumbnailOnTheLeftInCompactLayoutEvent((Boolean) newValue));
                return true;
            });
        }

        if (showAbsoluteNumberOfVotesSwitch != null) {
            showAbsoluteNumberOfVotesSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeShowAbsoluteNumberOfVotesEvent((Boolean) newValue));
                return true;
            });
        }

        if (longPressToHideToolbarInCompactLayoutSwitch != null) {
            longPressToHideToolbarInCompactLayoutSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeLongPressToHideToolbarInCompactLayoutEvent((Boolean) newValue));
                return true;
            });
        }

        if (postCompactLayoutToolbarHiddenByDefaultSwitch != null) {
            postCompactLayoutToolbarHiddenByDefaultSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeCompactLayoutToolbarHiddenByDefaultEvent((Boolean) newValue));
                return true;
            });
        }

        if (hidePostTypeSwitch != null) {
            hidePostTypeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHidePostTypeEvent((Boolean) newValue));
                return true;
            });
        }

        if (hidePostFlairSwitch != null) {
            hidePostFlairSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHidePostFlairEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideSubredditAndUserPrefixSwitch != null) {
            hideSubredditAndUserPrefixSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideSubredditAndUserPrefixEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideTheNumberOfVotesSwitch != null) {
            hideTheNumberOfVotesSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideTheNumberOfVotesEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideTheNumberOfCommentsSwitch != null) {
            hideTheNumberOfCommentsSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideTheNumberOfCommentsEvent((Boolean) newValue));
                return true;
            });
        }

        if (hideTextPostContentSwitch != null) {
            hideTextPostContentSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeHideTextPostContent((Boolean) newValue));
                return true;
            });
        }

        if (fixedHeightPreviewInCardSwitch != null) {
            fixedHeightPreviewInCardSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeFixedHeightPreviewInCardEvent((Boolean) newValue));
                return true;
            });
        }
    }
}