package hr.cizmic.seebanking.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import hr.cizmic.seebanking.data.local.entity.UserEntity;

@Dao
public interface UserDao {

    // insert or update user in db
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserEntity user);

    // get user by id (updates automatically when data changes
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<UserEntity> observeById(long id);

    // clear all users from db
    @Query("DELETE FROM users")
    void deleteAll();
}
