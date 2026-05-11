package ml.docilealligator.infinityforreddit.readpost;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import ml.docilealligator.infinityforreddit.account.Account;

@Entity(tableName = "read_comments", primaryKeys = {"username", "post_id"},
        foreignKeys = @ForeignKey(entity = Account.class, parentColumns = "username",
                childColumns = "username", onDelete = ForeignKey.CASCADE))
public class ReadComment {
    @NonNull
    @ColumnInfo(name = "username")
    private String username;

    @NonNull
    @ColumnInfo(name = "post_id")
    private String postId;

    @ColumnInfo(name = "last_read_comment_index")
    private int lastReadCommentIndex;

    @ColumnInfo(name = "time")
    private long time;

    public ReadComment(@NonNull String username, @NonNull String postId, int lastReadCommentIndex) {
        this.username = username;
        this.postId = postId;
        this.lastReadCommentIndex = lastReadCommentIndex;
        this.time = System.currentTimeMillis();
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPostId() {
        return postId;
    }

    public void setPostId(@NonNull String postId) {
        this.postId = postId;
    }

    public int getLastReadCommentIndex() {
        return lastReadCommentIndex;
    }

    public void setLastReadCommentIndex(int lastReadCommentIndex) {
        this.lastReadCommentIndex = lastReadCommentIndex;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
