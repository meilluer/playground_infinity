package ml.docilealligator.infinityforreddit.liveactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class LiveActivityReceiver extends BroadcastReceiver {
    public static final String ACTION_UNFOLLOW = "ml.docilealligator.infinityforreddit.liveactivity.ACTION_UNFOLLOW";
    public static final String EXTRA_ID = "ml.docilealligator.infinityforreddit.liveactivity.EXTRA_ID";

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    public void onReceive(Context context, Intent intent) {
        ((Infinity) context.getApplicationContext()).getAppComponent().inject(this);

        if (ACTION_UNFOLLOW.equals(intent.getAction())) {
            String id = intent.getStringExtra(EXTRA_ID);
            if (id != null) {
                new Thread(() -> {
                    mRedditDataRoomDatabase.followedThingDao().deleteById(id);
                    if (mRedditDataRoomDatabase.followedThingDao().getAllFollowedThings().isEmpty()) {
                        LiveActivityUtils.cancelWorker(context);
                    } else {
                        // Trigger update to show the next item in the notification
                        LiveActivityUtils.triggerImmediateUpdate(context);
                    }
                }).start();
            }
        }
    }
}
