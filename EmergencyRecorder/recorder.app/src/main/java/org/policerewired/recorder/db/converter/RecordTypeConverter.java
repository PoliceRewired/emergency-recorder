package org.policerewired.recorder.db.converter;

import org.policerewired.recorder.constants.AuditRecordType;

import androidx.room.TypeConverter;

public class RecordTypeConverter {

  @TypeConverter
  public static AuditRecordType toRecordType(String value) {
    return value == null ? null : AuditRecordType.valueOf(value);
  }

  @TypeConverter
  public static String toString(AuditRecordType value) {
    return value == null ? null : value.name();
  }

}
