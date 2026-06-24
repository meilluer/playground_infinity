package com.meilluer.infinity.customviews;

public interface SwipeLockInterface {
    void lockSwipe();
    void unlockSwipe();
    default void setSwipeLocked(boolean swipeLocked) {}
}
