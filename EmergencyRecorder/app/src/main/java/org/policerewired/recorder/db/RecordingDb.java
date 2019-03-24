package org.policerewired.recorder.db;

import org.policerewired.recorder.db.converter.BehaviourConverter;
import org.policerewired.recorder.db.converter.DateConverter;
import org.policerewired.recorder.db.converter.RecordTypeConverter;
import org.policerewired.recorder.db.converter.UUIDConverter;
import org.policerewired.recorder.db.converter.UriConverter;
import org.policerewired.recorder.db.dao.RecordingDao;
import org.policerewired.recorder.db.dao.RuleDao;
import org.policerewired.recorder.db.entity.Recording;
import org.policerewired.recorder.db.entity.Rule;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Rule.class, Recording.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, BehaviourConverter.class, UriConverter.class, RecordTypeConverter.class, UUIDConverter.class})
public abstract class RecordingDb extends RoomDatabase {

  public abstract RuleDao getRuleDao();
  public abstract RecordingDao getRecordingDao();


}
