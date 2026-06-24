package com.meilluer.infinity.postfilter;

import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;

public class SavePostFilterUsage {
    public static void savePostFilterUsage(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                           PostFilterUsage postFilterUsage) {
        executor.execute(() -> redditDataRoomDatabase.postFilterUsageDao().insert(postFilterUsage));
    }
}
