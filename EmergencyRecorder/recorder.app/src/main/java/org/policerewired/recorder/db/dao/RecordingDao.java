package org.policerewired.recorder.db.dao;


import org.policerewired.recorder.db.entity.Recording;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RecordingDao {

  @Query("SELECT * FROM recording")
  LiveData<List<Recording>> getAll();

  @Update
  void update(Recording... recordings);

  @Delete
  void delete(Recording... recordings);

  @Insert
  void insert(Recording... recordings);

}
