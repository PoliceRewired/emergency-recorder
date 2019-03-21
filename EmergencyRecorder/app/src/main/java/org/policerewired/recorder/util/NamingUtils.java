package org.policerewired.recorder.util;

import android.content.Context;

import org.policerewired.recorder.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NamingUtils {

  private Context context;
  private Locale locale;

  public NamingUtils(Context context) {
    this.context = context;
    this.locale = context.getResources().getConfiguration().locale;
  }

  public String generate_photo_filename(Date date) {
    return context.getString(R.string.photo_name, getConciseDate(date));
  }

  public String generate_photo_title(Date date) {
    return context.getString(R.string.photo_title, getConciseDate(date));
  }

  public String generate_photo_description(Date date) {
    return context.getString(R.string.photo_description, getLongFormatDate(date));
  }

  public String getFilenameDate(Date date) {
    return String.valueOf(date.getTime());
  }

  public String getConciseDate(Date date) {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd hhmmss", locale);
    return format.format(date);
  }

  public String getLongFormatDate(Date date) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", locale);
    return format.format(date);
  }
}
