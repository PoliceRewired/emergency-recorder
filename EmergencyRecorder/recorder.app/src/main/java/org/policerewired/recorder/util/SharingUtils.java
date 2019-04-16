package org.policerewired.recorder.util;

import android.content.Context;

import org.policerewired.recorder.R;

import java.io.File;
import java.util.Date;

public class SharingUtils {

  private Context context;

  public SharingUtils(Context context) {
    this.context = context;
  }

  public String getFileProviderAuthority() {
    return context.getPackageName() + ".share";
  }

  public File getSharingPath() {
    File path = new File(context.getExternalCacheDir(), "sharing");
    if (!path.exists()) {
      path.mkdirs();
    }
    return path;
  }

  public File generate_photo_file(Date date) {
    return new File(getSharingPath(), generate_photo_filename(date));
  }

  public File generate_hybrid_photo_file(Date date) {
    return new File(getSharingPath(), generate_hybrid_photo_filename(date));
  }

  public File generate_hybrid_video_file(Date date) {
    return new File(getSharingPath(), generate_hybrid_video_filename(date));
  }

  public File generate_audio_file(Date date) {
    return new File(getSharingPath(), generate_audio_filename(date));
  }

  public File generate_video_file(Date date) {
    return new File(getSharingPath(), generate_video_filename(date));
  }

  public String generate_photo_filename(Date date) {
    return context.getString(R.string.photo_filename, getFilenameDate(date));
  }

  public String generate_hybrid_photo_filename(Date date) {
    return context.getString(R.string.hybrid_photo_filename, getFilenameDate(date));
  }

  public String generate_hybrid_video_filename(Date date) {
    return context.getString(R.string.hybrid_video_filename, getFilenameDate(date));
  }

  public String generate_video_filename(Date date) {
    return context.getString(R.string.video_filename, getFilenameDate(date));
  }

  public String generate_audio_filename(Date date) {
    return context.getString(R.string.audio_filename, getFilenameDate(date));
  }

  public String getFilenameDate(Date date) {
    return String.valueOf(date.getTime());
  }

}
