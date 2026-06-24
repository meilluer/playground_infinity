package com.meilluer.infinity.events;

public class NeedForPostListFromPostFragmentEvent {
    public long postFragmentTimeId;

    public NeedForPostListFromPostFragmentEvent(long postFragmentId) {
        this.postFragmentTimeId = postFragmentId;
    }
}
