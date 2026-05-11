package ml.docilealligator.infinityforreddit.offline;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_comments",
        foreignKeys = @ForeignKey(entity = OfflinePost.class,
                parentColumns = "id",
                childColumns = "parentPostId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "parentPostId")})
public class OfflineComment {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String parentPostId;
    private String commentJson;

    public OfflineComment(String parentPostId, String commentJson) {
        this.parentPostId = parentPostId;
        this.commentJson = commentJson;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getParentPostId() {
        return parentPostId;
    }

    public String getCommentJson() {
        return commentJson;
    }
}
