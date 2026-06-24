package com.meilluer.infinity.events;

import com.meilluer.infinity.post.Post;

public class PostUpdateEventToPostDetailFragment {
    public final Post post;

    public PostUpdateEventToPostDetailFragment(Post post) {
        this.post = post;
    }
}
