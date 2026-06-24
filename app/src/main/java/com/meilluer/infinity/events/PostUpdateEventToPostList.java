package com.meilluer.infinity.events;

import com.meilluer.infinity.post.Post;

public class PostUpdateEventToPostList {
    public final Post post;
    public final int positionInList;

    public PostUpdateEventToPostList(Post post, int positionInList) {
        this.post = post;
        this.positionInList = positionInList;
    }
}
