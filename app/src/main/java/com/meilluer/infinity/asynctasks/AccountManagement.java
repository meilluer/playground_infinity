package com.meilluer.infinity.asynctasks;

import android.content.SharedPreferences;
import android.os.Handler;

import java.util.concurrent.Executor;

import com.meilluer.infinity.RedditDataRoomDatabase;
import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.account.AccountDao;
import com.meilluer.infinity.utils.SharedPreferencesUtils;

public class AccountManagement {

    public static void switchAccount(RedditDataRoomDatabase redditDataRoomDatabase,
                                     SharedPreferences currentAccountSharedPreferences, Executor executor,
                                     Handler handler, String newAccountName,
                                     SwitchAccountListener switchAccountListener) {
        executor.execute(() -> {
            redditDataRoomDatabase.accountDao().markAllAccountsNonCurrent();
            redditDataRoomDatabase.accountDao().markAccountCurrent(newAccountName);
            Account account = redditDataRoomDatabase.accountDao().getCurrentAccount();
            currentAccountSharedPreferences.edit()
                    .putString(SharedPreferencesUtils.ACCESS_TOKEN, account.getAccessToken())
                    .putString(SharedPreferencesUtils.ACCOUNT_NAME, account.getAccountName())
                    .putString(SharedPreferencesUtils.ACCOUNT_IMAGE_URL, account.getProfileImageUrl()).apply();
            currentAccountSharedPreferences.edit().remove(SharedPreferencesUtils.SUBSCRIBED_THINGS_SYNC_TIME).apply();
            handler.post(() -> switchAccountListener.switched(account));
        });

    }

    public static void switchToAnonymousMode(RedditDataRoomDatabase redditDataRoomDatabase, SharedPreferences currentAccountSharedPreferences,
                                             Executor executor, Handler handler, boolean removeCurrentAccount,
                                             SwitchToAnonymousAccountAsyncTaskListener switchToAnonymousAccountAsyncTaskListener) {
        executor.execute(() -> {
            AccountDao accountDao = redditDataRoomDatabase.accountDao();
            if (removeCurrentAccount) {
                accountDao.deleteCurrentAccount();
            }
            accountDao.markAllAccountsNonCurrent();

            String redgifsAccessToken = currentAccountSharedPreferences.getString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, "");

            currentAccountSharedPreferences.edit().clear().apply();
            currentAccountSharedPreferences.edit().putString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, redgifsAccessToken).apply();

            handler.post(switchToAnonymousAccountAsyncTaskListener::logoutSuccess);
        });
    }

    public static void removeAccount(RedditDataRoomDatabase redditDataRoomDatabase,
                                             Executor executor, String accoutName) {
        executor.execute(() -> {
            redditDataRoomDatabase.accountDao().deleteAccount(accoutName);
        });
    }

    public interface SwitchToAnonymousAccountAsyncTaskListener {
        void logoutSuccess();
    }

    public interface SwitchAccountListener {
        void switched(Account account);
    }
}
