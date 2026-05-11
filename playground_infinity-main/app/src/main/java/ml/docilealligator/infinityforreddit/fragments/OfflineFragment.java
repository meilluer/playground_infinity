package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.OfflineSubredditAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.DownloadOfflineSubredditBottomSheetFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentOfflineBinding;
import ml.docilealligator.infinityforreddit.offline.OfflineManager;
import ml.docilealligator.infinityforreddit.offline.OfflineSubreddit;
import ml.docilealligator.infinityforreddit.thing.SortType;
import retrofit2.Retrofit;

public class OfflineFragment extends Fragment {

    private FragmentOfflineBinding binding;
    private OfflineSubredditAdapter adapter;
    
    @Inject
    RedditDataRoomDatabase database;
    
    @Inject
    @Named("oauth")
    Retrofit oauthRetrofit;
    
    @Inject
    @Named("no_oauth")
    Retrofit noOauthRetrofit;
    
    @Inject
    Executor executor;
    
    @Inject
    @Named("current_account")
    SharedPreferences currentAccountSharedPreferences;

    private OfflineManager offlineManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((Infinity) context.getApplicationContext()).getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOfflineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        offlineManager = new OfflineManager(requireContext(), database, oauthRetrofit, noOauthRetrofit, executor);

        adapter = new OfflineSubredditAdapter(requireContext(), new OfflineSubredditAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(OfflineSubreddit offlineSubreddit) {
                Intent intent = new Intent(requireContext(), ml.docilealligator.infinityforreddit.activities.OfflinePostsActivity.class);
                intent.putExtra(ml.docilealligator.infinityforreddit.activities.OfflinePostsActivity.EXTRA_SUBREDDIT_NAME, offlineSubreddit.getName());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(OfflineSubreddit offlineSubreddit) {
                androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Download")
                        .setMessage("Are you sure you want to delete " + offlineSubreddit.getName() + "?")
                        .setPositiveButton("Delete", (d, which) -> {
                            offlineManager.deleteSubreddit(offlineSubreddit.getName());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                
                android.widget.TextView title = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
                if (title != null) title.setTextColor(android.graphics.Color.WHITE);
                android.widget.TextView message = dialog.findViewById(android.R.id.message);
                if (message != null) message.setTextColor(android.graphics.Color.WHITE);
            }

            @Override
            public void onAddClick() {
                DownloadOfflineSubredditBottomSheetFragment fragment = new DownloadOfflineSubredditBottomSheetFragment();
                fragment.setListener((subredditNames, postLimit, sortType, downloadComments, commentLimit, downloadVideos, downloadImages, downloadText, imageQuality, videoQuality) -> {
                    startDownload(subredditNames, postLimit, sortType, downloadComments, commentLimit, downloadVideos, downloadImages, downloadText, imageQuality, videoQuality);
                });
                fragment.show(getParentFragmentManager(), "DownloadOfflineSubreddit");
            }
        });

        binding.recyclerViewOffline.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewOffline.setAdapter(adapter);

        database.offlineSubredditDao().getAllOfflineSubreddits().observe(getViewLifecycleOwner(), offlineSubreddits -> {
            adapter.submitList(offlineSubreddits);
        });
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void startDownload(java.util.List<String> subredditNames, int postLimit, SortType.Type sortType, boolean downloadComments, int commentLimit, boolean downloadVideos, boolean downloadImages, boolean downloadText, String imageQuality, String videoQuality) {
        if (subredditNames == null || subredditNames.isEmpty()) return;

        String accessToken = "TODO_ACCESS_TOKEN"; // You need to get the access token properly
        // In a real implementation, you would use AccountViewModel or similar to get the token.
        // For simplicity here, I will assume the user has logged in and I can get it from Activity or SharedPreferences if stored there, 
        // but typically tokens are managed in memory or secure storage.
        // Let's assume we can get it from the Activity if it exposes it, or we rely on the injected Retrofit if it has interceptors.
        // BUT OfflineManager needs the raw token for manual calls if needed, OR we just trust the Retrofit instance.
        // OfflineManager takes `accessToken` as a string. Let's fix that or pass "" if Retrofit handles it.
        // However, `getSubredditBestPostsOauth` in OfflineManager uses `APIUtils.getOAuthHeader(accessToken)`.
        
        // Actually, MainActivity has `accessToken`. I can try to access it if I cast getActivity().
        String token = "";
        if (getActivity() instanceof ml.docilealligator.infinityforreddit.activities.MainActivity) {
             token = ((ml.docilealligator.infinityforreddit.activities.MainActivity) getActivity()).getAccessToken();
        }
        final String finalToken = token;

        MaterialAlertDialogBuilder progressDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Downloading...")
                .setMessage("Starting...")
                .setCancelable(false);
        
        androidx.appcompat.app.AlertDialog dialog = progressDialog.create();
        dialog.show();

        downloadNextSubreddit(subredditNames, 0, postLimit, sortType, finalToken, downloadComments, commentLimit, downloadVideos, downloadImages, downloadText, imageQuality, videoQuality, dialog);
    }

    private void downloadNextSubreddit(java.util.List<String> subredditNames, int index, int postLimit, SortType.Type sortType, String token, boolean downloadComments, int commentLimit, boolean downloadVideos, boolean downloadImages, boolean downloadText, String imageQuality, String videoQuality, androidx.appcompat.app.AlertDialog dialog) {
        if (index >= subredditNames.size()) {
            if (getActivity() != null) {
                dialog.dismiss();
                Toast.makeText(requireContext(), "All downloads complete", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String subredditName = subredditNames.get(index);
        if (getActivity() != null) {
            dialog.setTitle("Downloading " + subredditName + " (" + (index + 1) + "/" + subredditNames.size() + ")");
        }

        offlineManager.downloadSubreddit(subredditName, postLimit, sortType, token, downloadComments, commentLimit, downloadVideos, downloadImages, downloadText, imageQuality, videoQuality, new OfflineManager.DownloadCallback() {
            @Override
            public void onProgress(int progress, int total, String status) {
                if (getActivity() != null) {
                    dialog.setMessage(status + "\n" + progress + "/" + total);
                }
            }

            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    downloadNextSubreddit(subredditNames, index + 1, postLimit, sortType, token, downloadComments, commentLimit, downloadVideos, downloadImages, downloadText, imageQuality, videoQuality, dialog);
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    Toast.makeText(requireContext(), "Failed to download " + subredditName + ": " + error, Toast.LENGTH_LONG).show();
                    // Continue with next
                    downloadNextSubreddit(subredditNames, index + 1, postLimit, sortType, token, downloadComments, commentLimit, downloadVideos, downloadImages, downloadText, imageQuality, videoQuality, dialog);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
