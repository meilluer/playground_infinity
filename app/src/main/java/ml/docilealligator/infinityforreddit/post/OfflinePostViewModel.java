package ml.docilealligator.infinityforreddit.post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class OfflinePostViewModel extends ViewModel {
    private final LiveData<PagingData<Post>> posts;

    public OfflinePostViewModel(RedditDataRoomDatabase database, String subredditName, Executor executor) {
        OfflinePostPagingSource pagingSource = new OfflinePostPagingSource(database.offlinePostDao(), subredditName, executor);
        Pager<Integer, Post> pager = new Pager<>(
                new PagingConfig(100, 4, false, 100),
                () -> pagingSource);
        
        posts = PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
    }

    public LiveData<PagingData<Post>> getPosts() {
        return posts;
    }
}
