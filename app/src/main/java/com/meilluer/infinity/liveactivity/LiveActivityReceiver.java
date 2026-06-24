package com.meilluer.infinity.liveactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import com.meilluer.infinity.Infinity;
import com.meilluer.infinity.RedditDataRoomDatabase;

public class LiveActivityReceiver extends BroadcastReceiver {
    public static final String ACTION_UNFOLLOW = "com.meilluer.infinity.liveactivity.ACTION_UNFOLLOW";
    public static final String EXTRA_ID = "com.meilluer.infinity.liveactivity.EXTRA_ID";

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    public void onReceive(Context context, Intent intent) {
        ((Infinity) context.getApplicationContext()).getAppComponent().inject(this);

        if (ACTION_UNFOLLOW.equals(intent.getAction())) {
            String id = intent.getStringExtra(EXTRA_ID);
            if (id != null) {
                new Thread(() -> {
                    FollowedThing followedThing = mRedditDataRoomDatabase.followedThingDao().getFollowedThingById(id);
                    if (followedThing != null) {
                        mRedditDataRoomDatabase.followedThingDao().deleteById(id);
                        if (followedThing.getType() == FollowedThing.TYPE_COMMENT || mRedditDataRoomDatabase.followedThingDao().getAllFollowedThings().isEmpty()) {
                            LiveActivityUtils.cancelWorker(context);
                        } else {
                            // Trigger update to show the next item in the notification
                            LiveActivityUtils.triggerImmediateUpdate(context);
                        }
                    }
                }).start();
            }
        }
    }
}
