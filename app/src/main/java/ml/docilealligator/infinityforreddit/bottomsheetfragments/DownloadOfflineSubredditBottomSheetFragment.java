package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.databinding.FragmentDownloadOfflineSubredditBinding;
import ml.docilealligator.infinityforreddit.thing.SortType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DownloadOfflineSubredditBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentDownloadOfflineSubredditBinding binding;
    private OnDownloadClickListener listener;

    public interface OnDownloadClickListener {
        void onDownloadClick(List<String> subredditNames, int postLimit, SortType.Type sortType, boolean downloadComments, int commentLimit, boolean downloadVideos, boolean downloadImages, boolean downloadText, String imageQuality, String videoQuality);
    }

    public void setListener(OnDownloadClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDownloadOfflineSubredditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        // Setup Sort Spinner
        String[] sortTypes = new String[]{"Best", "Hot", "New", "Top"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortTypes);
        binding.spinnerSortType.setAdapter(adapter);

        // Setup Quality Spinners
        String[] qualities = new String[]{"High", "Low"};
        ArrayAdapter<String> qualityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, qualities);
        binding.spinnerImageQuality.setAdapter(qualityAdapter);

        android.content.SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        int savedCommentLimit = sharedPreferences.getInt("offline_download_comment_limit", 50);
        boolean savedDownloadComments = sharedPreferences.getBoolean("offline_download_comments_enabled", false);
        
        binding.editTextCommentLimit.setText(String.valueOf(savedCommentLimit));
        binding.switchDownloadComments.setChecked(savedDownloadComments);
        binding.inputLayoutCommentLimit.setVisibility(savedDownloadComments ? View.VISIBLE : View.GONE);

        binding.switchDownloadComments.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.inputLayoutCommentLimit.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        binding.buttonDownload.setOnClickListener(v -> {
            String subredditNamesRaw = binding.editTextSubredditName.getText().toString().trim();
            if (TextUtils.isEmpty(subredditNamesRaw)) {
                binding.inputLayoutSubredditName.setError("Subreddit name is required");
                return;
            }
            
            List<String> subredditNames = new ArrayList<>();
            for (String name : subredditNamesRaw.split(",")) {
                if (!name.trim().isEmpty()) {
                    subredditNames.add(name.trim());
                }
            }
            
            if (subredditNames.isEmpty()) {
                binding.inputLayoutSubredditName.setError("Subreddit name is required");
                return;
            }

            String limitStr = binding.editTextPostLimit.getText().toString().trim();
            int limit = 50; // default
            if (!TextUtils.isEmpty(limitStr)) {
                try {
                    limit = Integer.parseInt(limitStr);
                } catch (NumberFormatException e) {
                    binding.inputLayoutPostLimit.setError("Invalid number");
                    return;
                }
            }

            boolean downloadComments = binding.switchDownloadComments.isChecked();
            sharedPreferences.edit().putBoolean("offline_download_comments_enabled", downloadComments).apply();
            int commentLimit = 50; // default
            if (downloadComments) {
                String commentLimitStr = binding.editTextCommentLimit.getText().toString().trim();
                if (!TextUtils.isEmpty(commentLimitStr)) {
                    try {
                        commentLimit = Integer.parseInt(commentLimitStr);
                        sharedPreferences.edit().putInt("offline_download_comment_limit", commentLimit).apply();
                    } catch (NumberFormatException e) {
                        binding.inputLayoutCommentLimit.setError("Invalid number");
                        return;
                    }
                }
            } else {
                commentLimit = 0;
            }

            boolean downloadVideos = false;
            boolean downloadImages = binding.checkboxDownloadImages.isChecked();
            boolean downloadText = binding.checkboxDownloadText.isChecked();
            
            String imageQuality = binding.spinnerImageQuality.getSelectedItem().toString();
            String videoQuality = "";

            SortType.Type sortType = SortType.Type.BEST;
            String selectedSort = binding.spinnerSortType.getSelectedItem().toString();
            switch (selectedSort) {
                case "Hot": sortType = SortType.Type.HOT; break;
                case "New": sortType = SortType.Type.NEW; break;
                case "Top": sortType = SortType.Type.TOP; break;
            }

            if (listener != null) {
                listener.onDownloadClick(subredditNames, limit, sortType, downloadComments, commentLimit, downloadVideos, downloadImages, downloadText, imageQuality, videoQuality);
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
