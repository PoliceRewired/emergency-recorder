package org.policerewired.recorder.db.converter;

import org.policerewired.recorder.constants.Behaviour;
import org.policerewired.recorder.constants.RecordType;

import androidx.room.TypeConverter;

public class RecordTypeConverter {

  @TypeConverter
  public static RecordType toRecordType(String value) {
    return value == null ? null : RecordType.valueOf(value);
  }

  @TypeConverter
  public static String toString(RecordType value) {
    return value == null ? null : value.name();
  }

}
