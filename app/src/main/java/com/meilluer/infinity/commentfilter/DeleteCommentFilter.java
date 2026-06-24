package com.meilluer.infinity.commentfilter;

import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;

public class DeleteCommentFilter {
    public static void deleteCommentFilter(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor, CommentFilter commentFilter) {
        executor.execute(() -> redditDataRoomDatabase.commentFilterDao().deleteCommentFilter(commentFilter));
    }
}
