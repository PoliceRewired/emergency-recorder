package org.policerewired.recorder.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A class representing a collection of images, a start date, and an accompanying audio file.
 * Used to store everything that represents a 'hybrid' (aka 'burst mode') session.
 */
public class HybridCollection {

  public List<Pair<Date, byte[]>> blobs;
  public Date started;
  public File audio_file;

  public long ms_per_blob;

  public HybridCollection(long ms_per_blob) {
    this.blobs = new LinkedList<>();
    this.ms_per_blob = ms_per_blob;
    this.started = new Date(); // now!
  }

  public void addImage(byte[] bytes) {
    Date now = new Date();
    blobs.add(new Pair<>(now, bytes));
  }

}
