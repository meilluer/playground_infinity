package com.meilluer.infinity.asynctasks;

import android.os.Handler;

import java.util.concurrent.Executor;

import com.meilluer.infinity.account.Account;
import com.meilluer.infinity.account.AccountDao;

public class ParseAndInsertNewAccount {

    public static void parseAndInsertNewAccount(Executor executor, Handler handler, String username,
                                                String accessToken, String refreshToken, String profileImageUrl,
                                                String bannerImageUrl, int karma, boolean isMod, String code, AccountDao accountDao,
                                                ParseAndInsertAccountListener parseAndInsertAccountListener) {
        executor.execute(() -> {
            Account account = new Account(username, accessToken, refreshToken, code, profileImageUrl,
                    bannerImageUrl, karma, true, isMod);
            accountDao.markAllAccountsNonCurrent();
            accountDao.insert(account);

            handler.post(parseAndInsertAccountListener::success);
        });
    }

    public interface ParseAndInsertAccountListener {
        void success();
    }
}
