package org.policerewired.recorder.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.location.Location;

import org.policerewired.recorder.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ExifUtils {

  public static String getExifFormatLongitudeRef(Location location) {
    return location.getLongitude() < 0 ? "W" : "E";
  }

  public static String getExifFormatLongitude(Location location) {
    if (location == null) return "0/1,0/1,0/1000";
    return dec2DMS(location.getLongitude());
 }

  public static String getExifFormatLatitudeRef(Location location) {
    return location.getLatitude() < 0 ? "S" : "N";
  }

  public static String getExifFormatLatitude(Location location) {
    if (location == null) return "0/1,0/1,0/1000";
    return dec2DMS(location.getLatitude());
  }

  public static String dec2DMS(double coord) {
    coord = coord > 0 ? coord : -coord;
    String sOut = Integer.toString((int)coord) + "/1,";
    coord = (coord % 1) * 60;
    sOut = sOut + Integer.toString((int)coord) + "/1,";
    coord = (coord % 1) * 60000;
    sOut = sOut + Integer.toString((int)coord) + "/1000";
    return sOut;
  }

  public static String getExifFormatAltitude(Location location) {
    double altitude = Math.abs(location.getAltitude());
    return String.valueOf(altitude);
  }

  public static String getExifFormatAltitudeRef(Location location) {
    return location.getAltitude() < 0 ? "1" : "0";
  }

  public static String getExifFormatDate(Date date) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    return format.format(date);
  }

  public static String getExifGpsFormatTimestamp(Location location) {
    long time = location.getTime();
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(time);
    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
    int minutes = calendar.get(Calendar.MINUTE);
    int seconds = calendar.get(Calendar.SECOND);
    return hourOfDay + "/1," + minutes + "/1," + seconds + "/1";
  }

  public static String getExifFormatSoftwareName(Context context) {
    try {
      PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      String version = pInfo.versionName;
      return context.getString(R.string.exif_software, version);
    } catch (Exception e) {
      return context.getString(R.string.exif_software, "").trim();
    }
  }

}
