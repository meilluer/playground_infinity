package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.GeminiSummarizer;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.PostFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.activities.ReportActivity;
import ml.docilealligator.infinityforreddit.activities.SubmitCrosspostActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostList;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostDetailFragment;
import ml.docilealligator.infinityforreddit.post.HidePost;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.services.DownloadMediaService;
import ml.docilealligator.infinityforreddit.services.DownloadRedditVideoService;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostOptionsBottomSheetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private static final String EXTRA_POST = "EP";
    private static final String EXTRA_POST_LIST_POSITION = "EPLP";
    private static final String EXTRA_GALLERY_INDEX = "EGI";

    private BaseActivity mBaseActivity;
    private Post mPost;
    private FragmentPostOptionsBottomSheetBinding binding;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    public PostOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param post Post
     * @return A new instance of fragment PostOptionsBottomSheetFragment.
     */
    public static PostOptionsBottomSheetFragment newInstance(Post post, int postListPosition, int galleryIndex) {
        PostOptionsBottomSheetFragment fragment = new PostOptionsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_POST, post);
        args.putInt(EXTRA_POST_LIST_POSITION, postListPosition);
        args.putInt(EXTRA_GALLERY_INDEX, galleryIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public static PostOptionsBottomSheetFragment newInstance(Post post, int postListPosition) {
        PostOptionsBottomSheetFragment fragment = new PostOptionsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_POST, post);
        args.putInt(EXTRA_POST_LIST_POSITION, postListPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPost = getArguments().getParcelable(EXTRA_POST);
        } else {
            dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((Infinity) mBaseActivity.getApplication()).getAppComponent().inject(this);
        // Inflate the layout for this fragment
        binding = FragmentPostOptionsBottomSheetBinding.inflate(inflater, container, false);

        if (mPost != null) {
            switch (mPost.getPostType()) {
                case Post.IMAGE_TYPE:
                case Post.GALLERY_TYPE:
                    binding.downloadTextViewPostOptionsBottomSheetFragment.setVisibility(View.VISIBLE);
                    binding.downloadTextViewPostOptionsBottomSheetFragment.setText(R.string.download_image);
                    break;
                case Post.GIF_TYPE:
                    binding.downloadTextViewPostOptionsBottomSheetFragment.setVisibility(View.VISIBLE);
                    binding.downloadTextViewPostOptionsBottomSheetFragment.setText(R.string.download_gif);
                    break;
                case Post.VIDEO_TYPE:
                    binding.downloadTextViewPostOptionsBottomSheetFragment.setVisibility(View.VISIBLE);
                    binding.downloadTextViewPostOptionsBottomSheetFragment.setText(R.string.download_video);
                    break;
            }

            if (binding.downloadTextViewPostOptionsBottomSheetFragment.getVisibility() == View.VISIBLE) {
                binding.downloadTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    Toast.makeText(mBaseActivity, R.string.download_started, Toast.LENGTH_SHORT).show();
                    if (mPost.getPostType() == Post.VIDEO_TYPE) {
                        if (!mPost.isRedgifs() && !mPost.isStreamable() && !mPost.isImgur()) {
                            PersistableBundle extras = new PersistableBundle();
                            extras.putString(DownloadRedditVideoService.EXTRA_VIDEO_URL, mPost.getVideoDownloadUrl());
                            extras.putString(DownloadRedditVideoService.EXTRA_POST_ID, mPost.getId());
                            extras.putString(DownloadRedditVideoService.EXTRA_SUBREDDIT, mPost.getSubredditName());
                            extras.putInt(DownloadRedditVideoService.EXTRA_IS_NSFW, mPost.isNSFW() ? 1 : 0);

                            //TODO: contentEstimatedBytes
                            JobInfo jobInfo = DownloadRedditVideoService.constructJobInfo(mBaseActivity, 5000000, extras);
                            ((JobScheduler) mBaseActivity.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);

                            dismiss();
                            return;
                        }
                    }

                    JobInfo jobInfo = DownloadMediaService.constructJobInfo(mBaseActivity, 5000000, mPost, getArguments().getInt(EXTRA_GALLERY_INDEX, 0));
                    ((JobScheduler) mBaseActivity.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);

                    dismiss();
                });
            }

            if (mPost.getPostType() == Post.GALLERY_TYPE) {
                binding.downloadAllTextViewPostOptionsBottomSheetFragment.setVisibility(View.VISIBLE);
                binding.downloadAllTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    JobInfo jobInfo = DownloadMediaService.constructGalleryDownloadAllMediaJobInfo(mBaseActivity, 5000000, mPost);
                    ((JobScheduler) mBaseActivity.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(jobInfo);

                    dismiss();
                });
            }

            binding.shareTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                Bundle bundle = new Bundle();
                bundle.putString(ShareBottomSheetFragment.EXTRA_POST_LINK, mPost.getPermalink());
                if (mPost.getPostType() != Post.TEXT_TYPE) {
                    bundle.putInt(ShareBottomSheetFragment.EXTRA_MEDIA_TYPE, mPost.getPostType());
                    switch (mPost.getPostType()) {
                        case Post.IMAGE_TYPE:
                        case Post.GIF_TYPE:
                        case Post.LINK_TYPE:
                        case Post.NO_PREVIEW_LINK_TYPE:
                            bundle.putString(ShareBottomSheetFragment.EXTRA_MEDIA_LINK, mPost.getUrl());
                            break;
                        case Post.VIDEO_TYPE:
                            bundle.putString(ShareBottomSheetFragment.EXTRA_MEDIA_LINK, mPost.getVideoDownloadUrl());
                            break;
                    }
                }
                bundle.putParcelable(ShareBottomSheetFragment.EXTRA_POST, mPost);
                ShareBottomSheetFragment shareBottomSheetFragment = new ShareBottomSheetFragment();
                shareBottomSheetFragment.setArguments(bundle);
                Fragment parentFragment = getParentFragment();
                if (parentFragment != null) {
                    shareBottomSheetFragment.show(parentFragment.getChildFragmentManager(), shareBottomSheetFragment.getTag());
                } else {
                    shareBottomSheetFragment.show(mBaseActivity.getSupportFragmentManager(), shareBottomSheetFragment.getTag());
                }

                dismiss();
            });

            binding.addToPostFilterTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                Intent intent = new Intent(mBaseActivity, PostFilterPreferenceActivity.class);
                intent.putExtra(PostFilterPreferenceActivity.EXTRA_POST, mPost);
                startActivity(intent);

                dismiss();
            });

            binding.translateTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mBaseActivity);
                String apiKey = sharedPreferences.getString(SharedPreferencesUtils.GEMINI_API_KEY, "");
                if (TextUtils.isEmpty(apiKey)) {
                    Toast.makeText(mBaseActivity, "Gemini API Key is missing", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mBaseActivity, "Translating...", Toast.LENGTH_SHORT).show();
                    GeminiSummarizer.translateTitleAndBodyWithGemini(apiKey, mPost.getTitle(), mPost.getSelfText(), new GeminiSummarizer.GeminiCallback() {
                        @Override
                        public void onSuccess(String result) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                // Parse the result assuming "TITLE: [Title]\nBODY: [Body]"
                                String translatedTitle = mPost.getTitle();
                                String translatedBody = mPost.getSelfText();

                                String[] lines = result.split("\n");
                                StringBuilder bodyBuilder = new StringBuilder();
                                boolean isBody = false;

                                for (String line : lines) {
                                    if (line.startsWith("TITLE:")) {
                                        translatedTitle = line.substring(6).trim();
                                    } else if (line.startsWith("BODY:")) {
                                        isBody = true;
                                        String bodyStart = line.substring(5).trim();
                                        if (!bodyStart.isEmpty()) {
                                            bodyBuilder.append(bodyStart).append("\n");
                                        }
                                    } else if (isBody) {
                                        bodyBuilder.append(line).append("\n");
                                    }
                                }
                                
                                if (bodyBuilder.length() > 0) {
                                    translatedBody = bodyBuilder.toString().trim();
                                } else if (!isBody && lines.length > 0 && !result.contains("TITLE:")) {
                                     // Fallback if format is not respected, assume it's just the body or just the title?
                                     // If it doesn't contain TITLE:, maybe the whole thing is the body?
                                     // Or maybe it just failed to format. Let's leave it safe.
                                }

                                mPost.setTitle(translatedTitle);
                                if (mPost.getSelfText() != null && !mPost.getSelfText().isEmpty()) {
                                     mPost.setSelfText(translatedBody);
                                }

                                EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, getArguments().getInt(EXTRA_POST_LIST_POSITION, 0)));
                                EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(mPost));
                                Toast.makeText(mBaseActivity, "Translation complete", Toast.LENGTH_SHORT).show();
                                dismiss();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(mBaseActivity, "Translation failed: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            });

            if (mBaseActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                binding.commentTextViewPostOptionsBottomSheetFragment.setVisibility(View.GONE);
                binding.hidePostTextViewPostOptionsBottomSheetFragment.setVisibility(View.GONE);
                binding.crosspostTextViewPostOptionsBottomSheetFragment.setVisibility(View.GONE);
                binding.reportTextViewPostOptionsBottomSheetFragment.setVisibility(View.GONE);
            } else {
                binding.commentTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    Intent intent = new Intent(mBaseActivity, CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TITLE_KEY, mPost.getTitle());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, mPost.getSelfText());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, mPost.getSelfTextPlain());
                    intent.putExtra(CommentActivity.EXTRA_SUBREDDIT_NAME_KEY, mPost.getSubredditName());
                    intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
                    mBaseActivity.startActivity(intent);

                    dismiss();
                });

                if (mPost.isHidden()) {
                    binding.hidePostTextViewPostOptionsBottomSheetFragment.setText(R.string.action_unhide_post);
                } else {
                    binding.hidePostTextViewPostOptionsBottomSheetFragment.setText(R.string.action_hide_post);
                }

                binding.hidePostTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    if (mPost.isHidden()) {
                        HidePost.unhidePost(mOauthRetrofit, mBaseActivity.accessToken, mPost.getFullName(), new HidePost.HidePostListener() {
                            @Override
                            public void success() {
                                mPost.setHidden(false);
                                Toast.makeText(mBaseActivity, R.string.post_unhide_success, Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, getArguments().getInt(EXTRA_POST_LIST_POSITION, 0)));
                                dismiss();
                            }

                            @Override
                            public void failed() {
                                mPost.setHidden(true);
                                Toast.makeText(mBaseActivity, R.string.post_unhide_failed, Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, getArguments().getInt(EXTRA_POST_LIST_POSITION, 0)));
                                dismiss();
                            }
                        });
                    } else {
                        HidePost.hidePost(mOauthRetrofit, mBaseActivity.accessToken, mPost.getFullName(), new HidePost.HidePostListener() {
                            @Override
                            public void success() {
                                mPost.setHidden(true);
                                Toast.makeText(mBaseActivity, R.string.post_hide_success, Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, getArguments().getInt(EXTRA_POST_LIST_POSITION, 0)));
                                dismiss();
                            }

                            @Override
                            public void failed() {
                                mPost.setHidden(false);
                                Toast.makeText(mBaseActivity, R.string.post_hide_failed, Toast.LENGTH_SHORT).show();
                                EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, getArguments().getInt(EXTRA_POST_LIST_POSITION, 0)));
                                dismiss();
                            }
                        });
                    }
                });

                binding.crosspostTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    Intent submitCrosspostIntent = new Intent(mBaseActivity, SubmitCrosspostActivity.class);
                    submitCrosspostIntent.putExtra(SubmitCrosspostActivity.EXTRA_POST, mPost);
                    startActivity(submitCrosspostIntent);

                    dismiss();
                });

                binding.reportTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                    Intent intent = new Intent(mBaseActivity, ReportActivity.class);
                    intent.putExtra(ReportActivity.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
                    intent.putExtra(ReportActivity.EXTRA_THING_FULLNAME, mPost.getFullName());
                    startActivity(intent);

                    dismiss();
                });

                if (mPost.isCanModPost()) {
                    binding.modTextViewPostOptionsBottomSheetFragment.setVisibility(View.VISIBLE);
                    binding.modTextViewPostOptionsBottomSheetFragment.setOnClickListener(view -> {
                        ModerationActionBottomSheetFragment moderationActionBottomSheetFragment = ModerationActionBottomSheetFragment.newInstance(mPost, getArguments().getInt(EXTRA_POST_LIST_POSITION, 0));
                        Fragment parentFragment = getParentFragment();
                        if (parentFragment != null) {
                            moderationActionBottomSheetFragment.show(parentFragment.getChildFragmentManager(), moderationActionBottomSheetFragment.getTag());
                        } else {
                            moderationActionBottomSheetFragment.show(mBaseActivity.getSupportFragmentManager(), moderationActionBottomSheetFragment.getTag());
                        }
                        dismiss();
                    });
                }
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mBaseActivity = (BaseActivity) context;
    }
}