package ml.docilealligator.infinityforreddit.offline;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.post.ParsePost;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

public class OfflineManager {
    private final RedditDataRoomDatabase database;
    private final Retrofit oauthRetrofit;
    private final Retrofit noOauthRetrofit;
    private final OkHttpClient okHttpClient;
    private final Executor executor;
    private final File filesDir;
    private final Gson gson;
    private final Handler mainHandler;

    public OfflineManager(Context context, RedditDataRoomDatabase database, Retrofit oauthRetrofit, Retrofit noOauthRetrofit, Executor executor) {
        this.database = database;
        this.oauthRetrofit = oauthRetrofit;
        this.noOauthRetrofit = noOauthRetrofit;
        this.executor = executor;
        this.filesDir = context.getExternalFilesDir(null);
        this.okHttpClient = new OkHttpClient();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface DownloadCallback {
        void onProgress(int progress, int total, String status);
        void onSuccess();
        void onFailure(String error);
    }

    public void downloadSubreddit(String subredditName, int postLimit, SortType.Type sortType, String accessToken, boolean downloadComments, int commentLimit, boolean downloadVideos, boolean downloadImages, boolean downloadText, String imageQuality, String videoQuality, DownloadCallback callback) {
        executor.execute(() -> {
            try {
                notifyProgress(callback, 0, postLimit, "Fetching posts listing...");

                Map<String, String> headers = null;
                if (accessToken != null && !accessToken.isEmpty()) {
                    headers = APIUtils.getOAuthHeader(accessToken);
                }
                
                // If accessToken is empty, we might use a different retrofit instance or just try with empty headers
                // Assuming oauthRetrofit can handle empty headers or we use a different call.
                // But RedditAPI methods usually require headers for OAuth endpoints.
                // If the user is logged out (anonymous), they should have a token for "anonymous" (application-only oauth).
                // Let's assume headers is fine if not null, otherwise we might fail.
                
                Call<String> call;
                if (headers != null && !headers.isEmpty()) {
                    call = oauthRetrofit.create(RedditAPI.class).getSubredditBestPostsOauth(subredditName, sortType, SortType.Time.ALL, null, headers);
                } else {
                    call = noOauthRetrofit.create(RedditAPI.class).getSubredditBestPosts(subredditName, sortType, SortType.Time.ALL, null);
                }
                
                retrofit2.Response<String> response = call.execute();
                if (!response.isSuccessful() || response.body() == null) {
                    notifyFailure(callback, "Failed to fetch posts: " + response.code() + " " + response.message());
                    return;
                }

                String listingJson = response.body();
                // Create a default PostFilter
                PostFilter postFilter = new PostFilter(); 
                LinkedHashSet<Post> posts = ParsePost.parsePostsSync(listingJson, postLimit, postFilter, null);

                if (posts == null || posts.isEmpty()) {
                    notifyFailure(callback, "No posts found");
                    return;
                }

                // Create/Update OfflineSubreddit
                OfflineSubreddit offlineSubreddit = new OfflineSubreddit(subredditName, postLimit, sortType.name(), System.currentTimeMillis(), 0);
                database.offlineSubredditDao().insert(offlineSubreddit);

                int progress = 0;
                long totalSize = 0;

                for (Post post : posts) {
                    boolean shouldDownload = false;
                    if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                        if (downloadImages) shouldDownload = true;
                    } else if (post.getPostType() == Post.VIDEO_TYPE) {
                        if (downloadVideos) shouldDownload = true;
                    } else if (post.getPostType() == Post.TEXT_TYPE || post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                        if (downloadText) shouldDownload = true;
                    } else if (post.getPostType() == Post.GALLERY_TYPE) {
                        if (downloadImages) shouldDownload = true;
                    }

                    if (!shouldDownload) {
                        progress++;
                        continue;
                    }

                    notifyProgress(callback, progress, posts.size(), "Downloading " + post.getTitle());

                    String mediaPath = null;
                    if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.VIDEO_TYPE || post.getPostType() == Post.GIF_TYPE) {
                        String url = null;
                        if (post.getPostType() == Post.VIDEO_TYPE) {
                            // Prioritize MP4 over DASH/HLS
                            if (post.getVideoFallBackDirectUrl() != null && !post.getVideoFallBackDirectUrl().isEmpty() && !post.getVideoFallBackDirectUrl().contains(".mpd") && !post.getVideoFallBackDirectUrl().contains(".m3u8")) {
                                url = post.getVideoFallBackDirectUrl();
                            } else if (post.getVideoDownloadUrl() != null && !post.getVideoDownloadUrl().isEmpty() && !post.getVideoDownloadUrl().contains(".mpd") && !post.getVideoDownloadUrl().contains(".m3u8")) {
                                url = post.getVideoDownloadUrl();
                            } else if (post.getVideoUrl() != null && !post.getVideoUrl().contains(".mpd") && !post.getVideoUrl().contains(".m3u8")) {
                                url = post.getVideoUrl();
                            }
                        } else {
                            if ("Low".equals(imageQuality) && post.getPreviews() != null && !post.getPreviews().isEmpty()) {
                                url = post.getPreviews().get(0).getPreviewUrl();
                            } else {
                                url = post.getUrl();
                            }
                        }

                        if (url != null && !url.isEmpty()) {
                            // Fix XML encoded amp;
                            url = url.replace("&amp;", "&");
                            try {
                                File mediaFile = downloadFile(url, subredditName, post.getId());
                                if (mediaFile != null) {
                                    mediaPath = mediaFile.getAbsolutePath();
                                    totalSize += mediaFile.length();
                                }
                            } catch (IOException e) {
                                Log.e("OfflineManager", "Failed to download media for " + post.getId() + " url: " + url, e);
                            }
                        }
                    }
                    
                    // Update post with local path (optional, or handle in adapter)
                    if (mediaPath != null) {
                        // We might want to set this in the serialized JSON
                        // But Post object doesn't have mediaPath field easily accessible/settable for this purpose without modifying Post.
                        // We'll store it in the OfflinePost entity.
                    }

                    String postJson = gson.toJson(post);
                    OfflinePost offlinePost = new OfflinePost(post.getId(), subredditName, postJson, mediaPath);
                    database.offlinePostDao().insert(offlinePost);

                    // Fetch Comments
                    if (downloadComments) {
                        try {
                            Integer limit = commentLimit > 0 ? commentLimit : null;
                            Call<String> commentsCall;
                            if (headers != null && !headers.isEmpty()) {
                                commentsCall = oauthRetrofit.create(RedditAPI.class).getPostAndCommentsByIdOauth(post.getId(), SortType.Type.CONFIDENCE, limit, headers);
                            } else {
                                commentsCall = noOauthRetrofit.create(RedditAPI.class).getPostAndCommentsById(post.getId(), SortType.Type.CONFIDENCE, limit);
                            }
                            retrofit2.Response<String> commentsResponse = commentsCall.execute();
                            if (commentsResponse.isSuccessful() && commentsResponse.body() != null) {
                                String commentsJson = commentsResponse.body();
                                OfflineComment offlineComment = new OfflineComment(post.getId(), commentsJson);
                                database.offlineCommentDao().insert(offlineComment);
                                totalSize += commentsJson.length();
                            } else {
                                Log.e("OfflineManager", "Failed to download comments for " + post.getId() + ". Code: " + commentsResponse.code() + ", Message: " + commentsResponse.message());
                            }
                        } catch (Exception e) {
                            Log.e("OfflineManager", "Failed to download comments for " + post.getId(), e);
                        }
                    }

                    progress++;
                }
                
                // Update total size
                OfflineSubreddit updatedSubreddit = new OfflineSubreddit(subredditName, postLimit, sortType.name(), System.currentTimeMillis(), totalSize);
                database.offlineSubredditDao().insert(updatedSubreddit);

                notifySuccess(callback);

            } catch (Exception e) {
                e.printStackTrace();
                notifyFailure(callback, e.getMessage());
            }
        });
    }
    
    public void deleteSubreddit(String subredditName) {
        executor.execute(() -> {
            database.offlineSubredditDao().deleteSubreddit(subredditName);
            database.offlinePostDao().deletePostsBySubreddit(subredditName);
            // Comments cascade delete
            
            // Delete files
            File dir = new File(filesDir, "offline/" + subredditName);
            if (dir.exists()) {
                deleteRecursive(dir);
            }
        });
    }
    
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private File downloadFile(String url, String subreddit, String postId) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return null;

            File dir = new File(filesDir, "offline/" + subreddit);
            if (!dir.exists()) dir.mkdirs();

            // Guess extension
            String cleanUrl = url;
            if (cleanUrl.contains("?")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.indexOf("?"));
            }
            
            String extension = "";
            if (cleanUrl.contains(".")) {
                String potentialExt = cleanUrl.substring(cleanUrl.lastIndexOf("."));
                if (potentialExt.length() <= 5 && potentialExt.matches("\\.[a-zA-Z0-9]+")) {
                    extension = potentialExt;
                }
            }
            
            File file = new File(dir, postId + extension);
            try (InputStream is = response.body().byteStream();
                 FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }
            return file;
        }
    }

    private void notifyProgress(DownloadCallback callback, int progress, int total, String status) {
        mainHandler.post(() -> callback.onProgress(progress, total, status));
    }
    
    private void notifyFailure(DownloadCallback callback, String error) {
        mainHandler.post(() -> callback.onFailure(error));
    }
    
    private void notifySuccess(DownloadCallback callback) {
        mainHandler.post(callback::onSuccess);
    }
}
