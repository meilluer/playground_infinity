package ml.docilealligator.infinityforreddit.liveactivity;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LiveActivityUtils {
    private static OneTimeWorkRequest buildWorkerRequest(long delayMinutes) {
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(LiveActivityWorker.class);
        if (delayMinutes > 0) {
            builder.setInitialDelay(delayMinutes, TimeUnit.MINUTES);
        }
        return builder.build();
    }

    public static void scheduleWorker(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = sharedPreferences.getBoolean("enable_live_activity", false);
        if (!enabled) {
            cancelWorker(context);
            return;
        }

        String intervalStr = sharedPreferences.getString("live_activity_refresh_interval", "15");
        long interval = Long.parseLong(intervalStr);

        WorkManager.getInstance(context).enqueueUniqueWork(
                LiveActivityWorker.UNIQUE_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                buildWorkerRequest(interval));
    }

    public static void cancelWorker(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(LiveActivityWorker.UNIQUE_WORKER_NAME);
        LiveActivityNotificationManager.cancelNotification(context);
    }

    public static void enableLiveActivity(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean("enable_live_activity", true)
                .apply();
    }

    public static void showCachedNotification(Context context, List<FollowedThing> followedThings,
                                              FollowedThing headlineThing) {
        LiveActivityNotificationManager.updateNotification(context, followedThings, headlineThing, null);
    }

    public static void triggerImmediateUpdate(Context context) {
        WorkManager.getInstance(context).enqueueUniqueWork(
                LiveActivityWorker.UNIQUE_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                buildWorkerRequest(0));
    }
}
