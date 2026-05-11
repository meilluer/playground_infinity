package ml.docilealligator.infinityforreddit.offline;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OfflineCommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OfflineComment offlineComment);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<OfflineComment> offlineComments);

    @Query("SELECT * FROM offline_comments WHERE parentPostId = :postId")
    List<OfflineComment> getOfflineCommentsByPostId(String postId);
    
    @Query("DELETE FROM offline_comments WHERE parentPostId = :postId")
    void deleteCommentsByPostId(String postId);
}
