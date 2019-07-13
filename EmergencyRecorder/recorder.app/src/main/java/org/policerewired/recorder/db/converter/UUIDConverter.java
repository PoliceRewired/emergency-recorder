package org.policerewired.recorder.db.converter;

import androidx.room.TypeConverter;

import java.util.UUID;

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
