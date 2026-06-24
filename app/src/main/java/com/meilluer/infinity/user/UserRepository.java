package com.meilluer.infinity.user;

import androidx.lifecycle.LiveData;

import com.meilluer.infinity.RedditDataRoomDatabase;

public class UserRepository {
    private final LiveData<UserData> mUserLiveData;

    UserRepository(RedditDataRoomDatabase redditDataRoomDatabase, String userName) {
        mUserLiveData = redditDataRoomDatabase.userDao().getUserLiveData(userName);
    }

    LiveData<UserData> getUserLiveData() {
        return mUserLiveData;
    }
}
