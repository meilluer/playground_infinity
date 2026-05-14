package ml.docilealligator.infinityforreddit.liveactivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ml.docilealligator.infinityforreddit.R;

public class LiveActivityNotificationManager {
    public static final String CHANNEL_ID = "live_activity";
    public static final String CHANNEL_NAME = "Live Activity";
    public static final int NOTIFICATION_ID = 80000;

    public static void updateNotification(Context context, FollowedThing thing, String topContent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        RemoteViews collapsedView = new RemoteViews(context.getPackageName(), R.layout.notification_live_activity);
        collapsedView.setTextViewText(R.id.title, thing.getTitle());
        collapsedView.setTextViewText(R.id.subreddit, "r/" + thing.getSubreddit());
        collapsedView.setTextViewText(R.id.upvotes, "↑ " + thing.getScore());
        collapsedView.setTextViewText(R.id.comments, "💬 " + thing.getCommentCount());
        collapsedView.setViewVisibility(R.id.expanded_content, View.GONE);

        RemoteViews expandedView = new RemoteViews(context.getPackageName(), R.layout.notification_live_activity);
        expandedView.setTextViewText(R.id.title, thing.getTitle());
        expandedView.setTextViewText(R.id.subreddit, "r/" + thing.getSubreddit());
        expandedView.setTextViewText(R.id.upvotes, "↑ " + thing.getScore());
        expandedView.setTextViewText(R.id.comments, "💬 " + thing.getCommentCount());
        if (topContent != null && !topContent.isEmpty()) {
            expandedView.setViewVisibility(R.id.expanded_content, View.VISIBLE);
            expandedView.setTextViewText(R.id.expanded_content, topContent);
        } else {
            expandedView.setViewVisibility(R.id.expanded_content, View.GONE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true)
                .setOnlyAlertOnce(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
