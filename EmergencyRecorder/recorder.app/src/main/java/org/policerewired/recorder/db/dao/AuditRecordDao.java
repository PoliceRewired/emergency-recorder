package org.policerewired.recorder.db.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.policerewired.recorder.constants.AuditRecordType;
import org.policerewired.recorder.db.entity.AuditRecord;

import java.util.Date;
import java.util.List;

@Dao
public interface AuditRecordDao {

  @Query("SELECT * FROM audit ORDER BY started")
  LiveData<List<AuditRecord>> getUnlimitedAll_live();

  @Query("SELECT * FROM audit WHERE type != :exclusion ORDER BY started")
  LiveData<List<AuditRecord>> getNearlyAll_live(AuditRecordType exclusion);

  @Query("SELECT * FROM audit WHERE type in (:inclusion) ORDER BY started DESC LIMIT :max")
  LiveData<List<AuditRecord>> getSpecifically_live_limited(AuditRecordType[] inclusion, int max);

  @Query("SELECT * FROM audit WHERE started >= :since ORDER BY started")
  LiveData<List<AuditRecord>> getAllAfter_live(Date since);

  @Query("SELECT * FROM audit WHERE started >= :since AND started <= :until ORDER BY started")
  LiveData<List<AuditRecord>> getAllBetween_live(Date since, Date until);

  @Query("SELECT * FROM audit ORDER BY started")
  List<AuditRecord> getAll_static();

  @Query("SELECT * FROM audit WHERE started >= :since AND started <= :until ORDER BY started")
  List<AuditRecord> getAllBetween_static(Date since, Date until);

  @Query("SELECT * FROM audit ORDER BY started LIMIT 1")
  AuditRecord getEarliest();

  @Query("SELECT * FROM audit ORDER BY started DESC LIMIT 1")
  AuditRecord getLatest();

  @Query("SELECT COUNT(*) FROM audit")
  long count();

  @Query("SELECT COUNT(*) FROM audit WHERE started >= :since AND started <= :until")
  long countBetween(Date since, Date until);

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
