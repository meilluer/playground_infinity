package com.meilluer.infinity.subreddit;

import androidx.lifecycle.LiveData;

import com.meilluer.infinity.RedditDataRoomDatabase;

public class SubredditRepository {
    private final SubredditDao mSubredditDao;
    private final LiveData<SubredditData> mSubredditLiveData;

    SubredditRepository(RedditDataRoomDatabase redditDataRoomDatabase,
                        String subredditName) {
        mSubredditDao = redditDataRoomDatabase.subredditDao();
        mSubredditLiveData = mSubredditDao.getSubredditLiveDataByName(subredditName);
    }

    LiveData<SubredditData> getSubredditLiveData() {
        return mSubredditLiveData;
    }
}