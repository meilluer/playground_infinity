package ml.docilealligator.infinityforreddit.liveactivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.List;

import ml.docilealligator.infinityforreddit.R;

public class LiveActivityNotificationManager {
    public static final String CHANNEL_ID = "live_activity";
    public static final String CHANNEL_NAME = "Live Activity";
    public static final int NOTIFICATION_ID = 80000;

    public static void updateNotification(Context context, List<FollowedThing> followedThings,
                                          FollowedThing headlineThing, String topContent) {
        if (followedThings == null || followedThings.isEmpty() || headlineThing == null) {
            cancelNotification(context);
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent unfollowIntent = new Intent(context, LiveActivityReceiver.class);
        unfollowIntent.setAction(LiveActivityReceiver.ACTION_UNFOLLOW);
        unfollowIntent.putExtra(LiveActivityReceiver.EXTRA_ID, headlineThing.getId());
        PendingIntent unfollowPendingIntent = PendingIntent.getBroadcast(context, 0, unfollowIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : 0));

        int totalFollowedCount = followedThings.size();
        int totalReplyCount = 0;
        for (FollowedThing followedThing : followedThings) {
            totalReplyCount += followedThing.getCommentCount();
        }

        String compactStats = totalFollowedCount + "P " + totalReplyCount + "R";
        String title = totalFollowedCount == 1 ? headlineThing.getTitle() : totalFollowedCount + " live activities";
        String subtitle = totalFollowedCount == 1 ? "r/" + headlineThing.getSubreddit() : headlineThing.getTitle();
        String contentText = !TextUtils.isEmpty(topContent) ? topContent : compactStats;

        if (Build.VERSION.SDK_INT >= 36) {
            Notification.ProgressStyle style = new Notification.ProgressStyle();
            String chipText = compactStats.length() > 10 ? compactStats.substring(0, 10) : compactStats;

            Bundle extras = new Bundle();
            extras.putBoolean("android.requestPromotedOngoing", true);

            Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_day_night_24dp)
                    .setContentTitle(title)
                    .setSubText(subtitle)
                    .setContentText(contentText)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setStyle(style)
                    .addExtras(extras)
                    .setShortCriticalText(chipText)
                    .addAction(new Notification.Action.Builder(0, context.getString(R.string.unfollow), unfollowPendingIntent).build());

            notificationManager.notify(NOTIFICATION_ID, builder.build());
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_day_night_24dp)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        RemoteViews collapsedView = new RemoteViews(context.getPackageName(), R.layout.notification_live_activity);
        collapsedView.setTextViewText(R.id.title, title);
        collapsedView.setTextViewText(R.id.subreddit, subtitle);
        collapsedView.setTextViewText(R.id.upvotes, "Posts " + totalFollowedCount);
        collapsedView.setTextViewText(R.id.comments, "Replies " + totalReplyCount);
        collapsedView.setViewVisibility(R.id.expanded_content, View.GONE);
        collapsedView.setViewVisibility(R.id.unfollow_button, View.GONE);

        RemoteViews expandedView = new RemoteViews(context.getPackageName(), R.layout.notification_live_activity);
        expandedView.setTextViewText(R.id.title, title);
        expandedView.setTextViewText(R.id.subreddit, subtitle);
        expandedView.setTextViewText(R.id.upvotes, "Posts " + totalFollowedCount);
        expandedView.setTextViewText(R.id.comments, "Replies " + totalReplyCount);
        if (!TextUtils.isEmpty(topContent)) {
            expandedView.setViewVisibility(R.id.expanded_content, View.VISIBLE);
            expandedView.setTextViewText(R.id.expanded_content, topContent);
        } else {
            expandedView.setViewVisibility(R.id.expanded_content, View.GONE);
        }
        expandedView.setViewVisibility(R.id.unfollow_button, View.VISIBLE);
        expandedView.setOnClickPendingIntent(R.id.unfollow_button, unfollowPendingIntent);

        builder.setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
