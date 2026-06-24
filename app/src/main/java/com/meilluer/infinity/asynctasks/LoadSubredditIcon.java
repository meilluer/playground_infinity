package com.meilluer.infinity.asynctasks;

import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.subreddit.FetchSubredditData;
import com.meilluer.infinity.subreddit.SubredditDao;
import com.meilluer.infinity.subreddit.SubredditData;
import retrofit2.Retrofit;

public class LoadSubredditIcon {

    public static void loadSubredditIcon(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                         String subredditName, String accessToken, @NonNull String accountName,
                                         Retrofit oauthRetrofit, Retrofit retrofit,
                                         LoadSubredditIconListener loadSubredditIconListener) {
        executor.execute(() -> {
            SubredditDao subredditDao = redditDataRoomDatabase.subredditDao();
            SubredditData subredditData = subredditDao.getSubredditData(subredditName);
            if (subredditData != null) {
                String iconImageUrl = subredditDao.getSubredditData(subredditName).getIconUrl();
                handler.post(() -> loadSubredditIconListener.loadIconSuccess(iconImageUrl));
            } else {
                handler.post(() -> FetchSubredditData.fetchSubredditData(executor, handler,
                        accountName.equals(Account.ANONYMOUS_ACCOUNT) ? null : oauthRetrofit, retrofit,
                        subredditName, accessToken, new FetchSubredditData.FetchSubredditDataListener() {
                            @Override
                            public void onFetchSubredditDataSuccess(SubredditData subredditData1, int nCurrentOnlineSubscribers) {
                                ArrayList<SubredditData> singleSubredditDataList = new ArrayList<>();
                                singleSubredditDataList.add(subredditData1);
                                InsertSubscribedThings.insertSubscribedThings(executor, handler, redditDataRoomDatabase, accountName,
                                        null, null, singleSubredditDataList,
                                        () -> loadSubredditIconListener.loadIconSuccess(subredditData1.getIconUrl()));
                            }

                            @Override
                            public void onFetchSubredditDataFail(boolean isQuarantined) {
                                loadSubredditIconListener.loadIconSuccess(null);
                            }
                        }));
            }
        });
    }

    public interface LoadSubredditIconListener {
        void loadIconSuccess(String iconImageUrl);
    }
}
