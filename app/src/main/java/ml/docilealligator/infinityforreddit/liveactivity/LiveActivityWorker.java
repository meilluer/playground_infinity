package ml.docilealligator.infinityforreddit.liveactivity;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LiveActivityWorker extends Worker {
    public static final String UNIQUE_WORKER_NAME = "LiveActivityWorker";

    @Inject
    @Named("oauth_without_authenticator")
    Retrofit mOauthWithoutAuthenticatorRetrofit;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    private final Context context;

    public LiveActivityWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        ((Infinity) context.getApplicationContext()).getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!mSharedPreferences.getBoolean("enable_live_activity", false)) {
            LiveActivityNotificationManager.cancelNotification(context);
            return Result.success();
        }

        List<FollowedThing> followedThings = mRedditDataRoomDatabase.followedThingDao().getAllFollowedThings();
        if (followedThings.isEmpty()) {
            LiveActivityNotificationManager.cancelNotification(context);
            return Result.success();
        }

        // For now, we only support one live activity at a time (the most recently updated one)
        // Or we could show a summary. But the user said "on status bar show upvotes and total comment count", 
        // which implies a single item or a very compact summary.
        // Let's take the first one for simplicity, or we can improve this later.
        FollowedThing thing = followedThings.get(0);

        try {
            Account account = mRedditDataRoomDatabase.accountDao().getAccountData(thing.getAccountName());
            boolean isAnonymous = account == null || account.getAccountName().equals(Account.ANONYMOUS_ACCOUNT);
            RedditAPI redditAPI;
            if (!isAnonymous) {
                redditAPI = mOauthWithoutAuthenticatorRetrofit.create(RedditAPI.class);
            } else {
                redditAPI = mRetrofit.create(RedditAPI.class);
            }

            Response<String> response;
            if (!isAnonymous) {
                response = redditAPI.getInfoOauth(thing.getFullName(), APIUtils.getOAuthHeader(account.getAccessToken())).execute();
            } else {
                response = redditAPI.getInfo(thing.getFullName()).execute();
            }

            if (response.isSuccessful() && response.body() != null) {
                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                if (children.length() > 0) {
                    JSONObject data = children.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                    thing.setScore(data.getInt("score"));
                    if (thing.getType() == FollowedThing.TYPE_POST) {
                        thing.setCommentCount(data.getInt("num_comments"));
                    }
                    thing.setLastUpdated(System.currentTimeMillis());
                    mRedditDataRoomDatabase.followedThingDao().update(thing);

                    String topContent = null;
                    if (thing.getType() == FollowedThing.TYPE_POST) {
                        // Fetch latest comment for post
                        Response<String> commentResponse;
                        if (!isAnonymous) {
                            commentResponse = redditAPI.getPostOauth(thing.getId(), APIUtils.getOAuthHeader(account.getAccessToken())).execute();
                        } else {
                            commentResponse = redditAPI.getPost(thing.getId()).execute();
                        }
                        if (commentResponse.isSuccessful() && commentResponse.body() != null) {
                            JSONArray commentArray = new JSONArray(commentResponse.body());
                            JSONArray comments = commentArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                            if (comments.length() > 0) {
                                JSONObject latestComment = comments.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                                if (latestComment.has("body")) {
                                    topContent = latestComment.getString("body");
                                }
                            }
                        }
                    } else if (thing.getType() == FollowedThing.TYPE_COMMENT) {
                        // Fetch latest reply for comment
                        Response<String> commentResponse;
                        String postId = thing.getLinkId();
                        if (postId.startsWith("t3_")) {
                            postId = postId.substring(3);
                        }
                        if (!isAnonymous) {
                            commentResponse = redditAPI.getPostOauth(postId, APIUtils.getOAuthHeader(account.getAccessToken())).execute();
                        } else {
                            commentResponse = redditAPI.getPost(postId).execute();
                        }
                        if (commentResponse.isSuccessful() && commentResponse.body() != null) {
                            JSONArray commentArray = new JSONArray(commentResponse.body());
                            JSONArray comments = commentArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                            JSONObject targetComment = findCommentById(comments, thing.getFullName());
                            if (targetComment != null && targetComment.has("replies") && !targetComment.isNull("replies")) {
                                JSONObject repliesData = targetComment.getJSONObject("replies").getJSONObject(JSONUtils.DATA_KEY);
                                JSONArray replies = repliesData.getJSONArray(JSONUtils.CHILDREN_KEY);
                                if (replies.length() > 0) {
                                    JSONObject latestReply = replies.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                                    if (latestReply.has("body")) {
                                        topContent = latestReply.getString("body");
                                    }
                                }
                            }
                        }
                    }

                    LiveActivityNotificationManager.updateNotification(context, thing, topContent);
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return Result.retry();
        }

        return Result.success();
    }

    private JSONObject findCommentById(JSONArray comments, String fullName) throws JSONException {
        for (int i = 0; i < comments.length(); i++) {
            JSONObject comment = comments.getJSONObject(i);
            if (!comment.getString(JSONUtils.KIND_KEY).equals("t1")) {
                continue;
            }
            JSONObject data = comment.getJSONObject(JSONUtils.DATA_KEY);
            if (data.getString(JSONUtils.NAME_KEY).equals(fullName)) {
                return data;
            }
            if (data.has("replies") && !data.isNull("replies")) {
                JSONArray replies = data.getJSONObject("replies").getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                JSONObject found = findCommentById(replies, fullName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
