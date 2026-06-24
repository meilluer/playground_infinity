package com.meilluer.infinity.commentfilter;

import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;

public class SaveCommentFilterUsage {
    public static void saveCommentFilterUsage(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                           CommentFilterUsage commentFilterUsage) {
        executor.execute(() -> redditDataRoomDatabase.commentFilterUsageDao().insert(commentFilterUsage));
    }
}
