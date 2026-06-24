package com.meilluer.infinity.asynctasks;

import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.subscribedsubreddit.SubscribedSubredditData;

public class CheckIsSubscribedToSubreddit {

    public static void checkIsSubscribedToSubreddit(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                                    String subredditName, @NonNull String accountName,
                                                    CheckIsSubscribedToSubredditListener checkIsSubscribedToSubredditListener) {
        executor.execute(() -> {
            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                if (!redditDataRoomDatabase.accountDao().isAnonymousAccountInserted()) {
                    redditDataRoomDatabase.accountDao().insert(Account.getAnonymousAccount());
                }
            }
            SubscribedSubredditData subscribedSubredditData = redditDataRoomDatabase.subscribedSubredditDao().getSubscribedSubreddit(subredditName, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : accountName);
            handler.post(() -> {
                if (subscribedSubredditData != null) {
                    checkIsSubscribedToSubredditListener.isSubscribed();
                } else {
                    checkIsSubscribedToSubredditListener.isNotSubscribed();
                }
            });
        });
    }

    public interface CheckIsSubscribedToSubredditListener {
        void isSubscribed();

        void isNotSubscribed();
    }
}
