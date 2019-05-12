package org.policerewired.recorder.db.dao;

import org.policerewired.recorder.db.entity.Rule;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RuleDao {

  @Query("SELECT * FROM rule")
  LiveData<List<Rule>> getAll_live();

  @Query("SELECT * FROM rule")
  List<Rule> getAll_static();

  @Query("SELECT * FROM rule WHERE `match` LIKE :number")
  List<Rule> getMatchingRules_static(String number);

  @Update
  void update(Rule... rules);

  @Delete
  void delete(Rule... rules);

  @Insert
  void insert(Rule... rules);

}
