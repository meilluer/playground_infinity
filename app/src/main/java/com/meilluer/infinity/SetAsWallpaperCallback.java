package com.meilluer.infinity;

public interface SetAsWallpaperCallback {
    void setToHomeScreen(int viewPagerPosition);
    void setToLockScreen(int viewPagerPosition);
    void setToBoth(int viewPagerPosition);
}