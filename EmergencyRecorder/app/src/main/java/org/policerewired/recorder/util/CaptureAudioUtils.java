package org.policerewired.recorder.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.jcodec.common.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CaptureAudioUtils {
  private static final String TAG = CaptureAudioUtils.class.getSimpleName();

  /**
   * Derived from the Android internals insertVideo method, this method populates the
   * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
   * that is inserted manually gets saved at the end of the gallery (because date is not populated).
   */
  public static final String insertAudio(ContentResolver cr,
                                         File source,
                                         File target,
                                         String title,
                                         String description,
                                         String album,
                                         long duration_ms) {

    Uri url = null;
    String stringUrl = null;

    try {

      InputStream audioIn = new FileInputStream(source);
      OutputStream audioOut = new FileOutputStream(target);
      try {
        IOUtils.copy(audioIn, audioOut);
      } catch (Exception e) {
        Log.e(TAG, "Failed to copy video.", e);
      } finally {
        audioOut.close();
      }


      ContentValues values = new ContentValues();
      values.put(MediaStore.Audio.Media.TITLE, title);
      values.put(MediaStore.Audio.Media.DISPLAY_NAME, title);
      values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
      values.put(MediaStore.Audio.Media.IS_PODCAST, true);
      values.put(MediaStore.Audio.Media.IS_ALARM, false);
      values.put(MediaStore.Audio.Media.IS_MUSIC, true);
      values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
      values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
      // Add the date meta data to ensure the image is added at the front of the gallery
      values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
      values.put(MediaStore.Audio.Media.DURATION, duration_ms);
      values.put(MediaStore.Audio.Media.DATA, target.getAbsolutePath());
      values.put(MediaStore.Audio.Media.ALBUM, album);

      if (source != null) {
        url = cr.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
      }

    } catch (Exception e) {
      Log.e(TAG, "Unable to store audio.", e);
      if (url != null) {
        cr.delete(url, null, null);
        url = null;
      }
    }

    if (url != null) {
      stringUrl = url.toString();
    }

    return stringUrl;
  }


}
