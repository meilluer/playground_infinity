package ml.docilealligator.infinityforreddit.readpost;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface ReadCommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReadComment readComment);

    @Query("SELECT * FROM read_comments WHERE username = :username AND post_id = :postId")
    ReadComment getReadComment(String username, String postId);

    @Query("DELETE FROM read_comments WHERE username = :username AND post_id = :postId")
    void deleteReadComment(String username, String postId);
}
