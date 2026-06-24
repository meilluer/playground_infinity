package com.meilluer.infinity.postfilter;

import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;

public class DeletePostFilter {
    public static void deletePostFilter(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor, PostFilter postFilter) {
        executor.execute(() -> redditDataRoomDatabase.postFilterDao().deletePostFilter(postFilter));
    }
}
