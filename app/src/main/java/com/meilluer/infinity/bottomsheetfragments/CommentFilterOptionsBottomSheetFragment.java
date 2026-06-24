package com.meilluer.infinity.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.meilluer.infinity.activities.CommentFilterPreferenceActivity;
import com.meilluer.infinity.commentfilter.CommentFilter;
import com.meilluer.infinity.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import com.meilluer.infinity.databinding.FragmentCommentFilterOptionsBottomSheetBinding;
import com.meilluer.infinity.utils.Utils;

public class CommentFilterOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_COMMENT_FILTER = "ECF";
    private CommentFilterPreferenceActivity activity;

    public CommentFilterOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentCommentFilterOptionsBottomSheetBinding binding = FragmentCommentFilterOptionsBottomSheetBinding.inflate(inflater, container, false);

        CommentFilter commentFilter = getArguments().getParcelable(EXTRA_COMMENT_FILTER);

        binding.editTextViewCommentFilterOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.editCommentFilter(commentFilter);
            dismiss();
        });

        binding.applyToTextViewCommentFilterOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.applyCommentFilterTo(commentFilter);
            dismiss();
        });

        binding.deleteTextViewCommentFilterOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.deleteCommentFilter(commentFilter);
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
        activity = (CommentFilterPreferenceActivity) context;
    }
}