package org.policerewired.recorder.db.dao;


import org.policerewired.recorder.constants.AuditRecordType;
import org.policerewired.recorder.db.entity.AuditRecord;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AuditRecordDao {

  @Query("SELECT * FROM audit")
  LiveData<List<AuditRecord>> getUnlimitedAll_live();

  @Query("SELECT * FROM audit WHERE type != :exclusion")
  LiveData<List<AuditRecord>> getNearlyAll_live(AuditRecordType exclusion);

  @Query("SELECT * FROM audit WHERE started >= :since")
  LiveData<List<AuditRecord>> getAllAfter_live(Date since);

  @Query("SELECT * FROM audit WHERE started >= :since AND started <= :until")
  LiveData<List<AuditRecord>> getAllBetween_live(Date since, Date until);

  @Query("SELECT * FROM audit")
  List<AuditRecord> getAll_static();

  @Update
  void update(AuditRecord... auditRecords);

  @Delete
  void delete(AuditRecord... auditRecords);

  @Query("DELETE FROM audit WHERE started < :date")
  void deleteBefore(Date date);

  @Query("DELETE FROM audit")
  void deleteAll();

  @Insert
  void insert(AuditRecord... auditRecords);

}
