package com.meilluer.infinity.asynctasks;

import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.subscribeduser.SubscribedUserData;

public class CheckIsFollowingUser {
    public static void checkIsFollowingUser(Executor executor, Handler handler,
                                            RedditDataRoomDatabase redditDataRoomDatabase, String username,
                                            @NonNull String accountName, CheckIsFollowingUserListener checkIsFollowingUserListener) {
        executor.execute(() -> {
            SubscribedUserData subscribedUserData = redditDataRoomDatabase.subscribedUserDao().getSubscribedUser(username, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : accountName);
            handler.post(() -> {
                if (subscribedUserData != null) {
                    checkIsFollowingUserListener.isSubscribed();
                } else {
                    checkIsFollowingUserListener.isNotSubscribed();
                }
            });
        });
    }

    public interface CheckIsFollowingUserListener {
        void isSubscribed();

        void isNotSubscribed();
    }
}
