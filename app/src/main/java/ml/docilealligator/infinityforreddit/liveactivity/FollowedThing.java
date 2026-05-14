package ml.docilealligator.infinityforreddit.liveactivity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "followed_things")
public class FollowedThing {
    public static final int TYPE_POST = 0;
    public static final int TYPE_COMMENT = 1;

    @PrimaryKey
    @NonNull
    private String id;
    private String fullName;
    private int type;
    private String title; // Title for posts, content for comments
    private String subreddit;
    private String linkId; // Parent post ID for comments
    private int score;
    private int commentCount; // nComments for posts, childCount for comments
    private String accountName;
    private long lastUpdated;

    public FollowedThing(@NonNull String id, String fullName, int type, String title, String subreddit, 
                         String linkId, int score, int commentCount, String accountName, long lastUpdated) {
        this.id = id;
        this.fullName = fullName;
        this.type = type;
        this.title = title;
        this.subreddit = subreddit;
        this.linkId = linkId;
        this.score = score;
        this.commentCount = commentCount;
        this.accountName = accountName;
        this.lastUpdated = lastUpdated;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getLinkId() {
        return linkId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getAccountName() {
        return accountName;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
