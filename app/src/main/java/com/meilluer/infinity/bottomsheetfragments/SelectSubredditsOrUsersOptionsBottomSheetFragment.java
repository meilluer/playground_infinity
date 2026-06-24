package com.meilluer.infinity.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.meilluer.infinity.activities.SelectedSubredditsAndUsersActivity;
import com.meilluer.infinity.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import com.meilluer.infinity.databinding.FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding;
import com.meilluer.infinity.utils.Utils;

public class SelectSubredditsOrUsersOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private SelectedSubredditsAndUsersActivity activity;

    public SelectSubredditsOrUsersOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding binding = FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding.inflate(inflater, container, false);

        binding.selectSubredditsTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.setOnClickListener(view -> {
            activity.selectSubreddits();
            dismiss();
        });

        binding.selectUsersTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.setOnClickListener(view -> {
            activity.selectUsers();
            dismiss();
        });

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (SelectedSubredditsAndUsersActivity) context;
    }
}