package ml.docilealligator.infinityforreddit.post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ml.docilealligator.infinityforreddit.offline.OfflinePost;
import ml.docilealligator.infinityforreddit.offline.OfflinePostDao;

public class OfflinePostPagingSource extends ListenableFuturePagingSource<Integer, Post> {

    private final OfflinePostDao offlinePostDao;
    private final String subredditName;
    private final Gson gson;
    private final Executor executor;

    public OfflinePostPagingSource(OfflinePostDao offlinePostDao, String subredditName, Executor executor) {
        this.offlinePostDao = offlinePostDao;
        this.subredditName = subredditName;
        this.executor = executor;
        this.gson = new Gson();
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Post> pagingState) {
        return null;
    }

    @NonNull
    @Override
    public ListenableFuture<LoadResult<Integer, Post>> loadFuture(@NonNull LoadParams<Integer> loadParams) {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        return service.submit(new Callable<LoadResult<Integer, Post>>() {
            @Override
            public LoadResult<Integer, Post> call() throws Exception {
                try {
                    android.util.Log.d("OfflinePostPS", "Loading posts for: " + subredditName);
                    List<OfflinePost> offlinePosts = offlinePostDao.getOfflinePostsListBySubreddit(subredditName);
                    android.util.Log.d("OfflinePostPS", "Found " + offlinePosts.size() + " offline posts");
                    List<Post> posts = new ArrayList<>();
                    for (OfflinePost offlinePost : offlinePosts) {
                        Post post = gson.fromJson(offlinePost.getPostJson(), Post.class);
                        if (post != null) {
                            if (offlinePost.getMediaPath() != null) {
                                String path = offlinePost.getMediaPath();
                                if (!path.startsWith("file://")) {
                                    path = "file://" + path;
                                }
                                if (post.getPostType() == Post.VIDEO_TYPE || post.getPostType() == Post.GIF_TYPE) {
                                    post.setVideoUrl(path);
                                    post.setVideoDownloadUrl(path);
                                } else if (post.getPostType() == Post.IMAGE_TYPE) {
                                    post.setUrl(path);
                                }
                            }
                            posts.add(post);
                        }
                    }
                    return new LoadResult.Page<>(posts, null, null);
                } catch (Exception e) {
                    return new LoadResult.Error(e);
                }
            }
        });
    }
}
