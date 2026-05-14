package ml.docilealligator.infinityforreddit.liveactivity;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class LiveActivityUtils {
    public static void scheduleWorker(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = sharedPreferences.getBoolean("enable_live_activity", false);
        if (enabled) {
            String intervalStr = sharedPreferences.getString("live_activity_refresh_interval", "15");
            int interval = Integer.parseInt(intervalStr);
            
            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(LiveActivityWorker.class, interval, TimeUnit.MINUTES)
                    .build();
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    LiveActivityWorker.UNIQUE_WORKER_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest);
        } else {
            cancelWorker(context);
        }
    }

    public static void cancelWorker(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(LiveActivityWorker.UNIQUE_WORKER_NAME);
        LiveActivityNotificationManager.cancelNotification(context);
    }

    public static void triggerImmediateUpdate(Context context) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(LiveActivityWorker.class));
    }
}
