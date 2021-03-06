package org.policerewired.recorder.util;

import android.content.Context;
import android.os.Environment;

import org.policerewired.recorder.R;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class StorageUtils {

  private Context context;
  private NamingUtils naming;

  public StorageUtils(Context context) {
    this.context = context;
    this.naming = new NamingUtils(context);
  }

  public String photoFile(Date date) {
    return context.getString(R.string.photo_filename, naming.getConciseDate(date));
  }

  public String audioFile(Date date) {
    return context.getString(R.string.audio_filename, naming.getConciseDate(date));
  }

  public File tempAuditLogZipFile() throws IOException {
    // TODO: confirm Android P still permits unfettered access to this file from other apps
    File outputDir = context.getExternalCacheDir();
    File file = File.createTempFile("log_", ".zip", outputDir);
    return file;
  }

  public File tempPhotoFile(String suffix) throws IOException {
    File outputDir = context.getCacheDir();
    File file = File.createTempFile("photo_temp", suffix, outputDir);
    return file;
  }

  public File tempVideoFile(String suffix) throws IOException {
    File outputDir = context.getCacheDir();
    File file = File.createTempFile("video_processing", suffix, outputDir);
    return file;
  }

  public File tempAudioFile(String suffix) throws IOException {
    File outputDir = context.getCacheDir();
    File file = File.createTempFile("audio_temp", suffix, outputDir);
    return file;
  }

  public File tempAuditFile(String suffix) throws IOException {
    // TODO: confirm Android P still permits unfettered access to this file from other apps
    File outputDir = context.getExternalCacheDir();
    return File.createTempFile("log_temp", suffix, outputDir);
  }

  public File externalAudioFile(Date date, String suffix) throws IOException {
    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
    String filename = audioFile(date);
    File file = new File(dir, filename + suffix);
    return file;
  }

}
