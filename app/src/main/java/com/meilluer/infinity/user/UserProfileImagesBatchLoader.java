package com.meilluer.infinity.user;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.apis.RedditAPI;
import com.meilluer.infinity.comment.Comment;
import com.meilluer.infinity.utils.APIUtils;
import com.meilluer.infinity.utils.JSONUtils;
import com.meilluer.infinity.viewmodels.ViewPostDetailActivityViewModel;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserProfileImagesBatchLoader {
    public static final int BATCH_SIZE = 100;

    private final Executor mExecutor;
    private final Handler mHandler;
    private final RedditDataRoomDatabase mRedditDataRoomDatabase;
    private final Retrofit mRetrofit;
    private final Retrofit mOauthRetrofit;
    private final Map<String, String> mAuthorFullNameToImageMap;
    private final Queue<Comment> mCommentQueue;
    private final Map<String, ViewPostDetailActivityViewModel.LoadIconListener> mAuthorFullNameToListenerMap;
    private final List<Comment> mCallingComments;
    private final Set<String> mLoadingAuthorFullNames;
    private boolean mIsLoadingBatch = false;

    public UserProfileImagesBatchLoader(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                           Retrofit retrofit, Retrofit oauthRetrofit) {
        mExecutor = executor;
        mHandler = handler;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mRetrofit = retrofit;
        mOauthRetrofit = oauthRetrofit;
        mAuthorFullNameToImageMap = new HashMap<>();
        mCommentQueue = new LinkedList<>();
        mAuthorFullNameToListenerMap = new HashMap<>();
        mCallingComments = new LinkedList<>();
        mLoadingAuthorFullNames = new HashSet<>();
    }

    public void loadAuthorImages(@Nullable String accessToken, List<Comment> comments, @NonNull ViewPostDetailActivityViewModel.LoadIconListener loadIconListener) {
        String authorFullName = comments.get(0).getAuthorFullName();
        if (mAuthorFullNameToImageMap.containsKey(authorFullName)) {
            loadIconListener.loadIconSuccess(authorFullName, mAuthorFullNameToImageMap.get(authorFullName));
            return;
        }

        mAuthorFullNameToListenerMap.put(authorFullName, loadIconListener);
        mCommentQueue.addAll(comments);
        mCallingComments.add(comments.get(0));

        if (!mIsLoadingBatch) {
            loadNextBatch(accessToken);
        }
    }

    private void loadNextBatch(String accessToken) {
        if (mCommentQueue.isEmpty()) {
            return;
        }

        mIsLoadingBatch = true;

        mExecutor.execute(() -> {
            if (!mCallingComments.isEmpty()) {
                int index = 0;
                while (mCallingComments.size() > index) {
                    Comment c = mCallingComments.get(index);
                    ViewPostDetailActivityViewModel.LoadIconListener loadIconListener = mAuthorFullNameToListenerMap.get(c.getAuthorFullName());
                    if (loadIconListener != null) {
                        if (mAuthorFullNameToImageMap.containsKey(c.getAuthorFullName())) {
                            loadIconListener.loadIconSuccess(c.getAuthorFullName(), mAuthorFullNameToImageMap.get(c.getAuthorFullName()));
                            mCallingComments.remove(index);
                        } else {
                            UserDao userDao = mRedditDataRoomDatabase.userDao();
                            UserData userData = userDao.getUserData(c.getAuthor());
                            if (userData != null) {
                                String iconImageUrl = userData.getIconUrl();
                                String authorFullName = c.getAuthorFullName();
                                mAuthorFullNameToImageMap.put(authorFullName, iconImageUrl == null ? "" : iconImageUrl);
                                mHandler.post(() -> loadIconListener.loadIconSuccess(authorFullName, iconImageUrl));
                                mAuthorFullNameToListenerMap.remove(authorFullName);
                                mCallingComments.remove(index);
                            } else {
                                index++;
                            }
                        }
                    } else {
                        mCallingComments.remove(index);
                    }
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < BATCH_SIZE && !mCommentQueue.isEmpty(); i++) {
                Comment comment = mCommentQueue.poll();
                if (comment == null || comment.getAuthorFullName() == null || comment.getAuthorFullName().isEmpty()) {
                    i--;
                    continue;
                }
                String authorFullName = comment.getAuthorFullName();
                if (!mAuthorFullNameToImageMap.containsKey(authorFullName)) {
                    stringBuilder.append(authorFullName).append(",");
                    mLoadingAuthorFullNames.add(authorFullName);
                } else if (i == 0) {
                    ViewPostDetailActivityViewModel.LoadIconListener loadIconListener = mAuthorFullNameToListenerMap.get(authorFullName);
                    if (loadIconListener != null) {
                        mHandler.post(() -> {
                            loadIconListener.loadIconSuccess(authorFullName, mAuthorFullNameToImageMap.get(authorFullName));
                        });
                        mAuthorFullNameToListenerMap.remove(authorFullName);
                    }
                    for (int j = 0; j < BATCH_SIZE - 1 && !mCommentQueue.isEmpty(); j++) {
                        mCommentQueue.poll();
                    }
                    break;
                }
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                try {
                    Response<String> response;
                    if (accessToken != null) {
                        response = mOauthRetrofit.create(RedditAPI.class).loadPartialUserDataOauth(
                                stringBuilder.toString(), APIUtils.getOAuthHeader(accessToken)).execute();
                    } else {
                        response = mRetrofit.create(RedditAPI.class).loadPartialUserData(stringBuilder.toString()).execute();
                    }
                    if (response.isSuccessful()) {
                        parseUserProfileImages(response.body());
                        callListenerAndLoadNextBatch(accessToken, true);
                    } else {
                        callListenerAndLoadNextBatch(accessToken, false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    callListenerAndLoadNextBatch(accessToken, false);
                }
            } else {
                mIsLoadingBatch = false;
                loadNextBatch(accessToken);
            }
        });
    }

    @WorkerThread
    private void parseUserProfileImages(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            for (String s : mLoadingAuthorFullNames) {
                try {
                    mAuthorFullNameToImageMap.put(s, jsonResponse.getJSONObject(s).getString(JSONUtils.PROFILE_IMG_KEY).replaceAll("&amp;","&"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callListenerAndLoadNextBatch(String accessToken, boolean loadSuccessful) {
        for (String s : mLoadingAuthorFullNames) {
            ViewPostDetailActivityViewModel.LoadIconListener loadIconListener = mAuthorFullNameToListenerMap.get(s);
            if (loadIconListener != null) {
                mHandler.post(() -> {
                    loadIconListener.loadIconSuccess(s, mAuthorFullNameToImageMap.get(s));
                });
                mAuthorFullNameToListenerMap.remove(s);
            }
            if (!loadSuccessful && !mAuthorFullNameToImageMap.containsKey(s)) {
                mAuthorFullNameToImageMap.put(s, "");
            }
        }

        mLoadingAuthorFullNames.clear();
        mIsLoadingBatch = false;
        loadNextBatch(accessToken);
    }
}
