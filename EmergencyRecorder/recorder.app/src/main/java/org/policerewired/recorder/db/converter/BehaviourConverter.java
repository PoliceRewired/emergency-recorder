package org.policerewired.recorder.db.converter;

import androidx.room.TypeConverter;

import org.policerewired.recorder.constants.Behaviour;

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
