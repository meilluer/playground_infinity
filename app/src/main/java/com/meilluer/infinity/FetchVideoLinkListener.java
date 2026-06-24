package com.meilluer.infinity;

import androidx.annotation.Nullable;

import com.meilluer.infinity.post.Post;
import com.meilluer.infinity.thing.StreamableVideo;

public interface FetchVideoLinkListener {
    default void onFetchRedditVideoLinkSuccess(Post post, String fileName) {}
    default void onFetchImgurVideoLinkSuccess(String videoUrl, String videoDownloadUrl, String fileName) {}
    default void onFetchRedgifsVideoLinkSuccess(String webm, String mp4) {}
    default void onFetchStreamableVideoLinkSuccess(StreamableVideo streamableVideo) {}
    default void onChangeFileName(String fileName) {}
    default void onFetchVideoFallbackDirectUrlSuccess(String videoFallbackDirectUrl) {}
    default void failed(@Nullable Integer messageRes) {}
}
