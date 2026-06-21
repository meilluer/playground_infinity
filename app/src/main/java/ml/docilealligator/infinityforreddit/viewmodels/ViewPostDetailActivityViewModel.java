package ml.docilealligator.infinityforreddit.viewmodels;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.user.UserProfileImagesBatchLoader;
import retrofit2.Retrofit;

public class ViewPostDetailActivityViewModel extends ViewModel {
    private UserProfileImagesBatchLoader mLoader;
    private String mAccessToken;

    public ViewPostDetailActivityViewModel(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                           Retrofit retrofit, Retrofit oauthRetrofit, @Nullable String accessToken) {
        mLoader = new UserProfileImagesBatchLoader(executor, handler, redditDataRoomDatabase, retrofit, oauthRetrofit);
        mAccessToken = accessToken;
    }

    public void loadAuthorImages(List<Comment> comments, @NonNull LoadIconListener loadIconListener) {
        mLoader.loadAuthorImages(mAccessToken, comments, loadIconListener);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Executor mExecutor;
        private Handler mHandler;
        private RedditDataRoomDatabase mRedditDataRoomDatabase;
        private Retrofit mRetrofit;
        private Retrofit mOauthRetrofit;
        private String mAccessToken;

        public Factory(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                       Retrofit retrofit, Retrofit oauthRetrofit, @Nullable String accessToken) {
            this.mExecutor = executor;
            this.mHandler = handler;
            this.mRedditDataRoomDatabase = redditDataRoomDatabase;
            this.mRetrofit = retrofit;
            this.mOauthRetrofit = oauthRetrofit;
            this.mAccessToken = accessToken;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ViewPostDetailActivityViewModel(mExecutor, mHandler, mRedditDataRoomDatabase,
                    mRetrofit, mOauthRetrofit, mAccessToken);
        }
    }

    public interface LoadIconListener {
        void loadIconSuccess(String authorFullName, String iconUrl);
    }
}
