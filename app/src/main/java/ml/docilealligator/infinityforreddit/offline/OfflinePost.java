package ml.docilealligator.infinityforreddit.offline;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_posts")
public class OfflinePost {
    @PrimaryKey
    @NonNull
    private String id;
    private String subredditName;
    private String postJson;
    private String mediaPath; // Local path to downloaded media

    public OfflinePost(@NonNull String id, String subredditName, String postJson, String mediaPath) {
        this.id = id;
        this.subredditName = subredditName;
        this.postJson = postJson;
        this.mediaPath = mediaPath;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getSubredditName() {
        return subredditName;
    }

    public String getPostJson() {
        return postJson;
    }

    public String getMediaPath() {
        return mediaPath;
    }
}
