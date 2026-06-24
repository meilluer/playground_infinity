package com.meilluer.infinity.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.meilluer.infinity.R;
import com.meilluer.infinity.activities.ViewImgurMediaActivity;
import com.meilluer.infinity.activities.ViewRedditGalleryActivity;
import com.meilluer.infinity.activities.ViewVideoActivity;
import com.meilluer.infinity.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import com.meilluer.infinity.databinding.FragmentPlaybackSpeedBinding;
import com.meilluer.infinity.fragments.ViewImgurVideoFragment;
import com.meilluer.infinity.fragments.ViewRedditGalleryVideoFragment;
import com.meilluer.infinity.utils.Utils;

public class PlaybackSpeedBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_PLAYBACK_SPEED = "EPS";

    private Activity activity;

    public PlaybackSpeedBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentPlaybackSpeedBinding binding = FragmentPlaybackSpeedBinding.inflate(inflater, container, false);

        int playbackSpeed = getArguments().getInt(EXTRA_PLAYBACK_SPEED, ViewVideoActivity.PLAYBACK_SPEED_NORMAL);
        switch (playbackSpeed) {
            case ViewVideoActivity.PLAYBACK_SPEED_25:
                binding.playbackSpeed025TextViewPlaybackSpeedBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_day_night_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_50:
                binding.playbackSpeed050TextViewPlaybackSpeedBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_day_night_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_75:
                binding.playbackSpeed075TextViewPlaybackSpeedBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_day_night_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_NORMAL:
                binding.playbackSpeedNormalTextViewPlaybackSpeedBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_day_night_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_125:
                binding.playbackSpeed125TextViewPlaybackSpeedBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_day_night_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_150:
                binding.playbackSpeed150TextViewPlaybackSpeedBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_day_night_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_175:
                binding.playbackSpeed175TextViewPlaybackSpeedBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_day_night_24dp, 0);
                break;
            case ViewVideoActivity.PLAYBACK_SPEED_200:
                binding.playbackSpeed200TextViewPlaybackSpeedBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playback_speed_day_night_24dp, 0);
                break;
        }

        binding.playbackSpeed025TextViewPlaybackSpeedBottomSheetFragment.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_25);
            dismiss();
        });

        binding.playbackSpeed050TextViewPlaybackSpeedBottomSheetFragment.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_50);
            dismiss();
        });

        binding.playbackSpeed075TextViewPlaybackSpeedBottomSheetFragment.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_75);
            dismiss();
        });

        binding.playbackSpeedNormalTextViewPlaybackSpeedBottomSheetFragment.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_NORMAL);
            dismiss();
        });

        binding.playbackSpeed125TextViewPlaybackSpeedBottomSheetFragment.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_125);
            dismiss();
        });

        binding.playbackSpeed150TextViewPlaybackSpeedBottomSheetFragment.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_150);
            dismiss();
        });

        binding.playbackSpeed175TextViewPlaybackSpeedBottomSheetFragment.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_175);
            dismiss();
        });

        binding.playbackSpeed200TextViewPlaybackSpeedBottomSheetFragment.setOnClickListener(view -> {
            setPlaybackSpeed(ViewVideoActivity.PLAYBACK_SPEED_200);
            dismiss();
        });

        if (activity instanceof ViewVideoActivity) {
            if (((ViewVideoActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((ViewVideoActivity) activity).typeface);
            }
        } else if (activity instanceof ViewImgurMediaActivity) {
            if (((ViewImgurMediaActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((ViewImgurMediaActivity) activity).typeface);
            }
        } else if (activity instanceof ViewRedditGalleryActivity) {
            if (((ViewRedditGalleryActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((ViewRedditGalleryActivity) activity).typeface);
            }
        }
        return binding.getRoot();
    }

    private void setPlaybackSpeed(int playbackSpeed) {
        if (activity instanceof ViewVideoActivity) {
            ((ViewVideoActivity) activity).setPlaybackSpeed(playbackSpeed);
        } else {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof ViewImgurVideoFragment) {
                ((ViewImgurVideoFragment) parentFragment).setPlaybackSpeed(playbackSpeed);
            } else if (parentFragment instanceof ViewRedditGalleryVideoFragment) {
                ((ViewRedditGalleryVideoFragment) parentFragment).setPlaybackSpeed(playbackSpeed);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}