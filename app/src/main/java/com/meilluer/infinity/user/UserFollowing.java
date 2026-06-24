package com.meilluer.infinity.user;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.apis.RedditAPI;
import com.meilluer.infinity.subscribeduser.SubscribedUserDao;
import com.meilluer.infinity.subscribeduser.SubscribedUserData;
import com.meilluer.infinity.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class UserFollowing {
    public static void followUser(Executor executor, Handler handler, Retrofit oauthRetrofit, Retrofit retrofit,
                                  @Nullable String accessToken, String username, @NonNull String accountName,
                                  RedditDataRoomDatabase redditDataRoomDatabase,
                                  UserFollowingListener userFollowingListener) {
        userFollowing(executor, handler, oauthRetrofit, retrofit, accessToken, username, accountName, "sub",
                redditDataRoomDatabase.subscribedUserDao(), userFollowingListener);
    }

    public static void anonymousFollowUser(Executor executor, Handler handler, Retrofit retrofit, String username,
                                           RedditDataRoomDatabase redditDataRoomDatabase,
                                           UserFollowingListener userFollowingListener) {
        FetchUserData.fetchUserData(executor, handler, retrofit, username, new FetchUserData.FetchUserDataListener() {
            @Override
            public void onFetchUserDataSuccess(UserData userData, int inboxCount) {
                executor.execute(() -> {
                    if (!redditDataRoomDatabase.accountDao().isAnonymousAccountInserted()) {
                        redditDataRoomDatabase.accountDao().insert(Account.getAnonymousAccount());
                    }
                    redditDataRoomDatabase.subscribedUserDao().insert(new SubscribedUserData(userData.getName(), userData.getIconUrl(),
                            Account.ANONYMOUS_ACCOUNT, false));

                    handler.post(userFollowingListener::onUserFollowingSuccess);
                });
            }

            @Override
            public void onFetchUserDataFailed() {
                userFollowingListener.onUserFollowingFail();
            }
        });
    }

    public static void unfollowUser(Executor executor, Handler handler, Retrofit oauthRetrofit, Retrofit retrofit,
                                    @Nullable String accessToken, String username, @NonNull String accountName,
                                    RedditDataRoomDatabase redditDataRoomDatabase,
                                    UserFollowingListener userFollowingListener) {
        userFollowing(executor, handler, oauthRetrofit, retrofit, accessToken, username, accountName, "unsub",
                redditDataRoomDatabase.subscribedUserDao(), userFollowingListener);
    }

    public static void anonymousUnfollowUser(Executor executor, Handler handler, String username,
                                             RedditDataRoomDatabase redditDataRoomDatabase,
                                             UserFollowingListener userFollowingListener) {
        executor.execute(() -> {
            redditDataRoomDatabase.subscribedUserDao().deleteSubscribedUser(username, Account.ANONYMOUS_ACCOUNT);

            handler.post(userFollowingListener::onUserFollowingSuccess);
        });
    }

    private static void userFollowing(Executor executor, Handler handler, Retrofit oauthRetrofit, Retrofit retrofit, @Nullable String accessToken,
                                      String username, @NonNull String accountName, String action, SubscribedUserDao subscribedUserDao,
                                      UserFollowingListener userFollowingListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ACTION_KEY, action);
        params.put(APIUtils.SR_NAME_KEY, "u_" + username);

        Call<String> subredditSubscriptionCall = api.subredditSubscription(APIUtils.getOAuthHeader(accessToken), params);
        subredditSubscriptionCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    if (action.equals("sub")) {
                        FetchUserData.fetchUserData(executor, handler, null, oauthRetrofit, retrofit, accessToken,
                                username, new FetchUserData.FetchUserDataListener() {
                                    @Override
                                    public void onFetchUserDataSuccess(UserData userData, int inboxCount) {
                                        executor.execute(() -> {
                                            SubscribedUserData subscribedUserData = new SubscribedUserData(userData.getName(), userData.getIconUrl(),
                                                    accountName, false);
                                            subscribedUserDao.insert(subscribedUserData);
                                        });
                                    }

                                    @Override
                                    public void onFetchUserDataFailed() {

                                    }
                                });
                        userFollowingListener.onUserFollowingSuccess();
                    } else {
                        executor.execute(() -> {
                            subscribedUserDao.deleteSubscribedUser(username, accountName);
                            handler.post(userFollowingListener::onUserFollowingSuccess);
                        });
                    }
                } else {
                    userFollowingListener.onUserFollowingFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                userFollowingListener.onUserFollowingFail();
            }
        });
    }

    public interface UserFollowingListener {
        void onUserFollowingSuccess();

        void onUserFollowingFail();
    }
}

