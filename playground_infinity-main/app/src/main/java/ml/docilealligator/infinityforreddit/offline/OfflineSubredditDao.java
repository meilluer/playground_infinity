package ml.docilealligator.infinityforreddit.offline;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OfflineSubredditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OfflineSubreddit offlineSubreddit);

    @Delete
    void delete(OfflineSubreddit offlineSubreddit);

    @Query("SELECT * FROM offline_subreddits ORDER BY downloadTime DESC")
    LiveData<List<OfflineSubreddit>> getAllOfflineSubreddits();
    
    @Query("SELECT * FROM offline_subreddits WHERE name = :name")
    OfflineSubreddit getOfflineSubreddit(String name);

    @Query("DELETE FROM offline_subreddits WHERE name = :name")
    void deleteSubreddit(String name);
}
