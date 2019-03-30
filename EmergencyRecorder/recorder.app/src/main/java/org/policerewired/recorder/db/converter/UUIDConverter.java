package org.policerewired.recorder.db.converter;

import java.util.UUID;

import androidx.room.TypeConverter;

public class UUIDConverter {

  @TypeConverter
  public static UUID toUUID(String value) {
    return value == null ? null : UUID.fromString(value);
  }

  @TypeConverter
  public static String toString(UUID value) {
    return value == null ? null : value.toString();
  }


}
