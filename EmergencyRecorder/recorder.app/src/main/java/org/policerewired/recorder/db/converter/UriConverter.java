package org.policerewired.recorder.db.converter;

import android.net.Uri;

import androidx.room.TypeConverter;

public class UriConverter {

  @TypeConverter
  public static Uri toUri(String value) {
    return value == null ? null : Uri.parse(value);
  }

  @TypeConverter
  public static String toString(Uri value) {
    return value == null ? null : value.toString();
  }
}
