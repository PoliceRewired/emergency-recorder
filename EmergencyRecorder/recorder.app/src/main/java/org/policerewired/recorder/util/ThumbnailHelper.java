package org.policerewired.recorder.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

public class ThumbnailHelper {

  @SuppressWarnings("ConstantConditions")
  public static Bitmap getImageThumbnail(Context context, Uri content) {
    final int kind = MediaStore.Images.Thumbnails.MINI_KIND;
    BitmapFactory.Options opts = new BitmapFactory.Options();
    final long id = Long.parseLong(content.getLastPathSegment());
    return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, kind, opts);
  }

  @SuppressWarnings("ConstantConditions")
  public static Bitmap getVideoThumbnail(Context context, Uri content) {
    final int kind = MediaStore.Video.Thumbnails.MINI_KIND;
    BitmapFactory.Options opts = new BitmapFactory.Options();
    final long id = Long.parseLong(content.getLastPathSegment());
    return MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), id, kind, opts);
  }

}
