package org.policerewired.recorder.DTO;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.camerakit.CameraKitView;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class HybridCollection {

  public List<byte[]> blobs;
  public List<Bitmap> bitmaps;
  public Date started;

  public long ms_per_blob;

  public HybridCollection(long ms_per_blob) {
    this.blobs = new LinkedList<>();
    this.bitmaps = new LinkedList<>();
    this.ms_per_blob = ms_per_blob;
    this.started = new Date(); // now!
  }

  public CameraKitView.FrameCallback frame_callback = new CameraKitView.FrameCallback() {
    @Override
    public void onFrame(CameraKitView cameraKitView, byte[] bytes) {
      Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
      bitmaps.add(bmp);
    }
  };

  public CameraKitView.ImageCallback image_callback = new CameraKitView.ImageCallback() {
    @Override
    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
      blobs.add(bytes);
    }
  };

}
