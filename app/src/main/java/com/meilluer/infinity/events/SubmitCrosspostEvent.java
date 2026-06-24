package com.meilluer.infinity.events;

import com.meilluer.infinity.post.Post;

public class SubmitCrosspostEvent {
    public boolean postSuccess;
    public Post post;
    public String errorMessage;

    public SubmitCrosspostEvent(boolean postSuccess, Post post, String errorMessage) {
        this.postSuccess = postSuccess;
        this.post = post;
        this.errorMessage = errorMessage;
    }
}
