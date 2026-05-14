package ml.docilealligator.infinityforreddit.liveactivity;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FollowedThingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FollowedThing followedThing);

    @Update
    void update(FollowedThing followedThing);

    @Delete
    void delete(FollowedThing followedThing);

    @Query("SELECT * FROM followed_things")
    List<FollowedThing> getAllFollowedThings();

    @Query("SELECT * FROM followed_things WHERE id = :id")
    FollowedThing getFollowedThingById(String id);

    @Query("DELETE FROM followed_things WHERE id = :id")
    void deleteById(String id);
}
