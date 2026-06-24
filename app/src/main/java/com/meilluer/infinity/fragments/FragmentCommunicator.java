package com.meilluer.infinity.fragments;

import com.meilluer.infinity.postfilter.PostFilter;

public interface FragmentCommunicator {
    default void refresh() {
    }

    default void changeNSFW(boolean nsfw) {
    }

    default void stopRefreshProgressbar() {
    }

    void applyTheme();

    default void hideReadPosts() {
    }

    default void changePostFilter(PostFilter postFilter) {
    }

    default PostFilter getPostFilter() {
        return null;
    }

    default void filterPosts() {

    }
}
