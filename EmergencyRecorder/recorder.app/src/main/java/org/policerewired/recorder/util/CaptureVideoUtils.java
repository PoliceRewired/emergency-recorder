package org.policerewired.recorder.util;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.jcodec.common.io.IOUtils;
import org.policerewired.recorder.EmergencyRecorderApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class CaptureVideoUtils {
  private static final String TAG = CaptureVideoUtils.class.getSimpleName();

  /**
   * Derived from the Android internals insertVideo method, this method populates the
   * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
   * that is inserted manually gets saved at the end of the gallery (because date is not populated).
   */
  public static Uri insertVideo(ContentResolver cr,
                                File source,
                                String title,
                                String description,
                                Date started,
                                String mime,
                                long duration_ms) {

    ContentValues values = new ContentValues();
    values.put(MediaStore.Video.Media.TITLE, title);
    values.put(MediaStore.Video.Media.DISPLAY_NAME, title);
    values.put(MediaStore.Video.Media.DESCRIPTION, description);
    values.put(MediaStore.Video.Media.MIME_TYPE, mime);
    // Add the date meta data to ensure the image is processed at the front of the gallery
    values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
    values.put(MediaStore.Video.Media.DATE_TAKEN, started.getTime());
    values.put(MediaStore.Video.Media.DURATION, duration_ms);

    Uri url = null;

    try {
      url = cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

      if (source != null) {
        InputStream videoIn = new FileInputStream(source);
        OutputStream videoOut = cr.openOutputStream(url);
        try {
          IOUtils.copy(videoIn, videoOut);
        } catch (Exception e) {
          Log.e(TAG, "Failed to copy video.", e);
          EmergencyRecorderApp.recordAnalyticsException(e);

        } finally {
          // cannot, must not, throw during a finally
          try { videoOut.close(); } catch (Exception e) { EmergencyRecorderApp.recordAnalyticsException(e); }
        }

        long id = ContentUris.parseId(url);
        // Wait until MINI_KIND thumbnail is generated.
        Bitmap miniThumb = MediaStore.Video.Thumbnails.getThumbnail(cr, id, MediaStore.Video.Thumbnails.MINI_KIND, null);
        // This is for backward compatibility.
        storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Video.Thumbnails.MICRO_KIND);
      } else {
        EmergencyRecorderApp.recordAnalyticsIssue("No video source File provided for storage.");
        cr.delete(url, null, null);
        url = null;
      }
    } catch (Exception e) {
      Log.e(TAG, "Unable to store video.", e);
      EmergencyRecorderApp.recordAnalyticsException(e);
      if (url != null) {
        cr.delete(url, null, null);
        url = null;
      }
    }

    return url;
  }

  /**
   * A copy of the Android internals StoreThumbnail method, it used with the insertVideo to
   * populate the android.provider.MediaStore.Video.Media#insertImage with all the correct
   * meta data. The StoreThumbnail method is private so it must be duplicated here.
   * @see android.provider.MediaStore.Video.Media (StoreThumbnail private method)
   */
  private static Bitmap storeThumbnail(
    ContentResolver cr,
    Bitmap source,
    long id,
    float width,
    float height,
    int kind) {

    // create the matrix to scale it
    Matrix matrix = new Matrix();

    float scaleX = width / source.getWidth();
    float scaleY = height / source.getHeight();

    matrix.setScale(scaleX, scaleY);

    Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
      source.getWidth(),
      source.getHeight(), matrix,
      true
    );

    ContentValues values = new ContentValues(4);
    values.put(MediaStore.Video.Thumbnails.KIND,kind);
    values.put(MediaStore.Video.Thumbnails.VIDEO_ID,(int)id);
    values.put(MediaStore.Video.Thumbnails.HEIGHT,thumb.getHeight());
    values.put(MediaStore.Video.Thumbnails.WIDTH,thumb.getWidth());

    Uri url = cr.insert(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, values);

    try {
      OutputStream thumbOut = cr.openOutputStream(url);
      thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
      thumbOut.close();
      return thumb;
    } catch (FileNotFoundException ex) {
      return null;
    } catch (IOException ex) {
      return null;
    }
  }

}
