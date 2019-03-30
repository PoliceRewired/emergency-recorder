package org.policerewired.recorder.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.location.Location;

import com.google.android.gms.common.util.Strings;

import org.jcodec.common.StringUtils;

import java.util.Date;

public class PhotoAnnotationUtils {

  private Context context;
  private NamingUtils naming;

  public PhotoAnnotationUtils(Context context) {
    this.context = context;
    this.naming = new NamingUtils(context);
  }

  public Bitmap drawOnBitmap(Bitmap original, Date now, Location location, String geocode, String w3w) {

    Bitmap bmp = original.copy(original.getConfig(), true);
    Canvas canvas = new Canvas(bmp);

    canvas.drawBitmap(original, 0, 0, null);

    int textSize = 48;
    int textSpacing = 8;

    Paint paintText = new Paint();
    paintText.setStyle(Paint.Style.FILL);
    paintText.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
    paintText.setColor(Color.WHITE);
    paintText.setFakeBoldText(true);
    paintText.setTextSize(textSize);

    Paint paintOutline = new Paint();
    paintOutline.setStyle(Paint.Style.STROKE);
    paintOutline.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
    paintOutline.setColor(Color.BLACK);
    paintOutline.setFakeBoldText(true);
    paintOutline.setTextSize(textSize);

    int y = textSize + textSpacing;
    int x = textSpacing;

    canvas.drawText(naming.getLongFormatDate(now), x, y, paintText);

    if (location != null) {
      y += textSize + textSpacing;
      canvas.drawText(naming.describeLocation(location), x, y, paintText);
      canvas.drawText(naming.describeLocation(location), x, y, paintOutline);
    }

    if (!Strings.isEmptyOrWhitespace(geocode)) {
      y += textSize + textSpacing;
      canvas.drawText(naming.describeGeocode(geocode), x, y, paintText);
      canvas.drawText(naming.describeGeocode(geocode), x, y, paintOutline);
    }

    if (!Strings.isEmptyOrWhitespace(w3w)) {
      y += textSize + textSpacing;
      canvas.drawText(naming.describeW3W(w3w), x, y, paintText);
      canvas.drawText(naming.describeW3W(w3w), x, y, paintOutline);
    }

    return bmp;
  }

}
