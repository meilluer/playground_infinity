package com.meilluer.infinity.readpost;

import android.database.sqlite.SQLiteConstraintException;

import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.account.AccountDao;

public class InsertReadPost {
    public static void insertReadPost(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      String username, String postId, int readPostsLimit) {
        executor.execute(() -> {
            try {
                ReadPostDao readPostDao = redditDataRoomDatabase.readPostDao();
                int limit = Math.max(readPostsLimit, 100);
                boolean isReadPostLimit = readPostsLimit != -1;
                while (readPostDao.getReadPostsCount(username) > limit && isReadPostLimit) {
                    readPostDao.deleteOldestReadPosts(username);
                }
                if (username != null && !username.isEmpty()) {
                    if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
                        AccountDao accountDao = redditDataRoomDatabase.accountDao();
                        if (!accountDao.isAnonymousAccountInserted()) {
                            accountDao.insert(Account.getAnonymousAccount());
                        }
                    }
                    readPostDao.insert(new ReadPost(username, postId));
                }
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        });
    }
}
