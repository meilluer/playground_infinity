package com.meilluer.infinity.recentsearchquery;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.meilluer.infinity.RedditDataRoomDatabase;

public class RecentSearchQueryRepository {
    private final LiveData<List<RecentSearchQuery>> mAllRecentSearchQueries;

    RecentSearchQueryRepository(RedditDataRoomDatabase redditDataRoomDatabase, String username) {
        mAllRecentSearchQueries = redditDataRoomDatabase.recentSearchQueryDao().getAllRecentSearchQueriesLiveData(username);
    }

    LiveData<List<RecentSearchQuery>> getAllRecentSearchQueries() {
        return mAllRecentSearchQueries;
    }
}
