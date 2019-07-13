package org.policerewired.recorder.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.policerewired.recorder.db.converter.BehaviourConverter;
import org.policerewired.recorder.db.converter.DateConverter;
import org.policerewired.recorder.db.converter.RecordTypeConverter;
import org.policerewired.recorder.db.converter.UUIDConverter;
import org.policerewired.recorder.db.converter.UriConverter;
import org.policerewired.recorder.db.dao.AuditRecordDao;
import org.policerewired.recorder.db.dao.RuleDao;
import org.policerewired.recorder.db.entity.AuditRecord;
import org.policerewired.recorder.db.entity.Rule;

/**
 * Main database for the app - hold information about rules (behaviours to taken when outgoing
 * calls are detected) and recordings (the app's log of events).
 */
@Database(entities = {Rule.class, AuditRecord.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, BehaviourConverter.class, UriConverter.class, RecordTypeConverter.class, UUIDConverter.class})
public abstract class RecordingDb extends RoomDatabase {

  public abstract RuleDao getRuleDao();
  public abstract AuditRecordDao getRecordingDao();


}
