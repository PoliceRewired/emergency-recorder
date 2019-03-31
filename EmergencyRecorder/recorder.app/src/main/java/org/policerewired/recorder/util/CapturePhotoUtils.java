package org.policerewired.recorder.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore.Images;
import android.util.Log;

import org.jcodec.common.io.IOUtils;
import org.policerewired.recorder.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import static org.policerewired.recorder.util.ExifUtils.getExifFormatAltitude;
import static org.policerewired.recorder.util.ExifUtils.getExifFormatAltitudeRef;
import static org.policerewired.recorder.util.ExifUtils.getExifFormatDate;
import static org.policerewired.recorder.util.ExifUtils.getExifFormatLatitude;
import static org.policerewired.recorder.util.ExifUtils.getExifFormatLatitudeRef;
import static org.policerewired.recorder.util.ExifUtils.getExifFormatLongitude;
import static org.policerewired.recorder.util.ExifUtils.getExifFormatLongitudeRef;
import static org.policerewired.recorder.util.ExifUtils.getExifFormatSoftwareName;
import static org.policerewired.recorder.util.ExifUtils.getExifGpsFormatTimestamp;

/**
 * Derived from work by Samuel Kirton.
 */
public class CapturePhotoUtils {
  private static final String TAG = CapturePhotoUtils.class.getSimpleName();

  /**
   * A copy of the Android internals insertImage method, this method populates the
   * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
   * that is inserted manually gets saved at the end of the gallery (because date is not populated).
   * @see Images.Media#insertImage(ContentResolver, Bitmap, String, String)
   */
  public static final Uri insertImage(Context context,
                                      Bitmap source,
                                      String title,
                                      String description,
                                      Date taken,
                                      Location location) {

    ContentResolver cr = context.getContentResolver();

    ContentValues values = new ContentValues();
    values.put(Images.Media.TITLE, title);
    values.put(Images.Media.DISPLAY_NAME, title);
    values.put(Images.Media.DESCRIPTION, description);
    values.put(Images.Media.MIME_TYPE, "image/jpeg");
    // Add the date meta data to ensure the image is added at the front of the gallery
    values.put(Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
    values.put(Images.Media.DATE_TAKEN, taken.getTime());

    File temp;
    try {
      temp = new StorageUtils(context).tempPhotoFile(".jpg");
      FileOutputStream tempStream = new FileOutputStream(temp);
      source.compress(Bitmap.CompressFormat.JPEG, 100, tempStream);
      tempStream.close();

      // write EXIF data
      try {
        modifyExifData(context, temp, taken, location, description);
      } catch (Exception e) {
        Log.w(TAG, "Could not write EXIF data. Saving image anyway.", e);
      }

      InputStream imageInTemp = new FileInputStream(temp);
      Uri uri = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
      OutputStream imageOut = cr.openOutputStream(uri);
      IOUtils.copy(imageInTemp, imageOut);

      long id = ContentUris.parseId(uri);
      Bitmap miniThumb = Images.Thumbnails.getThumbnail(cr, id, Images.Thumbnails.MINI_KIND, null);
      storeThumbnail(cr, miniThumb, id, 50F, 50F,Images.Thumbnails.MICRO_KIND);

      return uri;

    } catch (Exception e) {
      Log.e(TAG, "Unable to store image." ,e);
      return null;
    }
  }

  public static void modifyExifData(Context context, File file, Date taken, Location location, String description) throws IOException {
    ExifInterface exifs = new ExifInterface(file.getPath());
    exifs.setAttribute(ExifInterface.TAG_DATETIME, getExifFormatDate(taken));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      exifs.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, getExifFormatDate(taken));
    }

    if (location != null) {
      exifs.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, getExifGpsFormatTimestamp(location));
      exifs.setAttribute(ExifInterface.TAG_GPS_LATITUDE, getExifFormatLatitude(location));
      exifs.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, getExifFormatLatitudeRef(location));
      exifs.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, getExifFormatLongitude(location));
      exifs.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, getExifFormatLongitudeRef(location));
      exifs.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, getExifFormatAltitude(location));
      exifs.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, getExifFormatAltitudeRef(location));

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        exifs.setAttribute(ExifInterface.TAG_SOFTWARE, getExifFormatSoftwareName(context));
        exifs.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description);
      }
    }
    exifs.saveAttributes();
  }

  /**
   * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
   * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
   * meta data. The StoreThumbnail method is private so it must be duplicated here.
   * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
   */
  private static final Bitmap storeThumbnail(
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
    values.put(Images.Thumbnails.KIND,kind);
    values.put(Images.Thumbnails.IMAGE_ID,(int)id);
    values.put(Images.Thumbnails.HEIGHT,thumb.getHeight());
    values.put(Images.Thumbnails.WIDTH,thumb.getWidth());

    Uri url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

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