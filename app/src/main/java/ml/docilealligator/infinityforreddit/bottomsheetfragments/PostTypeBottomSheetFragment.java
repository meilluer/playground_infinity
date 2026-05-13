package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostTypeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_LINK = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_GALLERY = 4;
    public static final int TYPE_POLL = 5;

    public static final String EXTRA_SUBMISSION_TYPE = "EST";
    public static final String EXTRA_ALLOW_VIDEOS = "EAV";
    public static final String EXTRA_ALLOW_IMAGES = "EAI";
    public static final String EXTRA_ALLOW_POLLS = "EAP";
    public static final String EXTRA_ALLOW_GALLERIES = "EAG";

    private BaseActivity activity;

    public PostTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    public static PostTypeBottomSheetFragment newInstance(String submissionType, boolean allowVideos,
                                                          boolean allowImages, boolean allowPolls,
                                                          boolean allowGalleries) {
        PostTypeBottomSheetFragment fragment = new PostTypeBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_SUBMISSION_TYPE, submissionType);
        args.putBoolean(EXTRA_ALLOW_VIDEOS, allowVideos);
        args.putBoolean(EXTRA_ALLOW_IMAGES, allowImages);
        args.putBoolean(EXTRA_ALLOW_POLLS, allowPolls);
        args.putBoolean(EXTRA_ALLOW_GALLERIES, allowGalleries);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentPostTypeBottomSheetBinding binding = FragmentPostTypeBottomSheetBinding.inflate(inflater, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            binding.getRoot().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        Bundle args = getArguments();
        if (args != null) {
            String submissionType = args.getString(EXTRA_SUBMISSION_TYPE, "any");
            boolean allowVideos = args.getBoolean(EXTRA_ALLOW_VIDEOS, true);
            boolean allowImages = args.getBoolean(EXTRA_ALLOW_IMAGES, true);
            boolean allowPolls = args.getBoolean(EXTRA_ALLOW_POLLS, true);
            boolean allowGalleries = args.getBoolean(EXTRA_ALLOW_GALLERIES, true);

            if (submissionType.equals("link")) {
                binding.textTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
            } else if (submissionType.equals("self")) {
                binding.linkTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
                binding.imageTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
                binding.videoTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
                binding.galleryTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
                binding.pollTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
            }

            if (!allowVideos) {
                binding.videoTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
            }
            if (!allowImages) {
                binding.imageTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
            }
            if (!allowPolls) {
                binding.pollTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
            }
            if (!allowGalleries) {
                binding.galleryTypeLinearLayoutPostTypeBottomSheetFragment.setVisibility(View.GONE);
            }
        }

        binding.textTypeLinearLayoutPostTypeBottomSheetFragment.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_TEXT);
            dismiss();
        });

        binding.linkTypeLinearLayoutPostTypeBottomSheetFragment.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_LINK);
            dismiss();
        });

        binding.imageTypeLinearLayoutPostTypeBottomSheetFragment.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_IMAGE);
            dismiss();
        });

        binding.videoTypeLinearLayoutPostTypeBottomSheetFragment.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_VIDEO);
            dismiss();
        });

        binding.galleryTypeLinearLayoutPostTypeBottomSheetFragment.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_GALLERY);
            dismiss();
        });

        binding.pollTypeLinearLayoutPostTypeBottomSheetFragment.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_POLL);
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
        this.activity = (BaseActivity) context;
    }

    public interface PostTypeSelectionCallback {
        void postTypeSelected(int postType);
    }

}
