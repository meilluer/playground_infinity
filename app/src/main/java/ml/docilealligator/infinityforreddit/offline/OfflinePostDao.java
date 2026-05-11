package ml.docilealligator.infinityforreddit.offline;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OfflinePostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OfflinePost offlinePost);

    @Delete
    void delete(OfflinePost offlinePost);

    @Query("SELECT * FROM offline_posts WHERE subredditName = :subredditName")
    LiveData<List<OfflinePost>> getOfflinePostsBySubreddit(String subredditName);

    @Query("SELECT * FROM offline_posts WHERE subredditName = :subredditName")
    List<OfflinePost> getOfflinePostsListBySubreddit(String subredditName);

    @Query("DELETE FROM offline_posts WHERE subredditName = :subredditName")
    void deletePostsBySubreddit(String subredditName);
}
