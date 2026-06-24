package com.meilluer.infinity.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.giphy.sdk.ui.views.GiphyDialogFragment;

import com.meilluer.infinity.activities.BaseActivity;
import com.meilluer.infinity.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import com.meilluer.infinity.databinding.FragmentGiphyGifInfoBottomSheetBinding;

public class GiphyGifInfoBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private BaseActivity activity;

    public GiphyGifInfoBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentGiphyGifInfoBottomSheetBinding binding = FragmentGiphyGifInfoBottomSheetBinding.inflate(inflater, container, false);

        binding.selectGiphyGifButtonUploadedImagesBottomSheetFragment.setOnClickListener(view -> {
            GiphyDialogFragment.Companion.newInstance().show(activity.getSupportFragmentManager(), "giphy_dialog");
            dismiss();
        });

        return binding.getRoot();
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
    }
}