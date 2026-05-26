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
        boolean removedExpiredThings = false;
        long currentTime = System.currentTimeMillis();
        for (FollowedThing thing : followedThings) {
            if (thing.getExpirationTime() != 0 && thing.getExpirationTime() < currentTime) {
                mRedditDataRoomDatabase.followedThingDao().deleteById(thing.getId());
                removedExpiredThings = true;
            }
        }

        if (removedExpiredThings) {
            followedThings = mRedditDataRoomDatabase.followedThingDao().getAllFollowedThings();
        }

        if (followedThings.isEmpty()) {
            LiveActivityUtils.cancelWorker(context);
            return Result.success();
        }

        FollowedThing headlineThing = null;
        String topContent = null;

        try {
            for (FollowedThing thing : followedThings) {
                Account account = mRedditDataRoomDatabase.accountDao().getAccountData(thing.getAccountName());
                boolean isAnonymous = account == null || account.getAccountName().equals(Account.ANONYMOUS_ACCOUNT);
                RedditAPI redditAPI = isAnonymous
                        ? mRetrofit.create(RedditAPI.class)
                        : mOauthWithoutAuthenticatorRetrofit.create(RedditAPI.class);

                Response<String> response = isAnonymous
                        ? redditAPI.getInfo(thing.getFullName()).execute()
                        : redditAPI.getInfoOauth(thing.getFullName(), APIUtils.getOAuthHeader(account.getAccessToken())).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    continue;
                }

                JSONObject jsonResponse = new JSONObject(response.body());
                JSONArray children = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                if (children.length() <= 0) {
                    continue;
                }

                JSONObject data = children.getJSONObject(0).getJSONObject(JSONUtils.DATA_KEY);
                thing.setScore(data.getInt("score"));
                if (thing.getType() == FollowedThing.TYPE_POST) {
                    thing.setCommentCount(data.getInt("num_comments"));
                }
                thing.setLastUpdated(System.currentTimeMillis());

                String itemTopContent = null;
                if (thing.getType() == FollowedThing.TYPE_POST) {
                    itemTopContent = fetchLatestPostComment(redditAPI, isAnonymous, account, thing.getId());
                } else {
                    CommentReplySnapshot replySnapshot = fetchLatestCommentReply(redditAPI, isAnonymous, account, thing);
                    thing.setCommentCount(replySnapshot.replyCount);
                    itemTopContent = replySnapshot.latestReplyBody;
                }

                mRedditDataRoomDatabase.followedThingDao().update(thing);

                if (headlineThing == null || thing.getLastUpdated() > headlineThing.getLastUpdated()) {
                    headlineThing = thing;
                    topContent = itemTopContent;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            LiveActivityUtils.scheduleWorker(context);
            return Result.success();
        }

        List<FollowedThing> updatedFollowedThings = mRedditDataRoomDatabase.followedThingDao().getAllFollowedThings();
        if (updatedFollowedThings.isEmpty()) {
            LiveActivityUtils.cancelWorker(context);
            return Result.success();
        }

        if (headlineThing == null) {
            headlineThing = updatedFollowedThings.get(0);
        }

        LiveActivityNotificationManager.updateNotification(context, updatedFollowedThings, headlineThing, topContent);
        LiveActivityUtils.scheduleWorker(context);
        return Result.success();
    }

    private String fetchLatestPostComment(RedditAPI redditAPI, boolean isAnonymous, Account account, String postId)
            throws IOException, JSONException {
        Response<String> commentResponse = isAnonymous
                ? redditAPI.getPostWithSort(postId, "new").execute()
                : redditAPI.getPostWithSortOauth(postId, "new", APIUtils.getOAuthHeader(account.getAccessToken())).execute();
        if (!commentResponse.isSuccessful() || commentResponse.body() == null) {
            return null;
        }

        JSONArray commentArray = new JSONArray(commentResponse.body());
        JSONArray comments = commentArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
        
        JSONObject latestComment = null;
        for (int i = 0; i < comments.length(); i++) {
            JSONObject commentObj = comments.getJSONObject(i);
            if (commentObj.optString(JSONUtils.KIND_KEY, "").equals("t1")) {
                latestComment = commentObj.getJSONObject(JSONUtils.DATA_KEY);
                break;
            }
        }
        return latestComment != null && latestComment.has("body") ? latestComment.getString("body") : null;
    }

    private CommentReplySnapshot fetchLatestCommentReply(RedditAPI redditAPI, boolean isAnonymous, Account account,
                                                         FollowedThing thing) throws IOException, JSONException {
        String postId = thing.getLinkId();
        if (postId.startsWith("t3_")) {
            postId = postId.substring(3);
        }

        Response<String> commentResponse = isAnonymous
                ? redditAPI.getPostWithSort(postId, "new").execute()
                : redditAPI.getPostWithSortOauth(postId, "new", APIUtils.getOAuthHeader(account.getAccessToken())).execute();
        if (!commentResponse.isSuccessful() || commentResponse.body() == null) {
            return new CommentReplySnapshot(thing.getCommentCount(), null);
        }

        JSONArray commentArray = new JSONArray(commentResponse.body());
        JSONArray comments = commentArray.getJSONObject(1).getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
        JSONObject targetComment = findCommentById(comments, thing.getFullName());
        if (targetComment == null || !targetComment.has("replies") || targetComment.isNull("replies")) {
            return new CommentReplySnapshot(0, null);
        }

        JSONObject repliesData = targetComment.getJSONObject("replies").getJSONObject(JSONUtils.DATA_KEY);
        JSONArray replies = repliesData.getJSONArray(JSONUtils.CHILDREN_KEY);
        
        JSONObject latestReply = null;
        double maxCreatedUtc = 0;
        int replyCount = 0;
        for (int i = 0; i < replies.length(); i++) {
            JSONObject replyObj = replies.getJSONObject(i);
            if (replyObj.optString(JSONUtils.KIND_KEY, "").equals("t1")) {
                replyCount++;
                JSONObject replyData = replyObj.getJSONObject(JSONUtils.DATA_KEY);
                double createdUtc = replyData.optDouble("created_utc", 0);
                if (latestReply == null || createdUtc > maxCreatedUtc) {
                    latestReply = replyData;
                    maxCreatedUtc = createdUtc;
                }
            }
        }

        String latestReplyBody = latestReply != null && latestReply.has("body") ? latestReply.getString("body") : null;
        return new CommentReplySnapshot(replyCount, latestReplyBody);
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

    private static class CommentReplySnapshot {
        final int replyCount;
        final String latestReplyBody;

        CommentReplySnapshot(int replyCount, String latestReplyBody) {
            this.replyCount = replyCount;
            this.latestReplyBody = latestReplyBody;
        }
    }
}
