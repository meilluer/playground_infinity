package ml.docilealligator.infinityforreddit.offline;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "offline_subreddits")
public class OfflineSubreddit {
    @PrimaryKey
    @NonNull
    private String name;
    private int postLimit;
    private String sortType;
    private long downloadTime;
    private long totalSize;

    public OfflineSubreddit(@NonNull String name, int postLimit, String sortType, long downloadTime, long totalSize) {
        this.name = name;
        this.postLimit = postLimit;
        this.sortType = sortType;
        this.downloadTime = downloadTime;
        this.totalSize = totalSize;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getPostLimit() {
        return postLimit;
    }

    public String getSortType() {
        return sortType;
    }

    public long getDownloadTime() {
        return downloadTime;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
