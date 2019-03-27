package org.policerewired.recorder.db.converter;

import org.policerewired.recorder.constants.Behaviour;

import androidx.room.TypeConverter;

public class BehaviourConverter {

  @TypeConverter
  public static Behaviour toBehaviour(String value) {
    return value == null ? null : Behaviour.valueOf(value);
  }

  @TypeConverter
  public static String toString(Behaviour value) {
    return value == null ? null : value.name();
  }

}
