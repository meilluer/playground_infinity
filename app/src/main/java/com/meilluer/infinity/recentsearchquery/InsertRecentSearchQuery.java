package com.meilluer.infinity.recentsearchquery;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;

import java.util.List;
import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.account.AccountDao;
import com.meilluer.infinity.multireddit.MultiReddit;

public class InsertRecentSearchQuery {
    public interface InsertRecentSearchQueryListener {
        void success();
    }

    public static void insertRecentSearchQueryListener(Executor executor, Handler handler,
                                                       RedditDataRoomDatabase redditDataRoomDatabase,
                                                       String username,
                                                       String recentSearchQuery,
                                                       String searchInSubredditOrUserName,
                                                       MultiReddit searchInMultiReddit,
                                                       int searchInThingType,
                                                       InsertRecentSearchQueryListener insertRecentSearchQueryListener) {
        executor.execute(() -> {
            try {
                RecentSearchQueryDao recentSearchQueryDao = redditDataRoomDatabase.recentSearchQueryDao();
                List<RecentSearchQuery> recentSearchQueries = recentSearchQueryDao.getAllRecentSearchQueries(username);
                if (recentSearchQueries.size() >= 5) {
                    for (int i = 4; i < recentSearchQueries.size(); i++) {
                        recentSearchQueryDao.deleteRecentSearchQueries(recentSearchQueries.get(i));
                    }
                }

                if (username != null && !username.isEmpty()) {
                    if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
                        AccountDao accountDao = redditDataRoomDatabase.accountDao();
                        if (!accountDao.isAnonymousAccountInserted()) {
                            accountDao.insert(Account.getAnonymousAccount());
                        }
                    } else {
                        if (redditDataRoomDatabase.accountDao().getAccountData(username) == null) {
                            handler.post(insertRecentSearchQueryListener::success);
                            return;
                        }
                    }

                    if (searchInMultiReddit == null) {
                        recentSearchQueryDao.insert(new RecentSearchQuery(username, recentSearchQuery,
                                searchInSubredditOrUserName, null, null, searchInThingType));
                    } else {
                        recentSearchQueryDao.insert(new RecentSearchQuery(username, recentSearchQuery,
                                searchInSubredditOrUserName, searchInMultiReddit.getPath(),
                                searchInMultiReddit.getDisplayName(), searchInThingType));
                    }
                }

                handler.post(insertRecentSearchQueryListener::success);
            } catch (SQLiteConstraintException e) {
                e.printStackTrace();
            }
        });
    }
}
