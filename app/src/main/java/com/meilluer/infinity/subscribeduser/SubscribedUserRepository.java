package com.meilluer.infinity.subscribeduser;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.meilluer.infinity.RedditDataRoomDatabase;

public class SubscribedUserRepository {
    private final SubscribedUserDao mSubscribedUserDao;
    private final String mAccountName;

    SubscribedUserRepository(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        mSubscribedUserDao = redditDataRoomDatabase.subscribedUserDao();
        mAccountName = accountName;
    }

    LiveData<List<SubscribedUserData>> getAllSubscribedUsersWithSearchQuery(String searchQuery) {
        return mSubscribedUserDao.getAllSubscribedUsersWithSearchQuery(mAccountName, searchQuery);
    }

    LiveData<List<SubscribedUserData>> getAllFavoriteSubscribedUsersWithSearchQuery(String searchQuery) {
        return mSubscribedUserDao.getAllFavoriteSubscribedUsersWithSearchQuery(mAccountName, searchQuery);
    }
}
